
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

import spock.lang.Specification

import javax.vecmath.Vector2d
import javax.vecmath.Vector3f

import static spock.util.matcher.HamcrestMatchers.closeTo

class PointsOnSphereSpec extends Specification {

    def "testGetLargeNumberOfPoints"(int numPtsAskedFor) {
        given:
        PointsOnSphere pos = new PointsOnSphere()

        when:
        long timeStart = new Date().getTime()
        PointsOnSphereResult r = pos.getThetaPhiRotationPoints(numPtsAskedFor)
        long timeEnd1 = new Date().getTime()
        // DEBUG Print statement
        // r.print();
        def simplified = pos.getSimplifiedXYZPoints(r)
        long timeEnd2 = new Date().getTime()
        def numSimplePts = simplified.size

        long diff1 = timeEnd1 - timeStart
        println(" Time between start and end1 " + diff1)
        long diff2 = timeEnd2 - timeEnd1
        println(" Time between end1 and end2 " + diff2)

        // DEBUG Print statement
        //        println(" ------ simplified ------")
        //        simplified.each { it -> println(" " + it.x + " " + it.y + " " + it.z + " " + it.theta + " " + it.phi) }
        //        println(" ------ done simplified ------\n\n")

        then:
        numSimplePts <= numPtsAskedFor

        where:
        numPtsAskedFor << [10, 100, 1000]
    }


    def "testGetThetaPhiRotationPointsViaIteration"(int numPtsAskedFor) {
        given:
        PointsOnSphere pos = new PointsOnSphere()

        when:
        PointsOnSphereResult result = pos.getThetaPhiRotationPointsViaIteration(numPtsAskedFor)
        int numPts = result.points.size()
        double angle = result.angle


        then:
        numPts closeTo(numPtsAskedFor, 5)
        result.angle != null
        result.angle > 0
        result.rotations > 0

        where:
        numPtsAskedFor << [10, 100, 1000]
    }

    def "testGetSingleThetaPhiRotationPoint"() {
        given:
        PointsOnSphere pos = new PointsOnSphere()

        when:
        def result = pos.getThetaPhiRotationPoints(1)

        then:
        result.points.size() == 1
        result.points[0].x == 0
        result.points[0].y == 0
        result.points[0].z == 0
    }


    def "testGetClosestNumberOfAspectsViaIteration"(int numPtsAskedFor) {
        given:
        PointsOnSphere pos = new PointsOnSphere()

        when:
        int numAspectsReturned = pos.getClosestNumberOfAspectsViaIteration(numPtsAskedFor)

        List<Vector2d> aspects = pos.getThetaPhiAspects(numAspectsReturned)
        double sepAngle = pos.getSeparationAngle(aspects)
        int numRots = Math.floor(Math.PI * 2 / sepAngle)
        int resultingNumberOfPoints = numAspectsReturned * numRots


        then:
        resultingNumberOfPoints closeTo(numPtsAskedFor, 5)

        where:
        numPtsAskedFor << [10, 100, 1000]
    }


    def "testGetAspectsAndNumRotationForNumAspects"() {
        given:
        PointsOnSphere pos = new PointsOnSphere()

        when:
        PointsOnSphereResult result = pos.getAspectsAndNumRotationForNumAspects(100)
        def rot = (Math.PI * 2 / (0.32924588))
        def numRots = result.rotations

        then:
        result.aspects.size == 100
        numRots closeTo(rot, 0.5)
    }

    def "testGetXYZAspectsbySpiral"() {
        given:
        PointsOnSphere pos = new PointsOnSphere()

        when:
        List<Vector3f> aspects = pos.generateXYZAspectsBySpiral(100)

        then:
        aspects.size == 100
        // TODO : Determine what else to test here
    }

    def "testSeparationAngleOnePoint"() {
        given:
        PointsOnSphere pos = new PointsOnSphere()
        List<Vector2d> pts = pos.getThetaPhiAspects(1)

        when:
        def angle = pos.getSeparationAngle(pts)

        then:
        angle closeTo(Math.PI, 0.00001)
    }

    def "testSeparationAngle100Points"() {
        given:
        PointsOnSphere pos = new PointsOnSphere()
        List<Vector2d> pts = pos.getThetaPhiAspects(100)

        when:
        def angle = pos.getSeparationAngle(pts)

        // NOTE:  This value determined by experimentation.  Wish I could have a closed form for it.
        then:
        angle closeTo(0.329242588, 0.0001)
    }


