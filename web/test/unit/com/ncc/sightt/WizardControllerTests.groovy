
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
import com.ncc.sightt.auth.UserAccountsService
import com.ncc.sightt.auth.UserPreferences
import com.ncc.sightt.s3.FileStorageService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.apache.shiro.SecurityUtils
import org.apache.shiro.crypto.SecureRandomNumberGenerator
import org.apache.shiro.crypto.hash.Sha512Hash
import org.apache.shiro.subject.Subject

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(WizardController)
@Mock([Background, BlenderOutput, Job, JobConfig, ObjectModel, Thumbnail, User, UserPreferences])
class WizardControllerTests {

    def defaultOwner


    private def createImageService() {
        def imageService = [getThumbnailSrcList: { x -> [:] }] as ImageService
    }

    private def createFileStorageService() {
        def fileStorageService = [getImageSrcURI: { x ->
            "uri://someuri"
        }] as FileStorageService
    }

    private def createBackgroundMock(number = "1") {
        def background = [owner: defaultOwner, permissions: Permissions.PUBLIC, name: "Name" + number, filePath: "FilePath" + number,
                thumbnail: new Thumbnail(), geometry: []] as Background
    }

    private def createObjectModelMock(number) {

        def model = [owner: defaultOwner, permissions: Permissions.PUBLIC, name: "Name" + number, filePath: "FilePath" + number,
                thumbnail: new Thumbnail(), modelType: ModelType.UNKNOWN,
                status: ModelStatus.SUCCESS, output: [exitValue: 1,
                outputImageFilename: "Output"] as BlenderOutput] as ObjectModel
    }

    private def createJobMock() {
        def job = [owner: defaultOwner, permissions: Permissions.PUBLIC, jobName: "Name", user: "User", jobTasks: [],
                numComplete: 0, numTasks: 0, submitDate: new Date(),
                config: new JobConfig()] as Job
    }

    void setUp() {
        def userAccountService = [isAdmin: { return true }, currentUser: { name ->
            println "Current user: ${name}"
            return defaultOwner
        }] as UserAccountsService
        controller.userAccountsService = userAccountService
        def passwordSalt = new SecureRandomNumberGenerator().nextBytes().getBytes()
        def passwordHash = new Sha512Hash("tester", passwordSalt, 1024).toHex()
        defaultOwner = new User(username: "tester", email: "tester@example.com", passwordHash: passwordHash, passwordSalt: passwordSalt, preferences: new UserPreferences())
        if (!defaultOwner.save()) {
            println "ERRORS HASH: ${passwordHash}"
            defaultOwner.errors.each {
                println it
            }
        }
        def suMetaClass = new ExpandoMetaClass(SecurityUtils)
        suMetaClass.'static'.getSubject = { [getPrincipal: { defaultOwner.username.toString() }, toString: { defaultOwner.username.toString() }] as Subject }
        suMetaClass.initialize()
        SecurityUtils.metaClass = suMetaClass

        controller.session.job = [:]
        controller.session.config = [:]
    }

    void tearDown() {
        SecurityUtils.metaClass = null
    }

    void testIndex() {
        controller.index()
        assert response.redirectedUrl == "/wizard/selectBackground"
    }

    void testSelectBackgroundWithNoBackgrounds() {
        controller.imageService = createImageService()

        def map = controller.selectBackground()
        assert map.backgroundThumbnailSrcMap != null
        assert map.backgroundList == []
        assert map.selectedBackground == null
    }

    void testSelectBackgroundWithOneBackground() {
        controller.imageService = createImageService()
        controller.fileStorageService = createFileStorageService()

        def background1 = createBackgroundMock()
        assert background1.save()
        assert Background.list().size == 1


        def map = controller.selectBackground()
        assert map.backgroundThumbnailSrcMap != null
        assert map.backgroundList == [background1]
        assert map.selectedBackground == null
    }

