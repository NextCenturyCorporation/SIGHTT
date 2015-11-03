
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
import com.ncc.sightt.s3.FileStorageConfiguration
import com.ncc.sightt.s3.FileStorageService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.apache.shiro.crypto.SecureRandomNumberGenerator
import org.apache.shiro.crypto.hash.Sha512Hash

@TestFor(BackgroundController)
@Mock([Background, Thumbnail])
class BackgroundControllerTests {

    void setUp() {
        def userAccountService = [isAdmin: { return true }] as UserAccountsService
        controller.userAccountsService = userAccountService
    }

    def createValidBackground(params) {
        populateValidParams(params)
        def passwordSalt = new SecureRandomNumberGenerator().nextBytes().getBytes()

        def owner = new User(username: "tester", passwordHash: new Sha512Hash("tester", passwordSalt, 1024), passwordSalt: passwordSalt)
        params.owner = owner
        params.permissions = Permissions.PRIVATE
        def background = new Background(params)
    }

    def getValidConfiguration() {
        def fsc = new FileStorageConfiguration()

        fsc.defaultImageType = 'png'
        fsc.backgroundPrefix = 'back'
        fsc.objectPrefix = 'obj'
        fsc.thumbnailPrefix = 'thumb'
        fsc.compositedPrefix = 'comp'
        fsc.renderPrefix = 'render'
        fsc
    }

    def getValidResourceMap() {
        def resMap = [:]
        resMap['filePath'] = "/tmp/test.png"
        resMap['width'] = 800
        resMap['height'] = 600
        resMap
    }

    def populateValidParams(params) {
        assert params != null
        params.name = "test"
        params.bucketName = ""
        params.filePath = "/tmp/sightt"
        params.thumbnail = new Thumbnail()
        params.height = 800
        params.width = 600
    }

    void testIndex() {
        controller.index()
        assert "/background/list" == response.redirectedUrl
    }

    void testList() {
        def service = mockFor(ImageService)
        service.demand.getThumbnailSrcList(1) {}
        controller.imageService = service.createMock()
        def model = controller.list()

        assert model.backgroundInstanceList.size() == 0
        assert model.backgroundInstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.backgroundInstance != null
    }

    // FIXME:  For some reason, this is continuing to fail, setting
    // the ImageIO.read result to null, even though it should be returning
    // a new bufferedimage
//    void testSave() {
//        ImageIO.metaClass.read = { file ->
//            return new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB_)
//        }
//
//        def fileService = [storeFileLocally: { return new File("/tmp/blah.txt") },
//                storeUploadedBackground: { getValidResourceMap() }] as FileStorageService
//
//        controller.fileStorageService = fileService
//
//        def suMetaClass = new ExpandoMetaClass(SecurityUtils)
//        suMetaClass.'static'.getSubject = { [getPrincipal: { 2 }, toString: { "testuser" }] as Subject }
//        suMetaClass.initialize()
//        SecurityUtils.metaClass = suMetaClass
//
//        request.method = "POST"
//
//        MockMultipartFile mockMultipartFile = new MockMultipartFile("backgroundFile", "Hello World".getBytes())
//        controller.request.addFile(mockMultipartFile)
//        populateValidParams(params)
//        controller.save()
//
//        assert response.redirectedUrl == "/background/show/1"
//        SecurityUtils.metaClass = null
//        ImageIO.metaClass = null
//        BufferedImage.metaClass = null
//    }

    void testShow() {
        def service = mockFor(FileStorageService)
        service.demand.getImageSrcURI(1) {}
        controller.fileStorageService = service.createMock()
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/background/list'

        def background = createValidBackground(params)

        assert background.save() != null

        params.id = background.id

        def model = controller.show()

        assert model.backgroundInstance == background
    }

    void testEdit() {
        def service = mockFor(FileStorageService)
        service.demand.getImageSrcURI(1) {}
        controller.fileStorageService = service.createMock()
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/background/list'

        def background = createValidBackground(params)

        assert background.save() != null

        params.id = background.id

        def model = controller.edit()

        assert model.backgroundInstance == background
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/background/list'

        response.reset()

        def background = createValidBackground(params)

        assert background.save() != null

        // test invalid parameters in update
        params.id = background.id
        //TODO: add invalid values to params object

        controller.update()

        background.clearErrors()

        populateValidParams(params)

        assert response.redirectedUrl == "/background/show/$background.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        background.clearErrors()

        populateValidParams(params)
        params.id = background.id
        params.version = -1
        controller.update()

        assert view == "/background/edit"
        assert model.backgroundInstance != null
        assert model.backgroundInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/background/list'

        response.reset()

        def background = createValidBackground(params)
        assert background.save() != null
        assert Background.count() == 1

        params.id = background.id

        controller.delete()

        assert Background.count() == 0
        assert Background.get(background.id) == null
        assert response.redirectedUrl == '/background/list'
    }
}
