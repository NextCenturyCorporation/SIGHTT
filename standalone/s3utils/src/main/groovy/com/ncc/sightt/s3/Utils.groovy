
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

import groovy.util.logging.Log4j
import org.apache.commons.lang3.tuple.ImmutablePair

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage

@Log4j
class Utils {

    static Random random = null

    def static getFilePathSuffix(String filepath) {
        int index = filepath.lastIndexOf(".");
        def lastSlash = filepath.lastIndexOf("/")
        int size = filepath.size()
        if (index != -1 && index > lastSlash && index + 1 < size) {
            return filepath.substring(index + 1);
        }
        return null
    }

    /** Get the model name from a mask file, i.e. after the initial "_" and before the first "."
     * <pre>
     *         mask_Target1.png0002.png      becomes   Target1
     *         mask_RPG7_Rocket.png0002.png  becomes   RPG7_Rocket
     * </pre>
     */
    static def getModelName(String name) {
        def beginIndex = name.indexOf("_")
        def endIndex = name.indexOf(".")
        if (beginIndex && endIndex && beginIndex + 1 < endIndex - 1) {
            def objName = name[beginIndex + 1..endIndex - 1]
            log.debug("Object name: ${objName}    ${beginIndex}  ${endIndex}")
            return objName
        }
        return name
    }

    static String getFilenameFromFullFilepath(String fullFilepath) {
        int slashIndex = fullFilepath.lastIndexOf("/")
        if (slashIndex == -1 || slashIndex == fullFilepath.length() - 1) {
            return fullFilepath
        }
        def filename = fullFilepath.substring(slashIndex + 1)
        filename
    }

    /** Given a model name and rotations, determine the basename for it */
    static def getBasenameForModelName(modelName, yaw, pitch, roll) {
        String baseFilename = modelName + "_" +
                Utils.getClosestIntDegreesForRadians(yaw) + "_" +
                Utils.getClosestIntDegreesForRadians(pitch) + "_" +
                Utils.getClosestIntDegreesForRadians(roll) + "_"
        return baseFilename
    }

    /** Find a file in a directory that match a pattern somewhere in their name.  This gets the last one */
    static def findMatchingFile(String directoryName, String filePatternString) {
        def pattern = ~/.*${filePatternString}.*/
        def returnval = null
        new File(directoryName).eachFileMatch(pattern) {
            log.debug("Matches: ${it}")
            returnval = it
        }
        returnval
    }

    /** Find files in a directory that match a pattern somewhere in their name */
    static def findMatchingFiles(String directoryName, String filePatternString) {
        def names = []
        def pattern = ~/.*${filePatternString}.*/
        def returnval = null
        new File(directoryName).eachFileMatch(pattern) { names.add(it) }
        names
    }

    /** Convert radians to degrees */
    static def convertRadiansToDegrees(radians) {
        return (radians * 180 / Math.PI)
    }

    /** Convert degrees to radians */
    static def convertDegreesToRadians(degrees) {
        return (degrees * Math.PI / 180)
    }

    /** Get a string representing degrees for radians.  Useful for printing */
    static String getClosestIntDegreesForRadians(radians) {
        float deg = convertRadiansToDegrees(radians)
        int degInt = deg.round()
        "" + degInt
    }

    static def convertPNGToJPG(File file) {

        // Make sure that it is a png file
        boolean endsWithPNG = file.getName().endsWith(".png")
        if (!endsWithPNG) {
            log.warn(" File ${file} does not end with .png; refusing to make it a png")
            return
        }

        int indexOfPNG = file.getAbsolutePath().lastIndexOf(".png")

        BufferedImage bi = ImageIO.read(file)
        if (bi) {
            BufferedImage bi2 = convertPNGToJPG(bi)
            String newFilename = file.getAbsolutePath().substring(0, indexOfPNG) + ".jpg"
            ImageIO.write(bi2, "jpg", new File(newFilename))
        }
        file.delete();
    }

    /**
     * Converts the given PNG buffered image to a JPG by re-drawing it without
     * its alpha channel.
     */
    static def convertPNGToJPG(BufferedImage image) {
        BufferedImage convertedImage = new BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
        Graphics2D graphics = convertedImage.createGraphics()
        graphics.drawImage(image, 0, 0, convertedImage.width, convertedImage.height, Color.white, null)
        convertedImage
    }

    static def getBoundingBoxOfNonZeroPixels(BufferedImage image) {
        int minX = image.width
        int maxX = 0
        int minY = image.height
        int maxY = 0
        for (xx in 1..image.width - 1) {
            for (yy in 1..image.height - 1) {
                int rgb = image.getRGB(xx, yy)
                int vr = (rgb >> 16) & 0xff
                int vg = (rgb >> 8) & 0xff
                int vb = rgb & 0xff
                if (vr != 0 || vg != 0 || vb != 0) {
                    if (xx < minX) minX = xx
                    if (xx > maxX) maxX = xx
                    if (yy < minY) minY = yy
                    if (yy > maxY) maxY = yy
                }
            }
        }
        def boundingBox = new ImmutablePair<Point, Point>(new Point(minX, minY), new Point(maxX, maxY))
    }

    static def getEmptyBoundingBox() {
        def boundingBox = new ImmutablePair<Point, Point>(new Point(0, 0), new Point(0, 0))
        return boundingBox
    }

    /**
     * Get a scaled image
     */
    def static getResizedRenderedImage(BufferedImage renderedImage, double scaleFactor) {
        int renderedWidth = renderedImage.width * scaleFactor
        int renderedHeight = renderedImage.height * scaleFactor
        def bi = Utils.getImageScaled(renderedImage, renderedWidth, renderedHeight)
        bi
    }

    /**
     * Get a resized image
     */
    static getImageScaled(BufferedImage image, int xSize, int ySize) {
        int type = image.type == 0 ? BufferedImage.TYPE_INT_ARGB : image.type
        BufferedImage resizedImage = new BufferedImage(xSize, ySize, type)

        Graphics2D g = resizedImage.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)

        float xImageScale = xSize / image.getWidth()
        float yImageScale = ySize / image.getHeight()
        g.scale(xImageScale, yImageScale)
        g.drawRenderedImage(image, null)
        g.dispose()
        resizedImage
    }
}
