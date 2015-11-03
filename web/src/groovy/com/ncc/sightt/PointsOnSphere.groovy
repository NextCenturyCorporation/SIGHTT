
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

import javax.vecmath.Vector2d
import javax.vecmath.Vector3d
import javax.vecmath.Vector3f

/**
 * Get Points for rendering, where a point is Theta, Phi, and Rotation, on a sphere, in 
 * spherical coordinates.
 *
 * <pre>
 * Terminology:
 *   Aspect: a Theta, Phi pair, representing a direction
 *   Point:  a Theta, Phi, Rotation triplet, representing coordinates for a rendering
 *
 *
 * Coordinates: 
 *   XYZ is z-up, right handed.  X points towards user, Y points to the right (looking down X axis toward origin), Z up
 *   Theta: angle in XY plane from the X-axis, i.e. rotate about Z, commonly called 'yaw', 
 *        positive is counter-clockwise from X towards Y; 
 *   Phi: angle above XY plane, positive toward from Z axis, i.e. rotate about intrinsic (body-centered) -Y,
 *        commonly called 'pitch'.  It is rotating about -Y because the right-hand-rule about rotations would 
 *        normally indicate that positive rotation would be down, below XY plane, away from Z.
 *   Rotation: angle along body axis, i.e. rotate about intrinsic (body-centered) X, commonly called 'roll'.  It 
 *        is positive when (from the body point of view) leaning to the right, in accordance with right-hand-rule.
 * </pre>
 *
 * Theta: between -PI and PI  (-180 to 180)
 * Phi:   between -PI/2 and PI/2 (-90 to 90)
 * Rotation: between -PI and PI (-180 to 180)
 *
 * Note:   Internally, Phi is measured from the Z-axis because that is the way that mathematicians do it, going from 
 * 0 to 180.  Externally, the value returned is 90 (straight up, i.e. 0 internal) to -90 (straight down, i.e. 180 
 * internal), because that is easier to understand.
 */
class PointsOnSphere {

    // Number of Points below which we have one rotation
    static final int NUM_POINTS_WITH_ONE_ROTATION = 23

    // Convenience constant.  PI2 = 2 Pi Radians, i.e. 360 degrees
    static final double PI2 = 2 * Math.PI

    // Convenience constant. PIOver2 = Pi / 2, i.e. 90 degrees
    static final double PIOver2 = Math.PI / 2

    /**
     * Primary function of this class: get the points (consisting of a vector3d: theta, phi, rotation)
     * number of aspects, number of rotations, and the angle between aspects.  Note that the number
     * of points may not be exactly #aspects * #rotations because there could be an uneven number, so some
     * may have been removed from the list
     */
    PointsOnSphereResult getThetaPhiRotationPoints(int numPointsAskedFor) {
        PointsOnSphereResult result

        if (numPointsAskedFor == 1) {
            result = getThetaPhiRotationPointsForSinglePoint()
        } else if (numPointsAskedFor <= NUM_POINTS_WITH_ONE_ROTATION) {
            result = getThetaPhiRotationPointsForSingleRotation(numPointsAskedFor)
        } else {
            result = getThetaPhiRotationPointsViaIteration(numPointsAskedFor)
        }

        return convertInternalPhiToPitch(result)
    }

    /**
     * Given a result, convert the Phi angles from [0,180] to [90,-90]
     */
    def convertInternalPhiToPitch(PointsOnSphereResult result) {
        def points = result.points
        points.each { i -> i.y = PIOver2 - i.y }
        result
    }

    def convertPitchToInternalPhi(PointsOnSphereResult result) {
        def points = result.points
        points.each { i -> i.y = PIOver2 - i.y }
        result
    }


    def convertInternalPhiValueToPitchValue = {
        (PIOver2 - it)
    }

    /**
     * Return XYZ points representing the different Aspects (NOT POINTS!)
     * for the set of ThetaPhiRotation points given.
     */
    def getSimplifiedXYZPoints(PointsOnSphereResult results) {

        def returnedPoints = []
        results.aspects.each { pt ->

            Vector3f xyzPt = getXYZForThetaPhi(pt)
            xyzPt.scale(12)
            def thetaPitch = new Vector2d(pt.x, convertInternalPhiValueToPitchValue(pt.y))

            def combinedPoints = [x: xyzPt.x, y: xyzPt.y, z: xyzPt.z, theta: thetaPitch.x, phi: thetaPitch.y]
            returnedPoints.add(combinedPoints)
        }
        return returnedPoints
    }

