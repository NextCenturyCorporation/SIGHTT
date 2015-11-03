
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
import com.ncc.sightt.message.ImageType
import grails.converters.JSON
import grails.util.Environment

class WizardController {
    // Maps the angle of rotation to the number of output images generated.
    // NOTE:  Keep these values as integers!
    static final def ROTATION_TO_IMAGES = [60: 66, 30: 492, 15: 4300,
            10: 13464, 5: 112036]

    static final def ROTATION_DEFAULT = 60

    // Available output image types.
    static final def IMAGE_TYPES = ImageType.ALL

    // The background image width is the size of the image on the screen.
    static final double BACKGROUND_IMAGE_WIDTH = 540

    // The size of the object model rendered by RenderModel
    static final double OBJECT_IMAGE_WIDTH = 800

    static final String SESSION_EXPIRED_TEXT = "Sorry, your session has expired. Please restart the wizard"

    def fileStorageService
    def imageService
    def userAccountsService
    def wizardService

    /**
     * This is used to obtain the list of camera locations
     */
    def taskService
    static final MAX_CAROUSEL_SIZE = 4;

    def index() {
        session['job'] = createJobMap()
        session['config'] = createJobConfigMap(new JobConfig())
        session['ephemeral'] = [:]
        redirect action: 'selectBackground'
    }

    private def createJobMap() {
        def map = [:]
        map += ['jobName': ""]
        map
    }

    private def createJobConfigMap(jobConfig) {
        def map = [:]
        map += ['backgrounds': jobConfig.backgrounds]
        map += ['objectModels': jobConfig.objectModels]
        map += ['numImages': jobConfig.numImages]
        map += ['degreeSpacing': jobConfig.degreeSpacing]
        map += ['position': jobConfig.position]
        map += ['pointsString': jobConfig.pointsString]
        map += ['modelBackgroundScale': jobConfig.modelBackgroundScale]
        map += ['imageType': jobConfig.imageType]
        map += ['reproducible': jobConfig.reproducible]
        map += ['generateAllMasks': jobConfig.generateAllMasks]
        map
    }

    def selectBackground(Integer max) {

        def listParams
        def backgroundList = userAccountsService.findAllVisible(Background.class, listParams)
        Map backgroundThumbnailSrcMap = imageService.getThumbnailSrcList(backgroundList)

        def messageType = "confirm"
        if (flash.message) {
            log.debug("There was a flash message: ${flash.message}")
        }
        log.debug("SESSION: " + session['config'])
        def selectedBackgrounds = session['config']?.backgrounds

        log.debug("Select Background session:\n${session}")
        log.debug("Select Background params:\n${params}")

        def backgroundFullSizeUrlMap = [:]
        for (background in backgroundList) {

            backgroundFullSizeUrlMap.put(background.id, fileStorageService.getImageSrcURI(background.filePath));
        }
        [
                backgroundFullSizeUrlMap: backgroundFullSizeUrlMap,
                backgroundThumbnailSrcMap: backgroundThumbnailSrcMap,
                backgroundList: backgroundList,
                selectedBackgrounds: null,
                preSelectedBackgrounds: selectedBackgrounds,
                uploadMessage: flash?.message,
                messageType: messageType,
                numCarouselItems: Math.min(MAX_CAROUSEL_SIZE, backgroundList?.size() ?: 0)
        ]
    }

    def getBackgroundUrl() {
        def json = params
        log.debug("GOT A BACKGROUND URL REQUEST: ")
        log.debug(json)
        def url = fileStorageService.getImageSrcURI(json.filePath)
        log.debug("URL: " + url)

        render url
    }

    def finishSelectBackground() {
        log.debug("Finish Select Background params:  ${params as JSON}")
        session['config'].backgrounds = JSON.parse(params['backgroundIds']).collect {
            Background.get(it)
        }
        log.debug("Backgrounds list after collect: ")
        log.debug(session['config'].backgrounds)

        session['uploadMessage'] = null
        if (params['next'] == "Advanced") {
            redirect action: 'groundPlaneControls'
        } else {
            redirect action: 'selectObject'
        }
    }

