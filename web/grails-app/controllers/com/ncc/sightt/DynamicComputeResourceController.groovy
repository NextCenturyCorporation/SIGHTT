
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

class DynamicComputeResourceController {

    static allowedMethods = [startInstances: "POST", updateSettings: "POST"]

    def dynamicComputeResourceService

    def index() {
        redirect action: 'listInstances'
    }

    def listInstances() {
        def instances = dynamicComputeResourceService.getInstances()
        def status = dynamicComputeResourceService.getStatusString()
        [instances: instances, numInstances: instances.size, status: status]
    }

    def requestInstances() {
        [config: dynamicComputeResourceService.computeResourceProvider.instanceConfiguration]
    }

    def startInstances() {
        def numStarted = dynamicComputeResourceService.startInstances(params["numInstances"] as Integer)
        flash.message = message(code: numStarted == 1 ? "started.instance.message" : "started.instances.message", args: ["${numStarted}"])
        redirect action: 'listInstances'
    }

    def stopInstance() {
        def instanceToStop = params["instanceId"]
        dynamicComputeResourceService.stopSpecificInstances([instanceToStop])
        flash.message = message(code: "stopped.instance.message", args: ["${instanceToStop}"])
        redirect action: 'listInstances'
    }

    def changeSettings() {
        [config: dynamicComputeResourceService.computeResourceProvider.instanceConfiguration]
    }

    def updateSettings() {
        def config = dynamicComputeResourceService.computeResourceProvider.instanceConfiguration
        config.instanceBaseId = params["ami"]
        config.instanceType = params["type"]
        config.instanceCapacity = params["capacity"]
        config.instanceDataFile = params["userdata"]
        [config: config]

    }
}
