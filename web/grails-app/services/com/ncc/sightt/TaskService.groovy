
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

import com.ncc.sightt.jms.JmsUtil
import com.ncc.sightt.message.JobMessage
import com.ncc.sightt.message.ModelRenderMessage
import com.ncc.sightt.message.TaskRenderMessage
import grails.converters.JSON
import grails.plugin.jms.Queue
import org.apache.activemq.command.ActiveMQTextMessage
import org.atmosphere.cpr.AtmosphereHandler
import org.atmosphere.cpr.AtmosphereResource
import org.atmosphere.cpr.AtmosphereResourceEvent
import org.atmosphere.cpr.BroadcasterFactory
import org.codehaus.groovy.grails.web.mapping.LinkGenerator

import javax.jms.TextMessage
import java.awt.*

class TaskService implements AtmosphereHandler {
    static transactional = false
    static exposes = ['jms']
    def jmsService
    def fileStorageService

    def imageService
    LinkGenerator grailsLinkGenerator
    static Random random

    // MAGIC NUMBER:  The size of the canvas shown to the user
    static final CANVAS_WIDTH = 540

    static {
        random = new Random(new Date().getTime())
    }

    static final double PI180 = 180 / Math.PI

    /**
     * Called when a new job is created (JobController)
     */
    @Queue
    def generateAndRunTasks(jobId) {
        try {
            def job = Job.get(jobId)
            if (job && job.config && job.config.position) {
                log.debug("Generating tasks for job ${jobId}  position: " + job.config.position)
            } else {
                log.error("Problems getting job: " + job)
                return null
            }

            generateTasks(job)
            job.status = JobStatus.RUNNING
            log.debug("Saving job")
            saveJob(job)
            log.debug("Sending task render messages")
            sendTaskRenderMessages(job)
            // Notify the web page that the job is created
            broadcastToBrowser(jobId, "updateTask", [type: "initialized"] as JSON)

        }
        catch (Exception e) {
            log.error("Problem in generateAndRunTasks.", e)
        }
        return null
    }

    /**
     * For each task, pick a random background and a random rendered image from a model and composite them together.
     */
    void generateTasks(job) {
        createTasksForNumberOfImages(job)
        job.numTasks = job.jobTasks.size()
        job.numComplete = 0
        job.status = JobStatus.INITIALIZED
        log.debug("Number of tasks created in generateTasks: " + job.numTasks)
    }

    def createTasksForNumberOfImages(job) {

        def numImagesReq = job.config.numImages
        log.debug("Generating ${numImagesReq} job tasks for job ${job.id}")
        PointsOnSphere pos = new PointsOnSphere()
        def results = pos.getThetaPhiRotationPoints(numImagesReq)
        if (log.isDebugEnabled()) {
            showPoints(results)
        }
        if (!job.config.reproducible) {
            log.warn("Random Job!")
            modifyRandomly(results)
        }
        results = purgeInactiveCameras(job, results)


        def locationCalculator = getLocationCalculator(job)

        def index = 1

        results.points.each { rot -> createTask(job, rot.x, rot.y, rot.z, index++, locationCalculator) }
        job.config.numImages = results.points.size()
    }

    def purgeInactiveCameras(job, results) {
        def currentAspect = 0
        log.debug("About to remove inactive cameras")
        if (job.config.customCameras == true) {
            def aspectValidityList = JSON.parse(job.config.activeCamerasJSON)
            def validityListSize = aspectValidityList.size()
            if (validityListSize == 0)
            {
                log.warn("validityListSize is zero, so not purging any cameras")
                return results
            }
            def resultsSize = results.points.size()
            if (resultsSize % validityListSize != 0) {
                log.warn("INVALID POINTS RETURNED (${resultsSize}%${validityListSize}==${resultsSize % validityListSize})! NOT PURGING!")
                return results
            }
            def numRotations = resultsSize / validityListSize
            log.debug(aspectValidityList)
            results.points.removeAll {
                def cameraBin = (currentAspect / numRotations) as Integer
                log.debug("Camera $currentAspect is: ${aspectValidityList[cameraBin]}")
                def remove
                if (aspectValidityList[cameraBin] == false) {
                    log.debug "Removing aspect ${currentAspect} of camera ${cameraBin}"
                    remove = true
                } else {
                    log.debug "Keeping aspect ${currentAspect} of camera ${cameraBin}"
                    remove = false
                }
                currentAspect++
                remove
            }

            if (log.isDebugEnabled()) {
                log.debug("**Purged aspect list**")
                showPoints(results)
            }
        } else {
            log.debug "Camera activation list was empty, all cameras active!"
        }
        results
    }

