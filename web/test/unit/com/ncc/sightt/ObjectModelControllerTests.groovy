
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
import com.ncc.sightt.s3.S3StorageService
import grails.plugin.jms.JmsService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.apache.shiro.SecurityUtils
import org.apache.shiro.crypto.SecureRandomNumberGenerator
import org.apache.shiro.crypto.hash.Sha512Hash
import org.apache.shiro.subject.Subject

@TestFor(ObjectModelController)
@Mock([ObjectModel, Thumbnail, RenderedView, User, UserPreferences])
class ObjectModelControllerTests {

    def defaultOwner

    void setUp() {
        def userAccountService = [isAdmin: { return true }, currentUser: { name ->
            println "Current user: ${name}"
            return defaultOwner
        }] as UserAccountsService
        controller.userAccountsService = userAccountService

        def fileStorageService = [getImageSrcURI: { String s -> return "URI://PLACE" }] as S3StorageService
        controller.fileStorageService = fileStorageService

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

    }

    void tearDown() {
        SecurityUtils.metaClass = null
    }

    def createValidObjectModel(params) {
        populateValidParams(params)
        params.owner = defaultOwner
        params.permissions = Permissions.PRIVATE
        def model = new ObjectModel(params)
    }

    def populateValidParams(params) {
        assert params != null
        params.bucketName = "sightt-test"
        params.name = "3D Object Model"
        params.filePath = 'tmp/validPath'
        params.imageFilePath = 'tmp/anotherPath'
        params.objFilePath = 'tmp/objfilePath'
        params.thumbnail = new Thumbnail()
        params.modelType = ModelType.OBJ
        params.sizeInMeters = 1.2
        params.renderHeight = 600
        params.renderWidth = 800
        params.status = ModelStatus.PENDING
    }

    def getValidObjParams(params) {
        assert params != null
        params.name = "Test"
        params.filePath = "/tmp/test.png"
        params.thumbnail = new Thumbnail()
    }

    void testIndex() {
        controller.index()
        assert "/objectModel/list" == response.redirectedUrl
    }

    def mockImageServiceForList() {
        def service = mockFor(ImageService)
        service.demand.getThumbnailSrcList(1..2) {}
        service.createMock()
    }

    void testListEmpty() {

        controller.imageService = mockImageServiceForList()
        def model = controller.list()

        assert model.objectModelInstanceList.size() == 0
        assert model.objectModelInstanceTotal == 0
    }

    void testListPendingAsAdmin() {
        controller.imageService = mockImageServiceForList()
        def objectModel = createValidObjectModel(params)
        assert objectModel.save() != null

        def model = controller.list()
        //model is still pending
        assert model.objectModelInstanceList.size() == 1
        assert model.objectModelInstanceTotal == 1
    }

    void testListFailedAsAdmin() {
        controller.imageService = mockImageServiceForList()
        def objectModel = createValidObjectModel(params)
        assert objectModel.save() != null

        objectModel.status = ModelStatus.FAILED
        objectModel.save()
        def model = controller.list()
        //model failed
        assert model.objectModelInstanceList.size() == 1
        assert model.objectModelInstanceTotal == 1
    }

    void testListSuccess() {
        controller.imageService = mockImageServiceForList()
        def objectModel = createValidObjectModel(params)
        assert objectModel.save() != null

        objectModel.status = ModelStatus.SUCCESS
        objectModel.save()
        def model = controller.list()
        //model failed
        assert model.objectModelInstanceList.size() == 1
        assert model.objectModelInstanceTotal == 1
    }

    void testCreate() {
        def model = controller.create()

        assert model.objectModelInstance != null
    }

    void testUpload() {
        def service = mockFor(FileStorageService)
        service.demand.storeUploadedModel(1) { modelFile -> "testUpload" }
        controller.fileStorageService = service.createMock()
        controller.upload()
        assert controller.session.filePath == "testUpload"
    }

