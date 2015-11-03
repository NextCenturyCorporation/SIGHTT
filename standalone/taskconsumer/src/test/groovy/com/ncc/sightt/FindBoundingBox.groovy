
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

import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage

import javax.imageio.ImageIO

/**
 * Class to experiment with what it means to try to find a bounding box around a rendered object.
 *
 * The assumption is that the output value is zero in integer RGB space where there is no object
 * and non-zero where there is.  We do not use the alpha channel. 
 */
class FindBoundingBox {

    static final String filename = "resources/image_0_0_0.png"

    def findBox() {
        BufferedImage bi = ImageIO.read(new File(filename))

        // I'm really not sure what this does
        //    WritableRaster wr = bi.getAlphaRaster()
        //    int numBands = wr.getNumBands()
        //    int numDataElements = wr.getNumDataElements()
        //    double[] pixelsVal = new double[10000]
        //    int[] pixels = wr.getPixel(10, 10, pixelsVal)
        //    DataBuffer db = wr.getDataBuffer()
        //    int datatype = db.getDataType();
        //    int databufferSize = db.getSize();
        //    for (int ii=0; ii<databufferSize; ii++) {
        //      int elem = db.getElem(ii)
        //      if ( elem != 0) {
        //        System.out.println("elem " + ii + " : " + elem)
        //        System.exit(0)
        //      }
        //    }

        int minx = 999999, miny = 999999, maxx = -1, maxy = -1;

        for (int jj = 0; jj < bi.height; jj++) {
            for (int ii = 0; ii < bi.width; ii++) {
                int rgb = bi.getRGB(ii, jj)
                if (rgb != 0) {
                    if (ii < minx) minx = ii
                    if (ii > maxx) maxx = ii
                    if (jj < miny) miny = jj
                    if (jj > maxy) maxy = jj
                }
            }
        }
        System.out.println(" " + minx + "  " + miny + " to " + maxx + "  " + maxy)

        Graphics g = bi.getGraphics()
        g.setColor(Color.YELLOW)
        g.drawRect(minx, miny, maxx - minx, maxy - miny)

        ImageIO.write(bi, "png", new File("/tmp/bb.png"))
    }

    static main(args) {
        FindBoundingBox f = new FindBoundingBox()
        f.findBox()
    }
}