    def showPoints(results) {
        results.points.each { i ->
            def dx = i.x * PI180
            def dy = i.y * PI180
            def dz = i.z * PI180
            log.debug " ${i.x.round(3)} ${i.y.round(3)} ${i.z.round(3)}  ==   " +
                    "${dx.round(0)} ${dy.round(0)} ${dz.round(0)}"
        }
    }

    /**
     * Get a ModelLocationCalculator for the job.
     *
     * pointsListFromConfigString is a List of Object, each with double x and y, but ModelLocationCalculator
     * needs a List<java.awt.Point> so it can calculate distances, and also so x and y are ints
     *
     * Then, the original point list is in canvas coordinates [0,540] pixels in X.  We have to
     * convert to image coordinates
     */
    def getLocationCalculator(job) {

        def points = []
        def pointsListFromConfigString = JSON.parse(job.config.pointsString)

        pointsListFromConfigString.each { pt -> points.add(new Point(pt.x as int, pt.y as int)) }

        def background = (Background) Utils.pickRandom(job.config.backgrounds)
        def bw = background.width
        def bh = background.height
        def scale = bw / CANVAS_WIDTH

        points.each { pt ->
            pt.x = pt.x * scale
            pt.y = pt.y * scale
            log.warn("Points (scaled): " + pt.x + " " + pt.y)
        }

        def locCalc = new ModelLocationCalculator(job.config.position, points, new Point(bw, bh))
        return locCalc
    }

    def modifyRandomly(results) {
        // Size of the angle between points
        def angle = results.angle
        for (rot in results.points) {
            rot.x = varyAngle(rot.x, angle / 5, -Math.PI, Math.PI)
            rot.y = varyAngle(rot.y, angle / 5, 0, Math.PI)
            rot.z = varyAngle(rot.z, angle / 10, -Math.PI, Math.PI)
        }
    }

    def varyAngle(initAngle, variance, min, max) {
        def vary = random.nextGaussian() * variance
        initAngle += vary
        while (initAngle < min) {
            initAngle += (max - min)
        }
        while (initAngle > max) {
            initAngle -= (max - min)
        }
        initAngle
    }

    def createTask(job, yaw, pitch, roll, taskNumber, locationCalculator) {
        ObjectModel objectModel = (ObjectModel) Utils.pickRandom(job.config.objectModels)
        Background background = (Background) Utils.pickRandom(job.config.backgrounds)
        def scaleFactor = job.config.modelBackgroundScale
        def locationPoint = locationCalculator.getPoint()
        def locationX = locationPoint.x
        def locationY = locationPoint.y
        def thumbnail = new Thumbnail()
        if (!thumbnail.save()) {
            log.warn "Saving thumbnail failed:"
            thumbnail.errors.each { log.warn it }
        }
        CompositeImage composite = new CompositeImage(bucketName: fileStorageService.fileStorageConfiguration.bucketName,
                name: "PLACEHOLDER", filePath: "NA", thumbnail: thumbnail, valid: false)
        def task = new JobTask(taskTimeStart: new Date().getTime(), taskTimeEnd: 0, executionTimeStart: 0,
                executionTimeEnd: 0, completed: false, running: false, background: background,
                objectModel: objectModel, compositeImage: composite, scaleFactor: scaleFactor,
                locationX: locationX, locationY: locationY, yaw: yaw, pitch: pitch, roll: roll, taskNumber: taskNumber)
        if (job.config.customLighting == true) {
            def lightingData = JSON.parse(job.config.lightingJSON)
            def newXYZ = thetaPhiToXYZ(lightingData.theta, lightingData.phi)
            task.sunLocation = "(" + newXYZ.x + ", " + newXYZ.y + ", " + newXYZ.z + ")"
            task.sunIntensity = lightingData.intensity
            task.useAmbient = true
            task.ambientIntensity = lightingData.ambient
            task.useLightingModel = true
        }

        if (job.config.customGroundPlane == true) {
            def groundData = JSON.parse(job.config.groundPlaneJSON)
            task.groundPlanePositionX = groundData.position.x
            task.groundPlanePositionY = groundData.position.y
            task.groundPlanePositionZ = groundData.position.z
            task.groundPlaneRotationX = groundData.rotation.x
            task.groundPlaneRotationY = groundData.rotation.y
            task.useGroundPlaneModel = true
        }
        job.addToJobTasks(task)
    }

