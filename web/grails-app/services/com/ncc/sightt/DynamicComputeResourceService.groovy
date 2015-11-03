
/*********************************************************************************************************
 * Software License Agreement (BSD License)
 * 
 * Copyright 2014 Next Century Corporation. All rights reserved.   
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ***********************************************************************************************************/
package com.ncc.sightt

import com.ncc.sightt.JobStatus
import com.ncc.sightt.compute.ComputeResourceInstance
import groovy.util.logging.Log4j

/**
 * DynamicComputeResourceService determines the number of instances
 * that there should be and either launches or shuts down instances to
 * make that the case.
 */
@Log4j
class DynamicComputeResourceService {
    static MIN_RUNNING_INSTANCES = 0

    class DynamicComputeEvent {
        Date date
        def numInstances
        def capacity
        def tasks
    }

    List<ComputeResourceInstance> runningInstances;
    Queue<String> status
    Queue<DynamicComputeEvent> events
    static final int HISTORY_LENGTH = 50

    static final int NUM_MAX_TO_START_AT_ONCE = 10

    // Injected by resources.groovy, usually an EC2ComputeResourceProvider
    def computeResourceProvider

    def DynamicComputeResourceService() {
        status = new LinkedList<String>()
        addStringToStatus(new String("Starting dynamic compute resource service"))
        events = new LinkedList<DynamicComputeEvent>()
        createNewDynamicComputeEvent(null, MIN_RUNNING_INSTANCES, 0)
    }

    def getInstances() {
        computeResourceProvider.getInstances()
    }

    /**
     * Start some number of instances, returning number actually started
     */
    def startInstances(numInstances) {
        if (numInstances > NUM_MAX_TO_START_AT_ONCE) {
            log.error("Throttling numinstances to start to NUM_MAX_TO_START_AT_ONCE: ${NUM_MAX_TO_START_AT_ONCE}")
            numInstances = NUM_MAX_TO_START_AT_ONCE
        }
        int started = computeResourceProvider.addInstances(numInstances)
        if (started != numInstances) {
            log.warn("Unable to start ${numInstances}.  Got ${started} instead")
        }
        started
    }

    /**
     * Stop instances, passing in list of instance ids
     * @param instancesList list of instance ids, in string form
     * @return list of instance ids that were actually shut down
     */
    def stopSpecificInstances(instancesList) {
        computeResourceProvider.terminateInstances(instancesList)
    }

    /**
     * Shut down some instances
     *
     * TODO: use the time that the instances have been up in this logic.  Since the
     * amount charged is a function of the number of integer hours an instance has been up,
     * the oldest may not be the best to shut down
     */
    def reduceInstances(numInstancesToShutDown) {
        def instances = getInstances()

        // Require at least one instance to stay up
        if (numInstancesToShutDown > instances.size() - MIN_RUNNING_INSTANCES) {
            log.error("Asking to shut down ${numInstancesToShutDown} but only ${instances.size} up")
            numInstancesToShutDown = instances.size() - MIN_RUNNING_INSTANCES
        }
        def instancesToShutDown = []
        for (int ii = 0; ii < numInstancesToShutDown; ii++) {
            instancesToShutDown.add(instances.get(ii).getIdentifier())
        }
        return stopSpecificInstances(instancesToShutDown)
    }

    def getStatusString() {
        StringBuffer sb = new StringBuffer()
        status.reverse().each { it ->
            if (it) {
                sb.append(it)
            }
            sb.append("\n")
        }
        return sb.toString()
    }

    /**
     * This method calculates the number of tasks that are in flight, the
     * total capacity of the 'compute cloud' and adjusts the number of
     * running instances as necessary
     *
     * @return the new number of instances that are running as well as the list of instances
     */
    def scaleForDemand() {
        addLatestEvent()
        int numToStart = determineNumberInstancesToStartOrStop()

        if (numToStart > 0) {
            startInstances(numToStart)
        } else if (numToStart < 0) {
            reduceInstances(-1 * numToStart)
        }
    }

