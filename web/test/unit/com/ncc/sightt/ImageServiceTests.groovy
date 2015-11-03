
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

import java.awt.image.BufferedImage

import static org.junit.Assert.assertEquals

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(ImageService)
@Mock([Thumbnail])
class ImageServiceTests {

    def final DEFAULT_PATH = "thumb/foobarbaz"
    def final DEFAULT_URI = "http://foo.bar/"

    void setUp() {
        // Setup logic here
    }

    void tearDown() {
        // Tear down logic here
    }

    void testGenerateAndStoreThumbnailFromBufferedImage() {
        def thumb = new Thumbnail()
        thumb.save()
        assert (thumb.validate())
        def fss = mockFor(FileStorageService)
        fss.demand.saveThumbnailImage(1) { bufImg -> return DEFAULT_PATH }
        service.fileStorageService = fss.createMock()
        BufferedImage buf = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB)
        def thumbId = service.generateAndStoreThumbnailFromBufferedImage(1, buf)
        assertEquals(1, thumbId)
    }

    void testGetThumbnailImageSrc() {
        def thumb = new Thumbnail(filePath: DEFAULT_PATH)
        thumb.save()
        assert (thumb.validate())
        def fss = mockFor(FileStorageService)
        fss.demand.getImageSrcURI(1) { fp -> return "${DEFAULT_URI}${fp}" }
        service.fileStorageService = fss.createMock()
        assertEquals("${DEFAULT_URI}${DEFAULT_PATH}", service.getThumbnailImageSrc(1))
    }

    void testGetThumbnailSrcListEmpty() {
        def fss = mockFor(FileStorageService)
        fss.demand.getImageSrcURI(1) { fp -> return "${DEFAULT_URI}${fp}" }
        service.fileStorageService = fss.createMock()

        def thumbList = service.getThumbnailSrcList([])
        assertEquals([:], thumbList)
    }

    void testGetThumbnailSrcListSingle() {
        def thumb1 = new Thumbnail(filePath: "thumb1")
        assert (thumb1.validate())
        thumb1.save()
        def img1 = [id: 1, thumbnail: thumb1]

        def fss = mockFor(FileStorageService)
        fss.demand.getImageSrcURI(1) { fp -> return "${DEFAULT_URI}${fp}" }
        service.fileStorageService = fss.createMock()

        List oneImage = [img1]

        def thumbList = service.getThumbnailSrcList(oneImage)
        assertEquals("${DEFAULT_URI}${thumb1.filePath}", thumbList[img1.id])
    }

    void testGetThumbnailSrcListMulti() {
        def thumb1 = new Thumbnail(filePath: "thumb1")
        def thumb2 = new Thumbnail(filePath: "thumb2")
        def thumb3 = new Thumbnail(filePath: "thumb3")
        def thumb4 = new Thumbnail(filePath: "thumb4")
        assert (thumb1.validate())
        assert (thumb2.validate())
        assert (thumb3.validate())
        assert (thumb4.validate())
        thumb1.save()
        thumb2.save()
        thumb3.save()
        thumb4.save()
        def img1 = [id: 1, thumbnail: thumb1]
        def img2 = [id: 2, thumbnail: thumb2]
        def img3 = [id: 3, thumbnail: thumb3]
        def img4 = [id: 4, thumbnail: thumb4]

        def fss = mockFor(FileStorageService)
        fss.demand.getImageSrcURI(4) { fp -> return "${DEFAULT_URI}${fp}" }
        service.fileStorageService = fss.createMock()

        List oneImage = [img1, img2, img3, img4]

        def thumbList = service.getThumbnailSrcList(oneImage)
        assertEquals("${DEFAULT_URI}${thumb1.filePath}", thumbList[img1.id])
        assertEquals("${DEFAULT_URI}${thumb2.filePath}", thumbList[img2.id])
        assertEquals("${DEFAULT_URI}${thumb3.filePath}", thumbList[img3.id])
        assertEquals("${DEFAULT_URI}${thumb4.filePath}", thumbList[img4.id])

    }

    void testGenerateAndStoreThumbnailFromStoredImage() {
        def thumb = new Thumbnail()
        thumb.save()
        assert (thumb.validate())
        def fss = mockFor(FileStorageService)
        fss.demand.getImage(1) { fp -> return new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB) }
        fss.demand.saveThumbnailImage(1) { bufImg -> return DEFAULT_PATH }
        service.fileStorageService = fss.createMock()
        def thumbId = service.generateAndStoreThumbnailFromStoredImage(1, DEFAULT_PATH)
        assertEquals(1, thumbId)
    }

//    void testGetImageScaledFile() {
//      
//    }
//    
//    void testGetImageScaledBuffered() {
//    
//  }

}