    /**
     * Get Theta, Phi, Rotation for single aspect.  Do not rotate, which means
     * that it should be returned as 0,0,0 after conversion from internalPhi to Pitch.
     */
    def getThetaPhiRotationPointsForSinglePoint() {
        Vector3d pt = new Vector3d(0, PIOver2, 0)
        PointsOnSphereResult result = new PointsOnSphereResult(points: [pt], aspects: new ArrayList<Vector2d>(), rotations: 1, angle: PI2)
        return result
    }

    /**
     * Get Theta, Phi, Rotation where there is only one rotation per ThetaPhi aspect,
     * which means that we simply get # pointsAskedFor ThetaPhi aspects and then step
     * through rotations from -pi to pi.
     *
     * TODO:  Handle 2-6 or less points differently, to get better values
     */
    def getThetaPhiRotationPointsForSingleRotation(int numPointsAskedFor) {
        List<Vector2d> thetaPhiPoints = getThetaPhiAspects(numPointsAskedFor)
        double angle = getSeparationAngle(thetaPhiPoints)

        List<Vector3d> pts = new ArrayList<Vector3d>()
        for (int ii = 0; ii < numPointsAskedFor; ii++) {
            double rotation = -Math.PI + PI2 * ii / numPointsAskedFor
            Vector3d pt = new Vector3d(thetaPhiPoints.get(ii).x, thetaPhiPoints.get(ii).y, rotation)
            pts.add(pt)
        }
        PointsOnSphereResult result = new PointsOnSphereResult(points: pts,
                aspects: thetaPhiPoints, rotations: 1, angle: angle)
        return result
    }

    /**
     * Determine the number of aspects via iteration, get that many aspects, and add correct number
     * of rotations per aspect, then remove any extras
     */
    def getThetaPhiRotationPointsViaIteration(int numPointsAskedFor) {
        // Determine number of aspects needed for this many total points
        int numAspects = getClosestNumberOfAspectsViaIteration(numPointsAskedFor)

        // Get that many Theta/Phi aspects
        def result = getAspectsAndNumRotationForNumAspects(numAspects)

        // Turn Theta/Phi aspects into theta/phi/rot points
        List<Vector3d> pts = new ArrayList<Vector3d>()
        result.aspects.each { i ->
            for (int ii = 0; ii < result.rotations; ii++) {
                double rotation = -Math.PI + PI2 * ii / result.rotations
                Vector3d pt = new Vector3d(i.x, i.y, rotation)
                pts.add(pt)
            }
        }

        result = new PointsOnSphereResult(points: pts, aspects: result.aspects, rotations: result.rotations, angle: result.angle)
        return result
    }

    /**
     * Given that you want X points (like 1000), this function iteratively determines the number of
     * aspects and rotations such that aspects * rotations is greater than, but as close as possible to,
     * numPointsAskedFor
     */
    int getClosestNumberOfAspectsViaIteration(int numPointsAskedFor) {
        int guess
        int lowGuess = 1
        int highGuess = numPointsAskedFor
        int diff = highGuess - lowGuess
        int totalPoints = 0
        while (totalPoints != numPointsAskedFor && diff > 1) {
            guess = (int) (highGuess + lowGuess) / 2
            def result = getAspectsAndNumRotationForNumAspects(guess)
            totalPoints = guess * result.rotations

            if (totalPoints > numPointsAskedFor) {
                highGuess = guess
            }
            if (totalPoints < numPointsAskedFor) {
                lowGuess = guess
            }
            diff = highGuess - lowGuess
        }
        if (diff == 1) {
            guess = highGuess
        }
        return guess
    }

    /**
     * Given a number of aspects, it gets that many aspects, determines the angular
     * separation between aspects, the number of rotations for that separation, and then
     * returns all the information
     */
    PointsOnSphereResult getAspectsAndNumRotationForNumAspects(int numAspects) {
        List<Vector2d> aspects = getThetaPhiAspects(numAspects)
        double angle = getSeparationAngle(aspects)
        int numRotations = (int) Math.floor(PI2 / angle)
        PointsOnSphereResult result = new PointsOnSphereResult(points: [], aspects: aspects, rotations: numRotations, angle: angle)
        return result
    }

    /**
     * Get the Theta Phi aspects, with the catch that the spiral code we have
     * gives XYZ, so do a conversion.
     */
    public List<Vector2d> getThetaPhiAspects(int numAspects) {
        List<Vector3f> xyzPts = generateXYZAspectsBySpiral(numAspects)
        def thetaPhiAspects = getThetaPhiForXYZ(xyzPts)
        thetaPhiAspects
    }