    def selectObject(Integer max) {
        def listParams
        def objectList = userAccountsService.findAllVisible(ObjectModel.class, listParams)
        Map modelThumbnailSrcMap = imageService.getThumbnailSrcList(objectList)
        log.debug "Object Model Thumbnails:"
        modelThumbnailSrcMap.each { key, value -> log.debug "    ${value}" }

        def selectedModel = 0
        if (session['config'].objectModels) {
            selectedModel = session['config'].objectModels.get(0).id
        }

        def selectedScale = 1.0
        if (session['config'].modelBackgroundScale) {
            selectedScale = reverseScaleCalculation(
                    session['config'].modelBackgroundScale)
        }

        log.debug("Select Object session:  ${session}")

        def disabledString = "disabled"
        def disabledClass = "sightt-disabled"
        if (selectedModel && selectedScale) {
            disabledString = "false"
            disabledClass = ""
        }

        [
                modelThumbnailSrcMap: modelThumbnailSrcMap,
                modelList: objectList,
                background: session['config'].backgrounds.get(0),
                selectedModel: ObjectModel.get(selectedModel),
                selectedScale: selectedScale,
                disabledString: disabledString,
                disabledClass: disabledClass,
                numCarouselItems: Math.min(MAX_CAROUSEL_SIZE, objectList.size())
        ]
    }

    def reverseScaleCalculation(modelBackgroundScale) {
        return modelBackgroundScale / (OBJECT_IMAGE_WIDTH / BACKGROUND_IMAGE_WIDTH)
    }

    def finishSelectObject() {
        log.debug("Finish Select Object params:  ${params}")
        session['config'].objectModels = [ObjectModel.get(params['modelId'])]
        session['config'].modelBackgroundScale = calculateScale(params['scaleData'].toString().toDouble())
        session['scaleData'] = params['scaleData']
        if (params['next'] == "Advanced") {
            redirect action: 'lightingControls'
        } else {
            redirect action: 'selectLocation'
        }
    }

    def lightingControls() {
        if (session.isNew()) {
            render "Sorry, your session has expired. Please restart the wizard"
            return
        }

        def modelLocation = fileStorageService.getImageSrcURI(session['config'].objectModels[0].objFilePath);

        def background = session['config'].backgrounds.get(0);
        def backgroundURL = fileStorageService.getImageSrcURI(background.filePath)
        [
                backgroundURL: backgroundURL,
                modelLocation: modelLocation
        ]
    }

    def setLightingModel() {
        log.debug("Got the following lighting model: " + params['lightingModel'])
        session['config'].lightingJSON = params['lightingModel']
        session['config'].customLighting = true
        redirect action: 'selectLocation'
    }

    def selectLocation() {
        def buttonDisabled = false;
        def locationDefault = ModelLocation.RANDOM

        def pointsString = session['config'].pointsString ?: [] as String;
        log.debug("In SelectLocation.  PointsString is: |" + pointsString + "|")
        def points = JSON.parse(pointsString)

        if ((session['config'].position) != null) {
            locationDefault = session['config'].position
            switch (locationDefault) {
                case ModelLocation.CENTERED:
                case ModelLocation.RANDOM:
                    break;

                case ModelLocation.POINT:
                    if (points?.size() < 1) {
                        buttonDisabled = true;
                    }
                    break;

                case ModelLocation.LINE:
                    if (points?.size() < 2) {
                        buttonDisabled = true;
                    }
                    break;

                case ModelLocation.POLYGON:
                    if (points?.size() < 3) {
                        buttonDisabled = true;
                    }
                    break;
            }
        }

        log.debug(" Select Location session:   ${session} ")


        [
                background: session['config'].backgrounds.get(0),
                model: session['config'].objectModels.get(0),
                scaleData: session['scaleData'],
                locationDefault: locationDefault,
                buttonDisabled: buttonDisabled,
                // NOTE:  the pointsString is a string of Position (x,y) objects, in JSON format.  This means it has
                // embedded double quotes.  For it to be passed by jQuery to the javascript during Location page init,
                // we have to escape those double quotes; otherwise, the string will be interpreted by the javascript
                // as a sequence of passed values rather than a single string.
                points: pointsString.replace("\"", "\\\"")
        ]
    }

