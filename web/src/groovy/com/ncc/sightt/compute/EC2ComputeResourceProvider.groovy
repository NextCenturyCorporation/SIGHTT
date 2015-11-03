
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
package com.ncc.sightt.compute

import com.amazonaws.services.ec2.model.CreateTagsRequest
import com.amazonaws.services.ec2.model.RunInstancesRequest
import com.amazonaws.services.ec2.model.Tag
import com.amazonaws.services.ec2.model.TerminateInstancesRequest

class EC2ComputeResourceProvider implements ComputeResourceProvider {


    final static def TASKCONSUMER_MIN_START = 1

    def amazonWebService
    def grailsApplication
    def instanceConfiguration

    def getInstances() {
        def instances = []
        amazonWebService.ec2.describeInstances().reservations.each {
            def reservationId = it.reservationId
            it.instances.each {
                def instance = new EC2ComputeResourceInstance()
                instance.instanceId = it.instanceId
                instance.imageId = it.imageId
                instance.instanceType = it.instanceType
                instance.tags = it.tags
                instance.name = it.tags.find { it.key.equalsIgnoreCase("name") }?.value
                instance.instance = it
                instance.reservationId = reservationId
                instances.push(instance)
            }
        }

        def processingInstances = instances.findAll {
            ((it.instance.state.name == "running" || it.instance.state.name == "pending") && it.name == "dynamic-consumer")
        }
        processingInstances
    }

    def getNumRunningInstances() {
        getInstances().size
    }

    def addInstances(numInstances) {
        def userData = getUserData()
        def request = new RunInstancesRequest(instanceConfiguration.instanceBaseId, TASKCONSUMER_MIN_START, numInstances)
        def result = amazonWebService.ec2.runInstances(request
                .withInstanceType(instanceConfiguration.instanceType)
                .withUserData(userData)
                .withKeyName(instanceConfiguration.instanceKeyPair)
                .withSecurityGroupIds(instanceConfiguration.instanceFirewall))
        def defTag = new Tag("Name", "dynamic-consumer")

        result.reservation.instances.each {
            log.debug("Tagging instance ${it.instanceId}")
            def tagRequest = new CreateTagsRequest()
            tagRequest.withResources(it.instanceId)
                    .withTags(defTag)
            amazonWebService.ec2.createTags(tagRequest)
        }
        result.reservation.instances.size
    }

    def terminateInstances(instanceList) {
        log.debug("Terminating instances: " + instanceList)
        def request = new TerminateInstancesRequest(instanceList)
        def result = amazonWebService.ec2.terminateInstances(request)
        result
    }

    def getUserData() {
        def userData = grailsApplication.mainContext.getResource(instanceConfiguration.dataFilePrefix + instanceConfiguration.instanceDataFile).file.text.bytes
        org.apache.commons.codec.binary.Base64.encodeBase64String(userData)
    }

    def getCapacityPerInstance() {
        instanceConfiguration.instanceCapacity
    }

    /**
     * Return the capacity of the current compute resources.
     *
     * Capacity is defined as the number of tasks a given consumer can process times the number of consumers per instance
     */
    def getCapacity() {
        [totalCapacity: numRunningInstances * capacityPerInstance, instanceCapacity: capacityPerInstance]
    }
}
