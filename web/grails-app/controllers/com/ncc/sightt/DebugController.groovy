
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

import grails.converters.JSON

/**
 * DebugController is a controller to put random (not ready for prime time) UI
 * elements, so that you can see what is going on, without going through the entire
 * Wizard each time.
 *
 */
class DebugController {

    def fileStorageService
    def wizardService
    static numPoints

    def index() {
        def debugActions = [:]
        log.debug("controllerName: ${controllerName}")
        grailsApplication.controllerClasses.each {
            log.debug("Controller: ${it.fullName}")
        }
        def controllerClass = grailsApplication.getControllerClass("com.ncc.sightt.DebugController")
        for (String uri : controllerClass.uris) {
            debugActions[controllerClass.getMethodActionName(uri)] = uri
        }
        log.debug(debugActions as JSON)
        [actions: debugActions]
    }

    def exportJob(long id) {
        def job = Job.get(id)
        render job as grails.converters.deep.JSON

    }

    def callLimitedAspects() {
        def model = ObjectModel.get(params['modelId'] ?: 1);
        numPoints = (params['numPoints'] ?: 66) as Integer
        log.debug("numPoints: " + numPoints)
        def modelLocation = fileStorageService.getImageSrcURI(model?.objFilePath);
        render(view: '/wizard/selectLimitedAspects', model: [modelLocation: modelLocation])
    }

    def showAspects(long id) {
        log.debug("JobId: " + id)
        def job = Job.get(id)
        log.debug(job)
        def cameras = JSON.parse(job.config.activeCamerasJSON)
        def modelLocation = fileStorageService.getImageSrcURI(job.config.objectModels[0]?.objFilePath);
        def numPoints = job.config.numImages
        def numPointsInJob = (numPoints / cameras.count { it == true }) * cameras.size()
        [activeCameras: cameras, jobId: id, modelLocation: modelLocation, numPointsInJob: numPointsInJob]
    }

    def lightingControls() {
        def model = ObjectModel.get(params['modelId'] ?: 1);
        def modelLocation = fileStorageService.getImageSrcURI(model?.objFilePath);
        render(view: '/wizard/lightingControls', model: [modelLocation: modelLocation])
    }

    def setLightingModel() {

        render("<p>Received the following lightingModel: " + params['lightingModel'] + "</p>")
    }

    def cameraList() {
        log.debug("CameraList: " + params)

        numPoints = (numPoints ?: 66) as Integer
        log.debug("numPoints(in cameraList): " + numPoints)

        render wizardService.produceCameraTessellation(numPoints) as JSON
    }

    def setActiveCameras() {
        render("<p>Received the following from limitedAspects: " + params + "</p>")
    }
}