    void testSaveInvalid() {
        def service = mockFor(FileStorageService)
        service.demand.getFileStorageConfiguration(1..2) {-> return [bucketName: "sightt-test"] }
        controller.fileStorageService = service.createMock()
        controller.session.filePath = null
        def suMetaClass = new ExpandoMetaClass(SecurityUtils)
        suMetaClass.'static'.getSubject = { [getPrincipal: { 2 }, toString: { "testuser" }] as Subject }
        suMetaClass.initialize()
        SecurityUtils.metaClass = suMetaClass
        controller.save()
        SecurityUtils.metaClass = null
        assert model.objectModelInstance != null
        assert view == '/objectModel/create'
    }

    void testSaveValid() {
        populateValidParams(params)

        def service = mockFor(FileStorageService)
        service.demand.getFileStorageConfiguration(1..2) {-> return [bucketName: "sightt-test"] }
        controller.fileStorageService = service.createMock()
        def sendCalled = false
        def jService = mockFor(JmsService)
        jService.demand.send(1) { map, msg ->
            sendCalled = true
            null
        }
        controller.jmsService = jService.createMock()
        assert params.filePath
        controller.session.filePath = params.filePath
        def model = controller.save()
        assert sendCalled
        assert response.redirectedUrl == '/objectModel/validate/1'
        assert controller.flash.message != null
        assert ObjectModel.count() == 1
    }

    void testShow() {

        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/objectModel/list'

        def objectModel = createValidObjectModel(params)
        getValidObjParams(params)

        assert objectModel.save() != null

        params.id = objectModel.id

        def model = controller.show()

        assert model.objectModelInstance == objectModel
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/objectModel/list'

        def objectModel = createValidObjectModel(params)

        assert objectModel.save() != null

        params.id = objectModel.id

        def model = controller.edit()

        assert model.objectModelInstance == objectModel
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/objectModel/list'

        response.reset()

        def objectModel = createValidObjectModel(params)

        assert objectModel.save() != null

        // test invalid parameters in update
        params.id = objectModel.id
        //add invalid values to params object
        params.filePath = null

        controller.update()

        assert view == "/objectModel/edit"
        assert model.objectModelInstance != null

        objectModel.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/objectModel/show/$objectModel.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        objectModel.clearErrors()

        populateValidParams(params)
        params.id = objectModel.id
        params.version = -1
        controller.update()

        assert view == "/objectModel/edit"
        assert model.objectModelInstance != null
        assert model.objectModelInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/objectModel/list'

        response.reset()

        def objectModel = createValidObjectModel(params)

        assert objectModel.save() != null
        assert ObjectModel.count() == 1

        params.id = objectModel.id

        controller.delete()

        assert ObjectModel.count() == 0
        assert ObjectModel.get(objectModel.id) == null
        assert response.redirectedUrl == '/objectModel/list'
    }

    void testFullImgDisplayLink() {
        def objectModel = createValidObjectModel(params)
        objectModel.save()
        assert objectModel.validate()
        assert ObjectModel.count > 0
        controller.fullImgDisplayLink(1)
        assert response.text == "http://localhost:8080/imageDisplay/display?filePath=tmp%2FanotherPath"
    }

    void testFullImgDisplaySrc() {
        def fs = mockFor(FileStorageService)
        fs.demand.getImageSrcURI(1) { path -> return path }
        controller.fileStorageService = fs.createMock()
        def objectModel = createValidObjectModel(params)
        objectModel.save()
        assert objectModel.validate()
        assert ObjectModel.count > 0
        controller.fullImgDisplaySrc(1)
        assert response.text == "tmp/anotherPath"
    }

    void testGetModelInfoNotReady() {
        def objectModel = createValidObjectModel(params)
        objectModel.save()
        assert objectModel.validate()
        assert ObjectModel.count > 0
        controller.getModelInfo(1)
        assert response.text == "NOTREADY"
    }
}
