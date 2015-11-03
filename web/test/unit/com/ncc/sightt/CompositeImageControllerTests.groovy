
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
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(CompositeImageController)
@Mock([Job, Thumbnail])

class CompositeImageControllerTests {

    void testGetFullImageSrc() {
        CompositeImage.metaClass.'static'.get = { Long num -> new CompositeImage(name: "test3", filePath: "/tmp/test3", thumbnail: new Thumbnail(), valid: false) }
        def service = mockFor(FileStorageService)
        service.demand.getFullImageSrc(1..1) { "fullImageSrc" }
        controller.fileStorageService = service.createMock()

        assert controller.getFullImageSrc(0)?.imgSrc == "fullImageSrc"
    }

    void testGenerateThumb() {
        CompositeImage.metaClass.'static'.get = { Long num -> new CompositeImage(name: "test3", filePath: "/tmp/test3", thumbnail: new Thumbnail(), valid: false) }
        def service = mockFor(ImageService)
        service.demand.generateAndStoreThumbnailFromStoredImage(1..1) {}
        controller.imageService = service.createMock()

        controller.generateThumb(0)
    }

    private void injectMockedService() {
        def imgService = mockFor(ImageService)
        service.demand.generateAndStoreThumbnailFromStoredImage(1..1) {}
        imgService.demand.getThumbnailFile(1..1) {}
        controller.imageService = imgService.createMock()

        def service = mockFor(FileStorageService)
        service.demand.deleteTempFile(1..1) {}

        controller.fileStorageService = service.createMock()
    }
}