    def thetaPhiToXYZ(theta, phi) {
        def x = Math.cos(phi) * Math.cos(theta);
        def y = Math.cos(phi) * Math.sin(theta);
        def z = Math.sin(phi);
        return [x: x, y: y, z: z];
    }

    /**
     * Send the TaskRenderMessagees, one for each task
     */
    def sendTaskRenderMessages(job) {
        job.jobTasks.each { JobTask task ->
            TaskRenderMessage msg = new TaskRenderMessage()
            msg.bucketName = fileStorageService.getFileStorageConfiguration().bucketName
            msg.backgroundName = task.background.name
            msg.backgroundKey = task.background.filePath
            msg.modelName = task.objectModel.name
            msg.modelKey = task.objectModel.filePath
            msg.scaleFactor = task.scaleFactor
            msg.pointX = task.locationX
            msg.pointY = task.locationY
            msg.taskId = task.id
            msg.jobName = job.jobName
            msg.jobId = job.id
            msg.yaw = task.yaw
            msg.pitch = task.pitch
            msg.roll = task.roll
            msg.useLightingModel = task.useLightingModel
            msg.sunLocation = task.sunLocation
            msg.sunIntensity = task.sunIntensity
            msg.ambientIntensity = task.ambientIntensity
            msg.useGroundPlaneModel = task.useGroundPlaneModel
            msg.groundPositionX = task.groundPlanePositionX
            msg.groundPositionY = task.groundPlanePositionY
            msg.groundPositionZ = task.groundPlanePositionZ
            msg.groundRotationX = task.groundPlaneRotationX
            msg.groundRotationY = task.groundPlaneRotationY
            msg.imageType = job.config.imageType
            msg.generateAllMasks = job.config.generateAllMasks
            TextMessage taskRenderMessageAsTextMessage = JmsUtil.getMessageAsTextMessage(msg)
            def ret = jmsService.send(service: 'task', method: 'runTask', taskRenderMessageAsTextMessage)
        }
    }

    /**
     * Triggered when the TaskConsumer returns the modelRenderMessage after generating the RenderView of an uploaded model.
     * @param modelRenderMessage
     * @return
     */
    @Queue
    def renderModelDone(modelRenderMessageText) {
        ModelRenderMessage modelRenderMessage = (ModelRenderMessage) JmsUtil.fromXML(modelRenderMessageText)
        def modelId = modelRenderMessage.objectModelId
        def model = ObjectModel.get(modelId)
        def bOut = new BlenderOutput()
        bOut.error = modelRenderMessage.error
        bOut.stdout = modelRenderMessage.stdout
        bOut.stderr = modelRenderMessage.stderr
        bOut.exitValue = modelRenderMessage.exitValue
        bOut.outputImageFilename = modelRenderMessage.compositeImageFilename ?: ""
        model.output = bOut

        if (bOut.error || bOut.exitValue != 0 || !modelRenderMessage.compositeKey) {
            //error state, model is invalid
            log.debug "Render model failed for model ${modelId}."
            model.status = ModelStatus.FAILED
            model.imageFilePath = ""
            model.objFilePath = ""
        } else {
            log.debug "Render model succeeded for model ${modelId}."
            model.status = ModelStatus.SUCCESS
            model.imageFilePath = modelRenderMessage.compositeKey
            model.objFilePath = modelRenderMessage.objFileKey
            imageService.generateAndStoreThumbnailFromStoredImage(model.thumbnail.id, modelRenderMessage.compositeKey)
        }

        if (!model.save(flush: true)) {
            log.warn "Saving object model ${model.id} failed:"
            model.errors.each { log.warn it }
        }

        null
    }

