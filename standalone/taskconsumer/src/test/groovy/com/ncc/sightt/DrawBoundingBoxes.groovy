
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

import static groovy.io.FileType.FILES
import groovy.util.logging.Log4j

import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage

import javax.imageio.ImageIO
import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller

import com.ncc.sightt.metadata.Annotation
import com.ncc.sightt.metadata.Polygon

/**
 * Given that we have an output zip with both metadata and png files, draw the boxes 
 * around the inserted objects in the images
 */
@Log4j
class DrawBoundingBoxes {

    static final String dir = "/home/HQ/cdorman/Downloads/comp"
    static final String outputDir = "/tmp/"
    static final String outputSuffix = ".2.png"

    static main(args) {
        DrawBoundingBoxes f = new DrawBoundingBoxes()
        f.drawBoxes()
    }

    def drawBoxes() {
        new File(dir).eachFileRecurse(FILES) {
            if (it.name.endsWith('.png')) {
                processImage(it)
            }
        }
    }

    def processImage(File imageFile) {
        if (!imageFile.exists()) {
            log.warn("Cannot find image file " + imageFile.getName())
            return
        }
        BufferedImage bi = ImageIO.read(imageFile)

        String xmlFilename = imageFile.getAbsolutePath() + ".xml"
        File xmlFile = new File(xmlFilename)
        if (!xmlFile.exists()) {
            log.warn "Found image file ${imageFile.name} but not the xml file ${xmlFilename}"
            return
        }

        JAXBContext jaxbContext = JAXBContext.newInstance(Annotation.class)
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller()
        Annotation a = jaxbUnmarshaller.unmarshal(xmlFile)
        Polygon p = a.getObject().get(0).getPolygon()
        int minx = Integer.parseInt p.getPt().get(0).getX()
        int miny = Integer.parseInt p.getPt().get(0).getY()
        int maxx = Integer.parseInt p.getPt().get(1).getX()
        int maxy = Integer.parseInt p.getPt().get(1).getY()

        System.out.println(" ${imageFile.name}   " + minx + "  " + miny + " to " + maxx + "  " + maxy)

        Graphics g = bi.getGraphics()
        g.setColor(Color.YELLOW)
        g.drawRect(minx, miny, maxx - minx, maxy - miny)

        String newFilename = outputDir + imageFile.getName() + outputSuffix
        ImageIO.write(bi, "png", new File(newFilename))
    }
}
