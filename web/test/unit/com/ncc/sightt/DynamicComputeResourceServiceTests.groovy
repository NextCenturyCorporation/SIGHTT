
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

import com.ncc.sightt.auth.Permissions
import com.ncc.sightt.auth.User
import com.ncc.sightt.compute.ComputeResourceProvider
import com.ncc.sightt.compute.EC2ComputeResourceInstance
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.apache.shiro.crypto.SecureRandomNumberGenerator
import org.apache.shiro.crypto.hash.Sha512Hash

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(DynamicComputeResourceService)
@Mock([Job, JobConfig, Background, ObjectModel, JobTask])
class DynamicComputeResourceServiceTests {

    def createValidJob(params) {
        populateValidParams(params)
        def passwordSalt = new SecureRandomNumberGenerator().nextBytes().getBytes()

        def owner = new User(username: "tester", passwordHash: new Sha512Hash("tester", passwordSalt, 1024), passwordSalt: passwordSalt)
        params.owner = owner
        params.permissions = Permissions.PRIVATE
        def job = new Job(params)
        def jobConfig = new JobConfig(params)
        job.config = jobConfig
        job.config.addToBackgrounds(new Background())
        job.config.addToObjectModels(new ObjectModel())
        job
    }

    void populateValidParams(map) {
        assert map != null
        map.clear()
        map.jobName = "TestJob"
        map.user = "Test"

        map.numComplete = 0
        map.numTasks = 1

        map.submitDate = new Date()
        map.expectedEndDate = new Date()
        map.actualEndDate = new Date()

        map
    }

    void testGetInstances() {
        def mockProvider = mockFor(ComputeResourceProvider)
        mockProvider.demand.getInstances(2) {
            return [
                    new EC2ComputeResourceInstance(instanceId: "xyz")
            ]
        }
        service.computeResourceProvider = mockProvider.createMock()

        assert service.getInstances().size == 1
        assert service.getInstances()[0].getIdentifier() == "xyz"
    }



    void testReduceInstances() {
        def mockProvider = mockFor(ComputeResourceProvider)
        mockProvider.demand.getInstances(1) {
            return [
                    new EC2ComputeResourceInstance(instanceId: "xyz"),
                    new EC2ComputeResourceInstance(instanceId: "abc"),
                    new EC2ComputeResourceInstance(instanceId: "mnp"),
                    new EC2ComputeResourceInstance(instanceId: "laksjd"),
            ]
        }
        mockProvider.demand.terminateInstances(1) { numToShutDown -> numToShutDown }
        service.computeResourceProvider = mockProvider.createMock()

        assert service.reduceInstances(2) == ["xyz", "abc"]
    }


    void testGetStatus() {
        service.addStringToStatus("test 1")
        service.addStringToStatus("test 2")

        assert service.getStatusString().contains("Starting")
        assert service.getStatusString().contains("test 1")
    }


    void testAddLatestEventAndGetLatestEvent() {
        def mockProvider = [
                getCapacity: { return [] },
                getInstances: {
                    return [
                            new EC2ComputeResourceInstance(instanceId: "xyz")]
                }
        ] as ComputeResourceProvider
        service.computeResourceProvider = mockProvider

        def j1params = [:]
        Job j1 = createValidJob(j1params)
        j1.numTasks = 10
        j1.numComplete = 4
        assert j1.validate()
        j1.save()

        def j2params = [:]
        Job j2 = createValidJob(j2params)
        j2.numTasks = 234
        j2.numComplete = 221
        assert j2.validate()
        j2.save()

        assert service.getStatus().size() == 1
        service.addLatestEvent()
        assert service.getLatestEvent().tasks == 19
        assert service.getStatus().size() == 2

        // Because everything is the same, getting latest should not add to status string
        service.addLatestEvent()
        assert service.getStatus().size() == 2
    }

    /**
     * Test trying to start too many instances, where max is DynamicComputeResourceService.NUM_MAX_TO_START_AT_ONCE
     */
    void testTooManyInstances() {

        def mockProvider = mockFor(ComputeResourceProvider)
        mockProvider.demand.addInstances(1) { numInstances -> return numInstances }
        service.computeResourceProvider = mockProvider.createMock()

        assert service.startInstances(DynamicComputeResourceService.NUM_MAX_TO_START_AT_ONCE + 21) == DynamicComputeResourceService.NUM_MAX_TO_START_AT_ONCE
    }

    /**
     * Check testing less than the max
     */
    void testVariousInstances() {
        def mockProvider = mockFor(ComputeResourceProvider)
        mockProvider.demand.addInstances(6) { numInstances -> return numInstances }
        service.computeResourceProvider = mockProvider.createMock()
        for (x in [0, 1, 2, 3, 4, 5]) {
            assert service.startInstances(x) == x
        }
    }
}