    /**
     * Get XYZ aspects 'evenly' distributed on a sphere.  This uses Vogel's method for
     * packing points in a disk, using the Golden Angle, applied to a sphere.
     *
     * NOTE:  this produces very well distributed points, but like Vogel's method for
     * distributing points on a disk, successive points are NOT right next to each other.
     *
     * From:   http://blog.marmakoide.org/?p=1
     */
    public List<Vector3f> generateXYZAspectsBySpiral(int n) {
        List<Vector3f> pts = new ArrayList<Vector3f>();
        double golden = Math.PI * (3 - Math.sqrt(5));
        for (def ii = 0; ii < n; ii++) {
            double dist = golden * ii;
            double z = (1 - (1 / n)) - 2 * (ii / n);
            double rad = Math.sqrt(1 - (z * z));
            double x = rad * Math.cos(dist);
            double y = rad * Math.sin(dist);
            Vector3f p = new Vector3f((float) x, (float) y, (float) z);
            pts.add(p);
        }
        return pts;
    }

    /**
     * Get the separation angle between aspects in a list.  This does not assume that the
     * aspects are in order (because the spiral algorithm does not put them in order), but
     * searches indices around early aspects.  Don't use the first since it is not a good example.
     */
    def getSeparationAngle(List<Vector2d> aspects) {
        def smallest
        if (aspects.size <= 1) {
            smallest = Math.PI
        } else if (aspects.size == 2) {
            smallest = angle(aspects.get(0), aspects.get(1))
        } else {
            smallest = getSmallestAngle(2, aspects, 10)
        }
        return smallest
    }

    def getSmallestAngle(int index, List<Vector2d> aspects, int numToSearch = -1) {
        def pt = aspects.get(index)
        double smallest = Double.MAX_VALUE
        int smallestJJ;
        if (numToSearch == -1 || numToSearch > aspects.size()) {
            numToSearch = aspects.size();
        }
        for (int jj = 0; jj < numToSearch; jj++) {
            if (jj != index) {
                double angle = angle(pt, aspects.get(jj))
                if (angle < smallest) {
                    smallestJJ = jj
                    smallest = angle
                }
            }
        }

        return smallest
    }

/**
 * Angle between two aspects in spherical coordinates
 * From: http://mathforum.org/kb/message.jspa?messageID=7291210
 */
    def angle(def pt1, def pt2) {
        double psi = Math.acos(Math.cos(pt1.y) * Math.cos(pt2.y)
                + Math.sin(pt1.y) * Math.sin(pt2.y)
                * Math.cos(pt1.x - pt2.x));
    }

// -------------------------------------------------------------------------
// Conversion routines
// -------------------------------------------------------------------------

/**
 * Convert list of XYZ to Theta/Phi
 */
    public List<Vector2d> getThetaPhiForXYZ(List<Vector3f> xyzVec) {
        List<Vector2d> pts = new ArrayList<Vector2d>();
        xyzVec.each { i -> pts.add(getThetaPhiForXYZ(i)) }
        pts
    }

/**
 * Convert list of Theta/Phi to XYZ
 */
    public List<Vector3f> getXYZForThetaPhi(List<Vector2d> thetaPhiVec) {
        List<Vector3f> pts = new ArrayList<Vector3f>();
        thetaPhiVec.each { i -> pts.add(getXYZForThetaPhi(i)) }
        pts
    }

/**
 * Convert spherical coords to XYZ, Note: no radius! Assumes on unit sphere.
 * Coordinate system described in class comment.
 *
 * From:
 * http://www.math.montana.edu/frankw/ccp/multiworld/multipleIVP/spherical
 * /learn.htm
 */
    public Vector3f getXYZForThetaPhi(Vector2d thetaPhi) {
        double x = Math.cos(thetaPhi.x) * Math.sin(thetaPhi.y)
        double y = Math.sin(thetaPhi.x) * Math.sin(thetaPhi.y)
        double z = Math.cos(thetaPhi.y);
        Vector3f p = new Vector3f((float) x, (float) y, (float) z);
    }

/**
 * Convert XYZ to a spherical coord system. Note that there is no radius, so
 * XYZ must lie on a unit sphere.
 *
 * From:
 * http://www.math.montana.edu/frankw/ccp/multiworld/multipleIVP/spherical
 * /learn.htm
 */
    public Vector2d getThetaPhiForXYZ(Vector3f xyz) {
        double theta
        double phi

        // Special case, on Z-axis
        if (xyz.x == 0 && xyz.y == 0) {
            theta = 0;
            phi = 0;
            if (xyz.z < 0) {
                phi = Math.PI;
            }
        } else {
            theta = Math.atan2(xyz.y, xyz.x);
            phi = Math.acos(xyz.z);
        }
        Vector2d p = new Vector2d(theta, phi);
    }

}