    void testSelectBackgroundWithMultipleBackgrounds() {
        controller.imageService = createImageService()
        controller.fileStorageService = createFileStorageService()


        def background1 = createBackgroundMock("1")
        def background2 = createBackgroundMock("2")
        assert background1.save()
        assert background2.save()
        assert Background.list().size == 2

        controller.session.config.backgrounds = [background2]

        def map = controller.selectBackground()
        assert map.backgroundThumbnailSrcMap != null
        assert map.backgroundList == [background1, background2]
        assert map.preSelectedBackgrounds == [background2]
    }

    void testFinishSelectBackgroundWithOneBackground() {
        def background1 = createBackgroundMock()
        assert background1.save()
        assert Background.list().size == 1

        controller.params.backgroundIds = "[1]"
        controller.finishSelectBackground()

        assert controller.session.config.backgrounds == [background1]
        assert response.redirectedUrl == "/wizard/selectObject"
    }

    void testFinishSelectBackgroundWithMultipleBackgrounds() {
        def background1 = createBackgroundMock("1")
        def background2 = createBackgroundMock("2")
        assert background1.save()
        assert background2.save()
        assert Background.list().size == 2

        controller.params.backgroundIds = "[2,1]"
        controller.finishSelectBackground()

        assert controller.session.config.backgrounds == [background2, background1]
        assert response.redirectedUrl == "/wizard/selectObject"
    }

    void testSelectObjectWithNoModels() {
        controller.imageService = createImageService()

        def background = createBackgroundMock()
        controller.session.config.backgrounds = [background]

        def map = controller.selectObject()
        assert map.modelThumbnailSrcMap != null
        assert map.modelList == []
        assert map.background == background
        assert map.selectedModel == null
        assert map.selectedScale == 1
    }

    void testSelectObjectWithOneModel() {
        controller.imageService = createImageService()

        def background = createBackgroundMock()
        controller.session.config.backgrounds = [background]

        def model1 = createObjectModelMock()
        assert model1.save()
        assert ObjectModel.list().size == 1

        def map = controller.selectObject()
        assert map.modelThumbnailSrcMap != null
        assert map.modelList == [model1]
        assert map.background == background
        assert map.selectedModel == null
        assert map.selectedScale == 1
    }

    void testSelectObjectWithMultipleModels() {
        controller.imageService = createImageService()

        def background = createBackgroundMock()
        controller.session.config.backgrounds = [background]

        def model1 = createObjectModelMock("1")
        def model2 = createObjectModelMock("2")
        assert model1.save()
        assert model2.save()
        assert ObjectModel.list().size == 2

        controller.session.config.objectModels = [model2]
        controller.session.config.modelBackgroundScale = 2.22

        def map = controller.selectObject()
        assert map.modelThumbnailSrcMap != null
        assert map.modelList == [model1, model2]
        assert map.background == background
        assert map.selectedModel == model2
        assertEquals map.selectedScale, 1.5, 0.01
    }

    void testFinishSelectObjectWithOneModel() {
        def model1 = createObjectModelMock()
        assert model1.save()
        assert ObjectModel.list().size == 1

        controller.params.modelId = 1
        controller.params.scaleData = 1
        controller.finishSelectObject()

        assert controller.session.config.objectModels == [model1]
        assertEquals controller.session.config.modelBackgroundScale, 1.48, 0.01
        assert controller.session.scaleData == 1
        assert response.redirectedUrl == "/wizard/selectLocation"
    }

    void testFinishSelectObjectWithMultipleModels() {
        def model1 = createObjectModelMock("1")
        def model2 = createObjectModelMock("2")
        assert model1.save()
        assert model2.save()
        assert ObjectModel.list().size == 2

        controller.params.modelId = 2
        controller.params.scaleData = 1.5
        controller.finishSelectObject()

        assert controller.session.config.objectModels == [model2]
        assertEquals controller.session.config.modelBackgroundScale, 2.22, 0.01
        assert controller.session.scaleData == 1.5
        assert response.redirectedUrl == "/wizard/selectLocation"
    }