    /**
     * Handle the results from a remote taskConsumer worker
     */
    @Queue
    def remoteTaskDone(taskRenderMessageText) {
        try {
            TaskRenderMessage taskRenderMessage = (TaskRenderMessage) JmsUtil.fromXML(taskRenderMessageText)

            // Update job task and save
            JobTask jobTask = JobTask.get(taskRenderMessage.taskId)
            jobTask.completed = true
            jobTask.executionTimeStart = taskRenderMessage.executionTimeStart
            jobTask.executionTimeEnd = taskRenderMessage.executionTimeEnd
            jobTask.taskTimeEnd = new Date().getTime()

            double executionTimeSec = (jobTask.executionTimeEnd - jobTask.executionTimeStart) / 1000

            if (!taskRenderMessage.error.isEmpty()) {
                log.warn "Message returned with error:  ${taskRenderMessage.error}"
                jobTask.error = taskRenderMessage.error
                // Need to add the extra slash for displaying the message in javascript.
                jobTask.stdout = taskRenderMessage.stdout.replace("\n", "\\n")
                jobTask.stderr = taskRenderMessage.stderr.replace("\n", "\\n")
                jobTask.exitValue = taskRenderMessage.exitValue
            } else {
                jobTask.compositeImage.filePath = taskRenderMessage.compositeKey
                if (!jobTask.compositeImage.thumbnail?.valid) {
                    jobTask.compositeImage.thumbnail.filePath =
                        taskRenderMessage.compositeThumbKey
                    jobTask.compositeImage.thumbnail.valid = true
                }
            }

            if (!jobTask.save(flush: true)) {
                log.warn "Saving job task ${jobTask.id} failed:"
                jobTask.errors.each { log.warn it }
            }

            forwardTaskDoneMessageToJobZipper(taskRenderMessageText)

            updateJobWithTaskDone(jobTask.job, executionTimeSec)
        }
        catch (e) {
            e.printStackTrace();
            log.error("problem with job ${taskRenderMessageText}", e);
        }
        null
    }

    /**
     * Tell the zipper that the task is done
     */
    def forwardTaskDoneMessageToJobZipper(taskRenderMessageText) {
        TextMessage taskRenderMessageAsTextMessage = new ActiveMQTextMessage()
        taskRenderMessageAsTextMessage.text = taskRenderMessageText
        jmsService.send(service: 'task', method: 'zipTask', taskRenderMessageAsTextMessage)
    }

    /**
     * Given that a task is done, update the job
     */
    def updateJobWithTaskDone(Job job, double executionTimeSec) {
        job.numComplete++
        job.sumOfExecutionTime += executionTimeSec
        job.sumOfExecutionTimeSquared += executionTimeSec * executionTimeSec

        computeUpdatedCompletionTime(job)

        log.debug("Remote task complete. Execution time: ${executionTimeSec} " +
                " sec.  Avg job execution time: ${job.meanOfExecutionTime}  " +
                "Var: ${job.varianceOfExecutionTime}")

        handleJobPossiblyTasksDone(job)

        saveJob(job)

        def link = getTaskLink(job.id)
        broadcastToBrowser(job.id, "updateTask", link as JSON)
    }

    /**
     * Determine when we think the completed time will be for this job
     */
    def computeUpdatedCompletionTime(Job job) {
        double secPerJob = job.meanOfExecutionTime
        int numJobsToGo = job.numTasks - job.numComplete
        double secToGo = numJobsToGo * secPerJob

        long currentTimeInMS = new Date().getTime()
        long completionTimeInMS = currentTimeInMS + (long) (secToGo * 1000)
        Date completionDate = new Date(completionTimeInMS)

        log.debug("ExpEndDate ${job.expectedEndDate}; jobsToGo ${numJobsToGo};  AvgExecTime ${secPerJob} "
                + " TotalSecToGo: ${secToGo}  NewCompletionTime: ${completionTimeInMS}  AsDate ${completionDate} ")

        job.expectedEndDate = completionDate
    }