    def calculateScale(scaleData) {
        // This should be map when we go to multi-background / multi-object
        double conversion = (OBJECT_IMAGE_WIDTH / BACKGROUND_IMAGE_WIDTH)
        double rescale = scaleData * conversion
        log.warn("Size of the background width is: " + BACKGROUND_IMAGE_WIDTH)
        log.warn("Scale from user is ${scaleData}.  Conversion: ${conversion}.  Final: ${rescale}")

        return rescale
    }

    /**
      * Store the parameters set by the selectLocations page in the session map.
      * @return
     */
    def finishSelectLocation() {
        session['config'].position = ModelLocation.valueOf(params['position'].toUpperCase())
        session['config'].pointsString = params['points']
        redirect action: 'selectNumber'
    }

    def selectNumber() {
        def rotationDefault = session['config'].degreeSpacing ?: ROTATION_DEFAULT

        if (isCustomRotation(session['config'].numImages, rotationDefault)) {

            rotationDefault = ""
            flash.message = "wizard.advanced.options.warning"
            flash.args = ["${session.config.numImages} images"]
        }

        log.debug("Select Number session:  ${session}")

        [
                background: session['config'].backgrounds.get(0),
                model: session['config'].objectModels.get(0),
                scaleData: session['scaleData'],
                rotationLabels: createRotationLabels(),
                rotationValues: createRotationValues(),
                rotationDefault: rotationDefault
        ]
    }


    def finishSelectNumber() {
        log.debug("Finish Select Number params:  ${params}")

        if (!session['config'].numImages || params['spacing']) {
            session['config'].numImages = 1
            if (params['genMethod'] == "rot") {
                log.debug("Using rotation values to determine number of images")
                session['config'].degreeSpacing = params['spacing'].toString().toInteger() ?: 0
                session['config'].numImages = ROTATION_TO_IMAGES[session['config'].degreeSpacing] ?: 1
            }
        }
        if (params['next'] == "Advanced") {
            redirect action: 'selectLimitedAspects', params: params
        } else {
            redirect action: 'jobSettings'
        }

    }

    def isCustomRotation(numImages, rotationDefault) {
        if (numImages == 1 && rotationDefault == 360) {
            return false
        }
        return numImages && numImages != ROTATION_TO_IMAGES[rotationDefault]
    }

    def createRotationLabels() {
        def labels = ["Generate a single image"]
        ROTATION_TO_IMAGES.each { key, value ->
            labels += "${key}\u00B0 (${value} images)"
        }
        return labels
    }

    def createRotationValues() {
        def values = [360]
        ROTATION_TO_IMAGES.each { key, value -> values << key }
        return values
    }

    /**
     * Send a list of the camera orientations to the client
     * @return
     */
    def cameraList() {
        def numPoints = session['config'].numPoints

        def tessellation
        try {
            tessellation = wizardService.produceCameraTessellation(numPoints)
        } catch (e) {
            tessellation = [error: e.message]
            log.warn("No tessellation produced: " + e.message)
        }

        render tessellation as JSON
    }

    def selectLimitedAspects() {
        if (session.isNew()) {
            render SESSION_EXPIRED_TEXT
            return
        }

        def numPoints = ROTATION_TO_IMAGES[params['spacing'].toString().toInteger()] ?: 1
        session['config'].numPoints = numPoints
        def modelLocation = fileStorageService.getImageSrcURI(session['config'].objectModels[0].objFilePath);
        [modelLocation: modelLocation]
    }

    def setActiveCameras() {
	try {
	    log.warn("limited aspects data: " + params.limitedAspectsData)

	    def limitedAspectsData = JSON.parse(params.limitedAspectsData)
            boolean customNumBool = false
            int customNumImages = 0

            if (limitedAspectsData?.customNumberOfImages != null && limitedAspectsData?.customNumberOfImages > 0) {
                customNumImages = Integer.parseInt(limitedAspectsData?.customNumberOfImages)
                customNumBool = true
            }

            def states = limitedAspectsData.camList
            def camActivationList = states.collect {
                it.active
            }
	    log.debug("Here is the cam activation list: ")
            log.debug(camActivationList as JSON).toString()
            if (session['ephemeral'] != null) {
                session['ephemeral'].activeCameras = camActivationList
                session['config'].customCameras = true
                session['config'].activeCamerasJSON = (camActivationList as JSON).toString()
                session['config'].reproducible = !limitedAspectsData.randomize
    
                if (customNumBool) {
                    session['config'].numImages = customNumImages
                }
            } else {
                log.warn("SESSION IS NULL!")
            }
        }
        catch (Exception e) {
	    log.warn("Params: " + params)
	    log.warn("CATCH: limited aspects data: " + params.limitedAspectsData)
	    e.printStackTrace()
	    log.warn(e.getMessage())
        }
	
	log.warn("returning from set active cameras: " +params)
	log.warn("Deleting limitedAspectsData from params")
	params.limitedAspectsData=null
        redirect action: 'jobSettings', params: params
    }
    
