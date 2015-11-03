
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
package com.ncc.sightt.geo

import org.codehaus.groovy.grails.web.json.JSONObject

class GeometryJsonService {

    Geometry createGeometry(JSONObject jsonObject) {
        Geometry geometry = new Geometry()

        geometry.json = jsonObject.toString()
        geometry.imageSizeInPixels = createImageRectangleFromJson(jsonObject)
        geometry.origin = createOriginFromJson(jsonObject)
        for (i in 0..2) {
            geometry.addToVectors(createVectorFromJson(jsonObject, i))
        }
        return geometry
    }

    Vector3D createVectorFromJson(JSONObject jsonObject, int i) {
        def pointsArray = jsonObject.get("children")[2].get("children")[i].get("attrs").get("points")
        Point p1 = createPointFromJson(pointsArray[0])
        Point p2 = createPointFromJson(pointsArray[1])
        double meters = getMetersFromJson(jsonObject, i);

        createVectorFromPoints(p1, p2, meters, getDirectionFromIndex(i))
    }

    double getMetersFromJson(JSONObject jsonObject, int i) {
        JSONObject lineObject = jsonObject.get("children")[2].get("children")[i].get("attrs")

        if (lineObject.containsKey("meters"))
            return Double.valueOf(lineObject.get("meters"))

        return 0
    }

    Point createOriginFromJson(JSONObject jsonObject) {
        def pointObject = jsonObject.get("children")[2].get("children")[0].get("attrs").get("points")[0]
        createPointFromJson(pointObject)
    }

    Rectangle createImageRectangleFromJson(JSONObject jsonObject) {
        def imageObject = jsonObject.get("children")[0].get("children")[0].get("attrs")
        def width = imageObject.get("width")
        def height = imageObject.get("height")
        Point offset = createPointFromJson(imageObject)
        new Rectangle(width: width, height: height, topLeftCorner: offset)
    }

    private Point createPointFromJson(def pointObject) {
        def x = pointObject.get("x")
        def y = pointObject.get("y")

        return new Point(x: x, y: y)
    }

    private Vector3D createVectorFromPoints(Point p1, Point p2, double meters, String direction) {
        def slope = GeometryJsonService.slope(p1, p2)
        def slopeInfinite = (slope == null)
        def distance = GeometryJsonService.distanceFormula(p1, p2)
        Vector3D vector3D = new Vector3D(slope: slope, slopeInfinite: slopeInfinite, lengthInPixels: distance, id: 5, lengthInMeters: meters)
        vector3D[direction] = 1

        return vector3D
    }

    static double distanceFormula(Point p1, Point p2) {
        double xDeltaSquared = Math.pow(yDelta(p1, p2), 2)
        double yDeltaSquared = Math.pow(xDelta(p1, p2), 2)

        Math.sqrt(xDeltaSquared + yDeltaSquared)
    }

    static Double slope(Point p1, Point p2) {
        double yDelta = yDelta(p1, p2)
        double xDelta = xDelta(p1, p2)

        if (xDelta == 0)
            return null
        return yDelta / xDelta
    }

    private static double yDelta(Point p1, Point p2) {
        p2.y - p1.y
    }

    private static double xDelta(Point p1, Point p2) {
        p2.x - p1.x
    }

    private String getDirectionFromIndex(i) {
        char ch = 'z'
        return String.valueOf((char) (ch - i))
    }
}