    void testSelectLocation() {
        def background = createBackgroundMock()
        controller.session.config.backgrounds = [background]
        def model = createObjectModelMock()
        controller.session.config.objectModels = [model]
        controller.session.scaleData = 1

        def map = controller.selectLocation()
        assert map.background == background
        assert map.model == model
        assert map.scaleData == 1
        assert map.locationDefault != null
    }

    void testFinishSelectLocation1() {
        controller.params.position = "Centered"
        controller.finishSelectLocation()
        assert controller.session.config.position
        assert response.redirectedUrl == "/wizard/selectNumber"
    }

    void testFinishSelectLocation2() {
        controller.params.position = "Random"
        controller.finishSelectLocation()
        assert controller.session.config.position == ModelLocation.RANDOM
        assert response.redirectedUrl == "/wizard/selectNumber"
    }

    void testSelectNumber() {
        def background = createBackgroundMock()
        controller.session.config.backgrounds = [background]
        def model = createObjectModelMock()
        controller.session.config.objectModels = [model]
        controller.session.scaleData = 1

        def map = controller.selectNumber()
        assert map.background == background
        assert map.model == model
        assert map.scaleData == 1
        assert map.rotationLabels.size() > 0
        assert map.rotationValues.size() > 0
        assert map.rotationLabels.size() == map.rotationValues.size()
        assert map.rotationDefault != null
        assert map.rotationValues.contains(map.rotationDefault)
    }

    void testFinishSelectNumber1() {
        controller.params.genMethod = "rot"
        controller.params.spacing = 60

        controller.finishSelectNumber()

        assert controller.session.config.numImages == 66
        assert controller.session.config.degreeSpacing == 60
        assert response.redirectedUrl == "/wizard/jobSettings"
    }

    void testFinishSelectNumber2() {
        controller.params.genMethod = "rot"
        controller.params.spacing = 360

        controller.finishSelectNumber()

        assert controller.session.config.numImages == 1
        assert controller.session.config.degreeSpacing == 360
        assert response.redirectedUrl == "/wizard/jobSettings"
    }

    void testSummary1() {
        def background = createBackgroundMock()
        controller.session.config.backgrounds = [background]
        def model = createObjectModelMock()
        controller.session.config.objectModels = [model]
        controller.session.config.numImages = 5
        controller.session.config.position = ModelLocation.CENTERED
        controller.session.config.degreeSpacing = 10
        controller.session.config.reproducible = true
        controller.session.config.imageType = "PNG"
        controller.session.config.generateAllMasks = true
        controller.session.job.jobName = "Job Name"

        def map = controller.summary()
        assert map.backgrounds == [background]
        assert map.model == model
        assert map.numImages == 5
        assert map.position == ModelLocation.CENTERED
        assert map.degreeSpacing == 10
        assert map.orientationType == "Uniform"
        assert map.imageType == "PNG"
        assert map.generateMasksString != null
        assert map.jobName == "Job Name"
    }

    void testSummary2() {
        def background = createBackgroundMock()
        controller.session.config.backgrounds = [background]
        def model = createObjectModelMock()
        controller.session.config.objectModels = [model]
        controller.session.config.numImages = 10
        controller.session.config.centered = false
        controller.session.config.degreeSpacing = 5
        controller.session.config.reproducible = false
        controller.session.config.imageType = "JPG"
        controller.session.config.generateAllMasks = false
        controller.session.job.jobName = "Other Job Name"

        def map = controller.summary()
        assert map.backgrounds == [background]
        assert map.model == model
        assert map.numImages == 10
        assert !map.centered
        assert map.degreeSpacing == 5
        assert map.orientationType == "Random"
        assert map.imageType == "JPG"
        assert map.generateMasksString != null
        assert map.jobName == "Other Job Name"
    }

    void testStartJob() {
        controller.startJob()
        assert response.forwardedUrl.startsWith("/grails/job/save.dispatch?job=")
    }

    void testCloneJob() {
        flash.jobInstance = createJobMock()
        controller.cloneJob()
        assert controller.session.job != null
        assert controller.session.config != null
        assert response.redirectedUrl == "/wizard/selectBackground"
    }
}
