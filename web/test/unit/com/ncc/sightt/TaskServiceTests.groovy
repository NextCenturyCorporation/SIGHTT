
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

import com.ncc.sightt.message.ImageType
import com.ncc.sightt.message.TaskRenderMessage
import com.ncc.sightt.s3.FileStorageService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

/**
 * Tests for task service
 */
@TestFor(TaskService)
@Mock([Thumbnail, Job, Background, JobTask, CompositeImage, ObjectModel])

class TaskServiceTests {

    def fileStorageService

    void testModifyRandomly() {
        service.random = new Random(new Date().getTime())
        PointsOnSphere pos = new PointsOnSphere()
        def results = pos.getThetaPhiRotationPoints(12)
        results.points.each { i -> println i }
        println ""
        service.modifyRandomly(results)
        results.points.each { i -> println i }
    }

    void testVaryAngle() {
        service.random = new Random(new Date().getTime())
        final int NUM_TEST = 100000
        final double MAX = Math.PI
        final double VAR = Math.PI / 6

        println("Max according to Java/Groovy: " + MAX)

        Binner b = new Binner();
        for (int ii = 0; ii < NUM_TEST; ii++) {
            double x = Math.PI / 5

            double val = service.varyAngle(x, VAR, -MAX, MAX)
            b.addData(val)
        }
        b.bin(40)
        b.printBins();
    }

    void testVaryAngle2() {
        service.random = new Random(new Date().getTime())
        final int NUM_TEST = 100000
        final double MAX = Math.PI
        final double VAR = Math.PI / 6

        println("Max according to Java/Groovy: " + MAX)

        Binner b = new Binner();
        for (int ii = 0; ii < NUM_TEST; ii++) {
            double x = 4 * Math.PI / 5

            double val = service.varyAngle(x, VAR, -MAX, MAX)
            b.addData(val)
        }
        b.bin(40)
        b.printBins();
    }

    def generateTaskResult() {
        def taskResult = new TaskRenderMessage()
        taskResult.bucketName = "sightt-test"
        taskResult.jobId = 1
        taskResult.taskId = 1
        taskResult.backgroundKey = "blah"
        taskResult.modelKey = "blah"
        taskResult.compositeKey = "blah"
        taskResult.compositeThumbKey = "blah"
        taskResult.scaleFactor = 1
        taskResult.pointX = 123
        taskResult.pointY = 234
        taskResult.yaw = 0
        taskResult.pitch = 1
        taskResult.roll = 0.4
        taskResult.imageType = ImageType.PNG
        taskResult
    }

    void setUp() {
        def background = new Background(bucketName: "sightt-test", name: "blah", filePath: "blah", thumbnail: new Thumbnail(), height: 1, width: 1)
        def objModel = new ObjectModel(bucketName: "sightt-test", name: "blah", filePath: "blah", thumbnail: new Thumbnail(),
                modelType: ModelType.BLENDER, renderHeight: 1, renderWidth: 1)

        def job = new Job(jobName: "blah", user: "sightt", backgrounds: [background], objectModels: [objModel],
                jobTasks: null, numImages: 1, numComplete: 0, numTasks: 1, submitDate: new Date())

        def thumbnail = new Thumbnail()
        thumbnail.save()
        def composite = new CompositeImage(bucketName: "sightt-test",
                name: "PLACEHOLDER", filePath: "NA", thumbnail: thumbnail, valid: false)
        def task = new JobTask(background: background, objectModel: objModel, yaw: 0, pitch: 0, roll: 0, scaleFactor: 1.0,
                startTime: new Date(), completedTime: new Date(), completed: false,
                compositeImage: composite, centered: true, job: job)
        if (!task.validate()) {
            task.errors.allErrors.each {
                log.info(it.toString())
            }
        } else {
            task.save()
        }
        job.addToJobTasks(task)
        Job j2 = job.save()
        println("Number of jobs: " + Job.all.size())
        println("${j2}")
        println("${job}")
        fileStorageService = [getFileStorageConfiguration: [bucketName: 'sightt-test']] as FileStorageService
    }
}