    def "getAngleBetweenTwoThetaPhi"() {
        given:
        PointsOnSphere pos = new PointsOnSphere()
        Vector3f xyz1 = new Vector3f(1, 0, 0)
        Vector2d thetaPhi1 = pos.getThetaPhiForXYZ(xyz1)
        Vector3f xyz2 = new Vector3f(0, 1, 0)
        Vector2d thetaPhi2 = pos.getThetaPhiForXYZ(xyz2)

        when:
        def angle = pos.angle(thetaPhi1, thetaPhi2)

        then:
        angle closeTo(PointsOnSphere.PIOver2, 0.0001)
    }

    def "getAngleBetweenTwoThetaPhi2"() {
        given:
        PointsOnSphere pos = new PointsOnSphere()
        Vector3f xyz1 = new Vector3f(0.7071, 0.7071, 0)
        Vector2d thetaPhi1 = pos.getThetaPhiForXYZ(xyz1)
        Vector3f xyz2 = new Vector3f(-0.7071, 0, 0.7071)
        Vector2d thetaPhi2 = pos.getThetaPhiForXYZ(xyz2)

        when:
        def angle = pos.angle(thetaPhi1, thetaPhi2)

        then:
        angle closeTo(Math.PI * 2 / 3, 0.0001)
    }

    /**
     * Converts from XYZ --> ThetaPhi and then back from ThetaPhi --> XYZ.  In theory,
     * you should get the exact same value (modulo rounding).
     */
    def "getXYZforThetaPhi"(Vector3f pt) {
        given:
        PointsOnSphere pos = new PointsOnSphere()
        double radius = Math.sqrt(pt.x * pt.x + pt.y * pt.y + pt.z * pt.z)

        when:
        Vector2d thetaPhi = pos.getThetaPhiForXYZ(pt)
        Vector3f ptXYZ = pos.getXYZForThetaPhi(thetaPhi)
        double radius2 = Math.sqrt(ptXYZ.x * ptXYZ.x + ptXYZ.y * ptXYZ.y + ptXYZ.z * ptXYZ.z)
        def x = ptXYZ.x
        def y = ptXYZ.y
        def z = ptXYZ.z

        then:
        radius closeTo(1, 0.001)
        radius2 closeTo(1, 0.001)
        x closeTo(pt.getX(), 0.0001)
        y closeTo(pt.getY(), 0.0001)
        z closeTo(pt.getZ(), 0.0001)

        where:
        pt << [new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0),
                new Vector3f(0, 0, 1),
                new Vector3f(-1, 0, 0),
                new Vector3f(-0.707106781, +0.707106781, 0)]
    }

    def "getThetaPhiForXYZat010"() {
        when:
        PointsOnSphere pos = new PointsOnSphere()
        Vector3f xyz = new Vector3f(0, 1, 0)
        Vector2d thetaPhi = pos.getThetaPhiForXYZ(xyz)
        def theta = thetaPhi.x
        def phi = thetaPhi.y

        then:
        thetaPhi != null
        theta closeTo(PointsOnSphere.PIOver2, 0.001)
        phi closeTo(PointsOnSphere.PIOver2, 0.001)
    }
//
    def "getThetaPhiForXYZat100"() {
        when:
        PointsOnSphere pos = new PointsOnSphere()
        Vector3f xyz = new Vector3f(1, 0, 0)
        Vector2d thetaPhi = pos.getThetaPhiForXYZ(xyz)
        def phi = thetaPhi.y

        then:
        thetaPhi != null
        thetaPhi.x == 0
        phi closeTo(PointsOnSphere.PIOver2, 0.001)
    }

    def "testConvertPhiToPitch"() {

        when:
        PointsOnSphere pos = new PointsOnSphere()

        def listOfPts = []
        // Pi/2 (90) gets converted to 0
        listOfPts.add(new Vector3f(0f, (float) (Math.PI / 2f), 0f))
        listOfPts.add(new Vector3f(0f, (float) (Math.PI / 4f), 0f))
        listOfPts.add(new Vector3f(0f, (float) (3 * Math.PI / 4f), 0f))

        PointsOnSphereResult result = new PointsOnSphereResult(points: listOfPts, aspects: null, rotations: 1, angle: 0)
        def newListOfPoints = pos.convertInternalPhiToPitch(result).points
        def yVal1 = newListOfPoints[0].y
        def yVal2 = newListOfPoints[1].y
        def yVal3 = newListOfPoints[2].y

        // check that it is zero (to 5 digits)
        then:
        yVal1 closeTo(0, 0.00001)
        yVal2 closeTo((Math.PI / 4f), 0.0001)
        yVal3 closeTo((-Math.PI / 4f), 0.0001)
    }
}
