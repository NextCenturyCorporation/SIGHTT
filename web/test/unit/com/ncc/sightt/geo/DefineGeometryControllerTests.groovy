
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
package com.ncc.sightt.geo

import com.ncc.sightt.Background
import com.ncc.sightt.Thumbnail
import com.ncc.sightt.auth.Permissions
import com.ncc.sightt.auth.User
import com.ncc.sightt.s3.FileStorageService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.apache.shiro.crypto.SecureRandomNumberGenerator
import org.apache.shiro.crypto.hash.Sha512Hash
import org.codehaus.groovy.grails.web.json.JSONObject

@TestFor(DefineGeometryController)
@Mock([Background, Geometry])
class DefineGeometryControllerTests {

    void testDrawing() {
        createTestBackground(1).save()
        JSONObject obj = saveBackgroundWithGeometry()

        def service = mockFor(FileStorageService)
        service.demand.getImageSrcURI(1..2) { "http://some/image/source/" }
        controller.fileStorageService = service.createMock()

        controller.params.id = 2
        assert controller.drawing().get("id") == 2
        assert controller.drawing().get("geometryJson") == obj
    }

    void testSaveGeometry() {
        givenBackgroundExists()
        withMockServiceAndRequestParamsSet()

        controller.saveGeometry()
        assert response.contentAsString == '{"success":"true"}'
    }

    void testExactlyOneGeometryMayBeSaved() {
        injectMockedService()
        Background background = givenBackgroundExists()
        withMockServiceAndRequestParamsSet()

        assert background.geometry == null

        controller.saveGeometry()
        assert background.geometry.size() == 1

        controller.saveGeometry()
        assert background.geometry.size() == 1
    }

    void testDeleteGeometry() {
        def background = givenBackgroundExists()
        withMockServiceAndRequestParamsSet()

        Geometry geo = createGeometry(new JSONObject("{'test':'payload'}"))
        background.addToGeometry(geo).save(flush: true)

        assert background.geometry.size() == 1
        controller.deleteGeometry()
        assert background.geometry.size() == 0

        assert response.getRedirectedUrl() == "/background/list"
    }

    private Background givenBackgroundExists() {
        Background background = createTestBackground(1)
        background.save(flush: true)
        background
    }

    private void withMockServiceAndRequestParamsSet() {
        request.JSON = "{'test':'payload'}"
        params.id = 1
        injectMockedService()
    }

    private void injectMockedService() {
        def service = mockFor(GeometryJsonService)
        JSONObject obj = new JSONObject("{'test':'payload'}")
        service.demand.createGeometry(1..2) { return createGeometry(obj) }
        controller.geometryJsonService = service.createMock()
    }

    private JSONObject saveBackgroundWithGeometry() {
        JSONObject obj = new JSONObject("{'test':'payload'}")
        Background background = createTestBackground(2)

        Geometry geo = createGeometry(obj)

        background.addToGeometry(geo).save()
        return obj
    }

    private Geometry createGeometry(JSONObject obj) {
        def rectangle = new Rectangle(width: 400, height: 400)
        def origin = new Point(x: 200, y: 200)
        new Geometry(background: new Background(), json: obj.toString(), imageSizeInPixels: rectangle, origin: origin)
    }

    private Background createTestBackground(int index) {
        def passwordSalt = new SecureRandomNumberGenerator().nextBytes().getBytes()

        def owner = new User(username: "tester", passwordHash: new Sha512Hash("tester", passwordSalt, 1024), passwordSalt: passwordSalt)
        new Background([owner: owner, permissions: Permissions.PUBLIC, name: "Test${index}", filePath: "/tmp/test${index}.png", thumbnail: new Thumbnail()])
    }
}
