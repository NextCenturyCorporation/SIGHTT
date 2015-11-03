
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
package com.ncc.sightt.s3

import org.junit.Assert
import org.junit.Before
import org.junit.Test

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
class LocalFileStorageServiceTests {

    LocalFileStorageService fss
    def imageFile
    def bufImg

    public static final String bucketName = "sightt-test"
    public static final String objectFilepath = "model/AK47.blend"
    public static final String imageFilepath = "test-do-not-delete/1.png"

    @Before
    void setUp() {
        fss = new LocalFileStorageService()
        def fsc = new FileStorageConfiguration()
        fsc.bucketName = "sightt-test"
        fsc.defaultImageType = "png"
        fsc.backgroundPrefix = "back/"
        fsc.modelPrefix = "model/"
        fsc.thumbnailPrefix = "thumb/"
        fsc.compositedPrefix = "comp/"
        fsc.renderPrefix = "render/"
        fss.fileStorageConfiguration = fsc

        def mainImg = "src/test/resources/office_small.jpg"
        bufImg = ImageIO.read(new File(mainImg))
    }

    @Test
    void testUniqueURI() {
        Assert.assertFalse(fss.getUniqueFileURI("test") == fss.getUniqueFileURI("test"))
    }

    @Test
    void testUploadFileToLocalFileStore() {
        String path = fss.saveBackgroundImage(bufImg)
        println "Path is: ${path}"

        boolean exists = fss.checkIfFileExists(path)
        assert exists

        BufferedImage bi = fss.getImage(path)
        Assert.assertTrue("Buffered image is not correct ", bi.width > 0)

        File f = fss.loadFileFromStore(path)
        if (!f || !f.exists()) {
            fail("file does not exit at ${path}")
        }
    }

    @Test
    void testGetTempFile() {
        def file
        assert (!file)
        file = fss.getTempFile()
        assert (file)
    }

}
