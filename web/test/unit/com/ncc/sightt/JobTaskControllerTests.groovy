
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
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(JobTaskController)
@Mock(JobTask)
class JobTaskControllerTests {

    def populateValidParams(params) {
        assert params != null
        params.startTime = new Date()
        params.completedTime = new Date()
        params.completed = false
        params.background = new Background(name: "test", filePath: "/tmp/test", thumbnail: new Thumbnail())
        params.objectModel = new ObjectModel(name: "t1", bucketName: "bucket", filePath: "/tmp/blah", thumbnail: new Thumbnail(), modelType: ModelType.BLENDER,
                sizeInMeters: 3.2, renderHeight: 600, renderWidth: 800, renderViews: null)
        params.compositeImage = new CompositeImage(name: "test2", filePath: "/tmp/test2", thumbnail: new Thumbnail(), valid: false)
        params.job = new Job(user: "test", submitDate: new Date(), expectedEndDate: new Date(), actualEndDate: new Date(), numImages: 2)
        params.locationX = 213
        params.locationY = 234
        params.yaw = 0.2
        params.pitch = 0.1
        params.roll = -0.12
        params.scaleFactor = 2.5
        params.imageType = ImageType.PNG
    }

    void testIndex() {
        controller.index()
        assert "/jobTask/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.jobTaskInstanceList.size() == 0
        assert model.jobTaskInstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.jobTaskInstance != null
    }

    void testSave() {
        controller.save()

        assert model.jobTaskInstance != null
        assert view == '/jobTask/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == "/jobTask/show/1"
        assert controller.flash.get("message")
        assert JobTask.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/jobTask/list'

        populateValidParams(params)
        def jobTask = new JobTask(params)

        assert jobTask.save() != null

        params.id = jobTask.id

        def model = controller.show()

        assert model.jobTaskInstance == jobTask
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/jobTask/list'

        populateValidParams(params)
        def jobTask = new JobTask(params)

        assert jobTask.save() != null

        params.id = jobTask.id

        def model = controller.edit()

        assert model.jobTaskInstance == jobTask
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/jobTask/list'

        response.reset()

        populateValidParams(params)
        def jobTask = new JobTask(params)

        assert jobTask.save() != null

    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/jobTask/list'

        response.reset()

        populateValidParams(params)
        def jobTask = new JobTask(params)

        assert jobTask.save() != null
        assert JobTask.count() == 1

        params.id = jobTask.id

        controller.delete()

        assert JobTask.count() == 0
        assert JobTask.get(jobTask.id) == null
        assert response.redirectedUrl == '/jobTask/list'
    }
}
