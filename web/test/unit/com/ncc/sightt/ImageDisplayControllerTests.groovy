
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

import com.ncc.sightt.s3.FileStorageService
import grails.test.mixin.TestFor
import org.junit.Assert

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(ImageDisplayController)
class ImageDisplayControllerTests {

    def final FAKE_URL = "http://foo.bar"

    def getValidParams() {
        [filePath: "/foo/bar/baz"]
    }

    void testIndex() {
        controller.index()
        assert "/main" == response.redirectedUrl
    }

    void testDisplayRaw() {
        def service = mockFor(FileStorageService)
        service.demand.getImageSrcURI(1) { fpath -> return "${FAKE_URL}${fpath}" }
        controller.fileStorageService = service.createMock()
        def params = getValidParams()
        controller.displayRaw(params)
        String correctString = "${FAKE_URL}${params['filePath']}"
        String responseText = response.text
        Assert.assertEquals(correctString, responseText)
    }

    void testDisplay() {
        def service = mockFor(FileStorageService)
        service.demand.getImageSrcURI(1) { fpath -> return "${FAKE_URL}${fpath}" }
        controller.fileStorageService = service.createMock()
        def params = getValidParams()
        def model = controller.display(params)
        Assert.assertEquals("${FAKE_URL}${params['filePath']}", model.srcStr)
    }

    void testGet() {
        def service = mockFor(FileStorageService)
        service.demand.getImageSrcURI(1) { fpath -> return "${FAKE_URL}${fpath}" }
        controller.fileStorageService = service.createMock()
        def params = getValidParams()
        def model = controller.display(params)
        Assert.assertEquals("${FAKE_URL}${params['filePath']}", model.srcStr)
    }
}
