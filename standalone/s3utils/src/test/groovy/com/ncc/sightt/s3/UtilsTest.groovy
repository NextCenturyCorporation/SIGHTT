
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
package com.ncc.sightt.s3;

import static org.junit.Assert.*
import groovy.util.logging.Log4j

import java.awt.image.BufferedImage

import javax.imageio.ImageIO

import org.apache.commons.lang3.RandomStringUtils
import org.junit.Assert
import org.junit.Test

@Log4j
class UtilsTest {

    static String backgroundFile = "src/test/resources/office_small.jpg"


    @Test
    public void testGetSuffix() {
        String f = "afsasf/asfds/asfasf/asfdsdf.png"
        String suffix = Utils.getFilePathSuffix(f)
        Assert.assertEquals("Wrong suffix", "png", suffix)

        def str = "/tmp/blah.val.xyz"
        String suf = Utils.getFilePathSuffix(str)
        Assert.assertEquals("Wrong suffix for ${str}", "xyz", suf)

        def s = "/home/blah.zyw/filename"
        String suff = Utils.getFilePathSuffix(s)
        Assert.assertNull("Suffix of ${s} should be null but is ${suff}", suff)
    }


    @Test
    public void findFirstMatchingFileTest() {
        String dirname = "/tmp/"
        String prefix = "blah_" + RandomStringUtils.random(16, true, true) + "_"

        File f = File.createTempFile(prefix, ".png")
        f.deleteOnExit()
        File resultFile = Utils.findMatchingFile(dirname, prefix)

        if (resultFile == null || !resultFile.exists()) {
            Assert.fail("Could not find file ${f}. Resulfile: ${resultFile}")
        }
    }


    @Test
    public void utilsTestSmallX() {
        def x = 0.0001
        def deg = Utils.convertRadiansToDegrees(x)
        Assert.assertEquals("Incorrect conversion of ${x}", 0.005729, deg, 0.0001)
    }

    /** Pi/2 is 90 degrees */
    @Test
    public void utilsTestLargeX() {
        def x = Math.PI / 2
        def deg = Utils.convertRadiansToDegrees(x)
        Assert.assertEquals("Incorrect conversion of ${x}", 90, deg, 0.0001)
    }

    @Test
    public void utilsTestLargeXToRadians() {
        def x = 180
        def rad = Utils.convertDegreesToRadians(x)
        Assert.assertEquals("Incorrect conversion of ${x}", Math.PI, rad, 0.0001)
    }

    @Test
    public void utilsTestToString() {
        def x = 0.321 // this is 18.391945
        def str = Utils.getClosestIntDegreesForRadians(x)
        Assert.assertTrue("Incorrect conversion to string of ${x}", str.equals("18"))
    }

    @Test
    public void utilsTestToStringNegative() {
        def x = -1.53299 // this is 87.833
        def str = Utils.getClosestIntDegreesForRadians(x)
        Assert.assertTrue("Incorrect conversion to string of ${x}", str.equals("-88"))
    }

    @Test
    public void testFilepath() {
        String expectedfilename = "filename.png"
        String fullFilepath = "/tmp/blah/anotherdir/" + expectedfilename
        String filename = Utils.getFilenameFromFullFilepath(fullFilepath)
        Assert.assertEquals("Filename is incorrect", expectedfilename, filename)
    }

    @Test
    public void testFilepath2() {
        String fullFilepath = "/tmp/blah/anotherdir/"
        String filename = Utils.getFilenameFromFullFilepath(fullFilepath)
        Assert.assertEquals("Filename is incorrect", fullFilepath, filename)
    }

    @Test
    public void testFilepath3() {
        String expectedfilename = "x"
        String fullFilepath = "/tmp/blah/anotherdir/" + expectedfilename
        String filename = Utils.getFilenameFromFullFilepath(fullFilepath)
        Assert.assertEquals("Filename is incorrect", expectedfilename, filename)
    }

    @Test
    public void resizeImageTest() {
        BufferedImage bi = ImageIO.read(new File(backgroundFile))
        int xSize = bi.getWidth() * 3
        int ySize = bi.getHeight() * 1.3

        BufferedImage bi2 = Utils.getImageScaled(bi, xSize, ySize)
        File tempFile = File.createTempFile("tempFile_resized", ".png")

        ImageIO.write(bi2, "png", tempFile)

        Assert.assertTrue("Buffered Image was not created", tempFile.exists())

        BufferedImage bi3 = ImageIO.read(tempFile)
        int resizedXSize = bi3.width
        Assert.assertEquals("Rezied is wrong size", xSize, resizedXSize)

        tempFile.deleteOnExit()
    }


    @Test
    public void getScaled() {
        BufferedImage bi = ImageIO.read(new File(backgroundFile))

        double scaleFactor = 3.125235

        BufferedImage bi2 = Utils.getResizedRenderedImage(bi, scaleFactor)
        File tempFile = File.createTempFile("tempFile", ".png")
        ImageIO.write(bi2, "png", tempFile)

        int expectedXsize = bi.width * scaleFactor
        int actualXsize = bi2.width
        Assert.assertEquals("Rezied is wrong size", expectedXsize, actualXsize)

        tempFile.deleteOnExit()
    }
}