    /**
     * Determine if all tasks are done by seeing if the number of completed tasks
     * is the same as the number of tasks created.  If so, send a message to the JobZipper
     */
    def handleJobPossiblyTasksDone(Job job) {
        if (job.numComplete == job.numTasks) {
            log.debug("Job ${job.id} tasks are done. Sending JobDone, starting zip")
            JobMessage jobMessage = new JobMessage();
            jobMessage.bucketName = fileStorageService.getFileStorageConfiguration().bucketName
            jobMessage.jobId = job.id
            jobMessage.jobName = job.jobName
            jobMessage.numTasks = job.numTasks
            jobMessage.zipFilePath = ""
            TextMessage jobMessageAsTextMessage = JmsUtil.getMessageAsTextMessage(jobMessage)
            jmsService.send(service: 'task', method: 'zipTask', jobMessageAsTextMessage)
        }
    }

    /**
     * Receive a message from the Zipper that the zip is loaded and available
     */
    @Queue
    def zipFileDone(jobMessageText) {
        JobMessage jobMessage = (JobMessage) JmsUtil.fromXML(jobMessageText)
        def jobId = jobMessage.jobId
        Job job = Job.get(jobId)
        job.zipFilePath = jobMessage.zipFilePath
        job.status = JobStatus.COMPLETE
        job.actualEndDate = new Date()
        job.zipFileSize = jobMessage.zipFileSize

        saveJob(job)

        def link = getTaskLink(jobId)
        broadcastToBrowser(jobId, "updateTask", link as JSON)
        return null
    }

    /**
     * Get a link to a particular job so atmosphere can update the web page
     */
    def getTaskLink(jobId) {
        [type: "update", link: grailsLinkGenerator.link(controller: 'job', action: 'updateProgress', id: jobId)]
    }

    /**
     * Save the Job object, reporting any errors.
     */
    def saveJob(Job job) {
        if (!job.save(flush: true)) {
            log.warn "Saving job ${job.id} failed:"
            job.errors.each { log.warn it }
        }
    }

    /*
     * ----------------------------------------------------------------
     * Atmosphere stuff below
     * ----------------------------------------------------------------
     */

    void destroy() {
        // TODO Auto-generated method stub
        log.debug("DESTROYED ATMOSPHERE HANDLER")
    }

    void onRequest(AtmosphereResource resource) {
        // We should only have GET requests here
        def req = resource.request
        log.info "onRequest, request: ${req}"
        log.info "onRequest: DEBUG: ${req.path} : ${req.requestURI} : ${req.servletPath}${req.pathInfo}"
        String broadcasterId = "${req.servletPath}${req.pathInfo}"
        def b = BroadcasterFactory.getDefault().lookup(broadcasterId, true)
        b.addAtmosphereResource(resource)
        log.trace "got broadcaster request: ${b}"
    }

    void onStateChange(AtmosphereResourceEvent event) {
        try {
            log.trace("onStateChange: Getting resource")
            def resource = event.resource
            log.trace("onStateChange: Getting response")
            def resp = resource.response
            log.trace("Checking if event is suspended...")
            if (event.isSuspended()) {
                log.trace("onStateChange: Resource was suspended!")
                if (event.message) {

                    log.trace "onStateChange, message: ${event.message}"

                    String mesg = "${event.message}"
                    resp.getWriter().write(mesg)
                } else {
                    log.info("Resource did NOT have a message...")
                }
            } else {
                log.info("onStateChange: Resource was NOT suspended...")
            }
        } catch (Throwable t) {
            log.error("Got a stacktrace: ${t}")
            log.error(t.stackTrace)
        }
    }

    def broadcastToBrowser(jobId, prefix, message) {
        String target = "/atmosphere/task/${prefix}${jobId}"
        def broadcaster = BroadcasterFactory.getDefault()?.lookup(target, true);
        if (broadcaster) {
            log.trace("Broadcasting ${message} to ${target}: ${broadcaster}")
            try {
                if (message instanceof JSON) {
                    broadcaster.broadcast(message.toString())
                } else {
                    String msgString = "{'message' : '${message}'}"
                    broadcaster.broadcast(msgString)
                }
            } catch (Throwable t) {
                t.printStackTrace()
            }
        } else {
            log.warn("Listener was not connected yet, they probably missed this message: ${message}")
        }
    }
}
