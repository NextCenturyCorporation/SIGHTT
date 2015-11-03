
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

import java.awt.image.BufferedImage

import javax.imageio.ImageIO

import org.junit.*
import org.junit.Assert.*


/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
class S3StorageServiceTests {

// NOTE:  Tests removed prior to putting on github

//    S3StorageService fss
//    def imageFile
//    def bufImg
//
//    public static final String bucketName = "sightt-test"
//    public static final String objectFilepath = "model/AK47.blend"
//    public static final String imageFilepath = "test-do-not-delete/1.png"
//
//    @Before
//    void setUp() {
//        fss = new S3StorageService()
//        def fsc = new FileStorageConfiguration()
//        fsc.bucketName = "sightt-test"
//        fsc.defaultImageType = "png"
//        fsc.backgroundPrefix = "back/"
//        fsc.modelPrefix = "model/"
//        fsc.thumbnailPrefix = "thumb/"
//        fsc.compositedPrefix = "comp/"
//        fsc.renderPrefix = "render/"
//        fss.fileStorageConfiguration = fsc
//
//        def mainImg = "src/test/resources/office_small.jpg"
//        imageFile = new File(mainImg)
//        bufImg = ImageIO.read(imageFile)
//    }
//
//    @Test
//    def void testPutImage() {
//        def filepath = fss.saveBackgroundImage(bufImg)
//        println "Image saved to ${filepath}"
//        Assert.assertTrue("File not saved to backround ", fss.checkIfFileExists(filepath))
//    }
//
//    @Test
//    def void testGetImage() {
//        BufferedImage bi = fss.getImage(imageFilepath)
//        Assert.assertTrue("Buffered image is not correct ", bi.width > 0)
//    }
//
//    @Test
//    void testUniqueURI() {
//        Assert.assertFalse(fss.getUniqueFileURI("test") == fss.getUniqueFileURI("test"))
//    }
//
//    @Test
//    void testUploadFileToS3() {
//        fss = new S3StorageService()
//    }
//
//    @Test
//    void testGetTempFile() {
//        def file
//        assert (!file)
//        file = fss.getTempFile()
//        assert (file)
//    }
//
//    @Test
//    void testWhatHappensWhenYouAddNullToAString() {
//        String nullString = null
//        String astring = "blah"
//        String together = astring + ("" + nullString)
//        println "Together they are : |${together}|"
//    }
}