    /**
     * Logic to determine now many instances to start
     */
    def determineNumberInstancesToStartOrStop() {
        DynamicComputeEvent lastEvent = getLatestEvent()
        int numInstances = lastEvent.numInstances

        // Determine the total number of seconds to go
        def listOfJobMaps = []
        int secondsToGoForAllJobs = 0;
        Job?.list()?.each { job ->
            if (job.status != JobStatus.COMPLETE) {
                int numJobs = (job.numTasks - job.numComplete)
                double avgTime = job.getMeanOfExecutionTime()
                int totalTime = avgTime * numJobs
                secondsToGoForAllJobs += totalTime
                def jobMap = [id: job.id, numJobs: numJobs, avgTime: avgTime, totalTime: totalTime]
                listOfJobMaps.add(jobMap)
            }
        }

        if (secondsToGoForAllJobs > 0) {
            listOfJobMaps.each { it ->
                addStringToStatus(" Job ${it.id}: ${it.numJobs} at avg of ${it.avgTime} sec gives ${it.totalTime} sec")
            }
            addStringToStatus(" Num seconds to go: ${secondsToGoForAllJobs}")
        }

        // If not much time to go, shut down all the machines except 1
        if (secondsToGoForAllJobs < 500) {
            if (numInstances == 1) {
                return 0
            }
            addStringToStatus(" ${secondsToGoForAllJobs} sec to go, so shutting down all but 1")
            return -1 * (numInstances - 1)
        }

        // shutdown all workers if no jobs left...
        if (secondsToGoForAllJobs == 0) {
            return (-1 * numInstances)
        }

        // Try to make the entire time about 1 hour
        int numCpusPerMachine = 8
        int secondsPerHour = 3600
        int numDesiredMachines = secondsToGoForAllJobs / (secondsPerHour * numCpusPerMachine)
        if (numInstances >= numDesiredMachines) {
            addStringToStatus(" ${secondsToGoForAllJobs} sec to go, so computing ${numDesiredMachines} " +
                    "but have ${numInstances} currently")
            return 0
        }
        int numToStart = numDesiredMachines - numInstances
        return numToStart
    }

    def getLatestEvent() {
        if (events.size() > 0) {
            return events.get(events.size() - 1)
        }
        return
    }


    def addLatestEvent() {
        def capacityInfo = computeResourceProvider.capacity
        def numTasksInFlight = 0
        Job?.list()?.each {
            numTasksInFlight += it.numTasks - it.numComplete
        }
        int numInstances = getInstances().size()

        def event = createNewDynamicComputeEvent(capacityInfo, numInstances, numTasksInFlight)
        updateStatus(event)
    }

    def createNewDynamicComputeEvent(capacity, numInstances, numTasksInFlight) {
        DynamicComputeEvent d = new DynamicComputeEvent()
        d.date = new Date()
        d.capacity = capacity
        d.numInstances = numInstances
        d.tasks = numTasksInFlight
        d
    }


    def updateStatus(DynamicComputeEvent event) {
        if (events.size() < 1) {
            log.debug("Adding event because size was 0")
            events.add(event)
            addEventToStatusList(event)
            return
        }

        // Only add if it is different
        DynamicComputeEvent last = events.get(events.size() - 1)
        if (last.tasks != event.tasks || event.numInstances != last.numInstances) {
            log.debug "Adding new event"
            events.add(event)
            while (events.size() > 50) {
                events.removeFirst()
            }
            addEventToStatusList(event)
        } else {
            log.debug "same event, not adding"
            int idx = status.size() - 1
            status.set(idx, status.get(idx) + ".")
        }
    }

    def addEventToStatusList(DynamicComputeEvent event) {
        String newEventString = new String("  #inst: " + event.numInstances +
                "  #tasks: " + event.tasks)
        addStringToStatus(newEventString)
    }

    def addStringToStatus(String newStatus) {
        status.add("" + new Date() + " " + newStatus)
        while (status.size() > 50) {
            status.removeFirst()
        }
    }
}
