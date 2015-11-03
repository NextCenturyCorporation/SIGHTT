
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

import groovy.util.logging.Log4j

import java.awt.*
import java.util.List

/**
 * Given a ModelLocation (random, center, etc.) and possible points, determine
 * where to put the model in the image.
 */
@Log4j
class ModelLocationCalculator {

    ModelLocation location
    def points
    Point imageSize
    Random random

    ModelLocationType loc

    ModelLocationCalculator(ModelLocation location, points, Point imageSize) {
        this.location = location
        this.points = points
        this.imageSize = imageSize
        this.random = new Random()
        initialize()
    }

    /**
     * Initialize internal variables depending on the type of location we need to calculate
     */
    def void initialize() {
        switch (location) {
            case ModelLocation.CENTERED:
                loc = new ModelLocationTypeCenter()
                break

            case ModelLocation.RANDOM:
                loc = new ModelLocationTypeRandom()
                break

            case ModelLocation.POINT:
                loc = new ModelLocationTypePoint()
                break

            case ModelLocation.MULTILINE:
                loc = new ModelLocationTypeLine()
                break

            case ModelLocation.RECTANGLE:
                loc = new ModelLocationTypeArea()
                break

            case ModelLocation.POLYGON:
                loc = new ModelLocationTypeArea()
                break
        }
        loc.initialize()
    }

    Point getPoint() {
        return loc.getPoint()
    }

    interface ModelLocationType {
        void initialize()

        Point getPoint()
    }

    class ModelLocationTypeCenter implements ModelLocationType {
        def pt

        @Override
        void initialize() {
            pt = new Point((int) imageSize.x / 2, (int) imageSize.y / 2)
        }

        @Override
        Point getPoint() {
            return pt
        }
    }

    class ModelLocationTypeRandom implements ModelLocationType {
        @Override
        void initialize() {}

        @Override
        Point getPoint() {
            def x = random.nextInt((int) (imageSize.x - 1)) + 1
            def y = random.nextInt((int) (imageSize.y - 1)) + 1
            return new Point(x, y)
        }
    }

    class ModelLocationTypePoint implements ModelLocationType {
        def pt

        @Override
        void initialize() {
            pt = points[0]
        }

        @Override
        Point getPoint() {
            return pt
        }
    }

    class ModelLocationTypeLine implements ModelLocationType {

        // Given a series of points, indicating a multiline, form a structure that keeps track of how far along
        // the entire line (in terms of percent) each segment is
        List<Double> segmentLengths = new ArrayList<Double>()

        @Override
        void initialize() {
            // Calculate the entire length
            double length = 0
            for (int ii = 0; ii < points.size() - 1; ii++) {
                length += points[ii].distance(points[ii + 1])
            }

            def accumulatedLength = 0
            for (int ii = 0; ii < points.size() - 1; ii++) {
                def percentLength = points[ii].distance(points[ii + 1]) / length
                accumulatedLength += percentLength
                segmentLengths.add(accumulatedLength)
            }
        }

        @Override
        Point getPoint() {
            // Determine which segment to go on
            int segmentIndex = 0
            def r = random.nextDouble()
            for (int ii = 0; ii < points.size() - 1; ii++) {
                if (r < segmentLengths.get(ii)) {
                    segmentIndex = ii
                    break
                }
            }

            // Pick a random point along the segment
            r = random.nextDouble()
            def deltaX = points[segmentIndex + 1].x - points[segmentIndex].x
            def deltaY = points[segmentIndex + 1].y - points[segmentIndex].y
            def x = (int) (points[segmentIndex].x + r * deltaX)
            def y = (int) (points[segmentIndex].y + r * deltaY)
            return new Point(x, y)
        }
    }

    class ModelLocationTypeArea implements ModelLocationType {

        Polygon polygon
        Rectangle boundingBox

        @Override
        void initialize() {
            polygon = new Polygon()
            for (Point pt in points) {
                polygon.addPoint((int) pt.x, (int) pt.y)
            }
            boundingBox = polygon.getBounds()
        }

        @Override
        Point getPoint() {
            while (true) {
                def x = boundingBox.x + random.nextDouble() * boundingBox.width
                def y = boundingBox.y + random.nextDouble() * boundingBox.height
                Point pt = new Point((int) x, (int) y)
                if (polygon.contains(pt)) {
                    return pt
                }
            }
        }
    }
}