    def jobSettings() {
        log.debug("Valid Image Types: " + IMAGE_TYPES)
        [
                validImageTypes: IMAGE_TYPES,
                defaultPrivacy: userAccountsService.currentUser.preferences.defaultPrivacy
        ]
    }

    def finishJobSettings() {
        def jobSettings = JSON.parse(params['data'])
        log.debug("Received the following: " + params['data'])
        session['job'].jobName = jobSettings.jobName
        session['job'].permissions = (jobSettings.private ? Permissions.PRIVATE : Permissions.PUBLIC)
        session['config'].generateAllMasks = jobSettings.generateAllMasks
        session['config'].imageType = jobSettings.imageType
        redirect action: 'summary'
    }

    def summary() {
        log.debug("Summary session:  ${session}")

        def orientationTypeString = OrientationType.RANDOM.toString()
        if (session['config'].reproducible) {
            orientationTypeString = OrientationType.REPRODUCIBLE.toString()
        }

        def generateMasksString = "Single Image"
        if (session['config'].generateAllMasks) {
            generateMasksString = "All Layers"
        }

        def actualNumImages = session['config'].numImages
        if (session.config.activeCamerasJSON != null) {
            def cameras = session['ephemeral'].activeCameras
            def numCameras = cameras.size()
            if (numCameras > 0) {
                def numActiveCameras = cameras.findAll { it == true }.size()
                def imagesPerCamera = session['config'].numImages / numCameras
                actualNumImages = (imagesPerCamera * numActiveCameras) as Integer
                actualNumImages = "~${actualNumImages} (from ${numActiveCameras} active aspect${numActiveCameras > 1 ? "s" : ""})"
            }
        }

        [
                backgrounds: session['config'].backgrounds,
                model: session['config'].objectModels.get(0),
                scaleData: session['scaleData'],
                numImages: actualNumImages,
                position: session['config'].position,
                degreeSpacing: session['config'].degreeSpacing,
                orientationType: orientationTypeString,
                imageType: session['config'].imageType,
                generateMasksString: generateMasksString,
                jobName: session['job'].jobName,
                jobPrivacy: session['job'].permissions,
                lightingModel: session['config'].lightingJSON ? JSON.parse(session['config'].lightingJSON) : null,
                groundPlaneModel: session['config'].groundPlaneJSON ? JSON.parse(session['config'].groundPlaneJSON) : null
        ]
    }

    def startJob() {
        log.info("Starting job:  ${session.job} ${session.config}")
        log.debug("PARAMS: ${params}")
        params['job'] = session['job']
        params['config'] = session['config']
        def privacyString = params['_private']
        log.debug("PRIVATE: ${privacyString}")
        log.debug("Setting job permissions to: ${params['job'].permissions}")
        forward(controller: "job", action: "save", params: params)
    }


    def groundPlaneControls() {
        float scaleData = 0.001
        def model = ObjectModel.get(params['modelId'] ?: 1)
        def background = session['config'].backgrounds.get(0);
        def backgroundURL = fileStorageService.getImageSrcURI(background.filePath)
        [
                backgroundURL: backgroundURL,
                backgroundWidth: background.width,
                backgroundHeight: background.height,
                model: model,
                scaleData: scaleData,
        ]
    }

    def finishGroundPlaneControls() {
        session['config'].customGroundPlane = true
        session['config'].groundPlaneJSON = params['groundPlaneData']
        redirect action: 'selectObject'
    }

/**
 * Render the resize window
 */
    def resize() {
        render
    }

    def cloneJob() {
        def config = flash.jobInstance.config
        session['job'] = createJobMap()
        session['config'] = createJobConfigMap(config)
        log.debug("Clone Job session:  ${session}")
        redirect action: 'selectBackground'
    }


}
