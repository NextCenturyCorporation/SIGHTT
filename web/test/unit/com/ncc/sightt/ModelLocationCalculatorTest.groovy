
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
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.junit.Before
import org.junit.Test

import java.awt.*
import java.util.List


/**
 * Test the ModelLocationCalculator
 */
@Log4j
class ModelLocationCalculatorTest {

    int NUM_TESTS = 1000

    @Before
    void setUp() {
        Logger.getLogger("com").level = Level.DEBUG
    }

    @Test
    void testCenter() {
        List<Point> pts = null
        Point imageSize = new Point(893, 234)
        ModelLocationCalculator calc = new ModelLocationCalculator(ModelLocation.CENTERED, pts, imageSize)

        Point pt = calc.getPoint()
        assert pt == new Point(446, 117)
    }

    @Test
    void testRandom() {
        List<Point> pts = null
        Point imageSize = new Point(893, 234)
        ModelLocationCalculator calc = new ModelLocationCalculator(ModelLocation.RANDOM, pts, imageSize)

        for (int ii = 0; ii < NUM_TESTS; ii++) {
            Point pt = calc.getPoint()
            assert pt.x > 0
            assert pt.x < 893
            assert pt.y > 0
            assert pt.y < 234
        }
    }

    @Test
    void testPoint() {
        List<Point> pts = new ArrayList<Point>()
        pts.add(new Point(124, 231))
        Point imageSize = new Point(893, 234)
        ModelLocationCalculator calc = new ModelLocationCalculator(ModelLocation.POINT, pts, imageSize)

        Point pt = calc.getPoint()
        assert pt.x == 124
        assert pt.y == 231
    }

    @Test
    void testLineWithTwoPoints() {
        List<Point> pts = new ArrayList<Point>()
        pts.add(new Point(124, 24))
        pts.add(new Point(45, 201))
        Point imageSize = new Point(893, 234)
        ModelLocationCalculator calc = new ModelLocationCalculator(ModelLocation.MULTILINE, pts, imageSize)

        for (int ii = 0; ii < NUM_TESTS; ii++) {
            Point pt = calc.getPoint()
            assert pt.x >= 45
            assert pt.x <= 124
            assert pt.y >= 24
            assert pt.y <= 201
        }
    }

    @Test
    void testLineWithManyPoints() {
        List<Point> pts = new ArrayList<Point>()
        pts.add(new Point(39, 95))
        pts.add(new Point(743, 8))
        pts.add(new Point(855, 146))
        pts.add(new Point(256, 323))
        pts.add(new Point(361, 11))
        pts.add(new Point(2, 4))
        pts.add(new Point(892, 10))
        Point imageSize = new Point(893, 534)
        ModelLocationCalculator calc = new ModelLocationCalculator(ModelLocation.MULTILINE, pts, imageSize)

        for (int ii = 0; ii < NUM_TESTS; ii++) {
            Point pt = calc.getPoint()
            assert pt.x >= 0
            assert pt.x <= 893
            assert pt.y >= 0
            assert pt.y <= 534
        }
    }

    @Test
    void testPolygonWithManyPoints() {
        List<Point> pts = new ArrayList<Point>()
        pts.add(new Point(39, 95))
        pts.add(new Point(743, 8))
        pts.add(new Point(855, 146))
        pts.add(new Point(256, 323))
        Point imageSize = new Point(893, 534)
        ModelLocationCalculator calc = new ModelLocationCalculator(ModelLocation.POLYGON, pts, imageSize)

        for (int ii = 0; ii < NUM_TESTS; ii++) {
            Point pt = calc.getPoint()
            assert pt.x >= 0
            assert pt.x <= 893
            assert pt.y >= 0
            assert pt.y <= 534
        }
    }
}
