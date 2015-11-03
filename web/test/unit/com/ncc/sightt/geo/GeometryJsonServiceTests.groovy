
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

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.web.json.JSONObject

@TestFor(GeometryJsonService)
@Mock([Geometry, Vector3D])
class GeometryJsonServiceTests {

    private static final String JSON = '{"attrs":{"width":900,"height":700,"visible":true,"listening":true,"opacity":1,"x":0,"y":0,"scale":{"x":1,"y":1},"rotation":0,"offset":{"x":0,"y":0},"draggable":false,"dragOnTop":true},"nodeType":"Stage","children":[{"attrs":{"clearBeforeDraw":true,"visible":true,"listening":true,"opacity":1,"x":0,"y":0,"scale":{"x":1,"y":1},"rotation":0,"offset":{"x":0,"y":0},"draggable":false,"dragOnTop":true},"nodeType":"Layer","children":[{"attrs":{"visible":true,"listening":true,"opacity":1,"x":50,"y":50,"scale":{"x":1,"y":1},"rotation":0,"offset":{"x":0,"y":0},"draggable":false,"dragOnTop":true,"id":"background","width":1000,"height":844},"nodeType":"Shape","shapeType":"Image"}]},{"attrs":{"clearBeforeDraw":true,"visible":true,"listening":true,"opacity":1,"x":0,"y":0,"scale":{"x":1,"y":1},"rotation":0,"offset":{"x":0,"y":0},"draggable":false,"dragOnTop":true},"nodeType":"Layer","children":[]},{"attrs":{"clearBeforeDraw":true,"visible":true,"listening":true,"opacity":1,"x":0,"y":0,"scale":{"x":1,"y":1},"rotation":0,"offset":{"x":0,"y":0},"draggable":false,"dragOnTop":true},"nodeType":"Layer","children":[{"attrs":{"points":[{"x":537,"y":401},{"x":537,"y":50}],"lineCap":"butt","visible":true,"listening":true,"opacity":1,"x":0,"y":0,"scale":{"x":1,"y":1},"rotation":0,"offset":{"x":0,"y":0},"draggable":false,"dragOnTop":true,"id":"zline","stroke":"red","strokeWidth":3,"meters":"20"},"nodeType":"Shape","shapeType":"Multiline"},{"attrs":{"points":[{"x":537,"y":401},{"x":193,"y":612}],"lineCap":"butt","visible":true,"listening":true,"opacity":1,"x":0,"y":0,"scale":{"x":1,"y":1},"rotation":0,"offset":{"x":0,"y":0},"draggable":false,"dragOnTop":true,"id":"yline","stroke":"blue","strokeWidth":3,"meters":"4"},"nodeType":"Shape","shapeType":"Multiline"},{"attrs":{"points":[{"x":537,"y":401},{"x":856,"y":449}],"lineCap":"butt","visible":true,"listening":true,"opacity":1,"x":0,"y":0,"scale":{"x":1,"y":1},"rotation":0,"offset":{"x":0,"y":0},"draggable":false,"dragOnTop":true,"id":"xline","stroke":"green","strokeWidth":3},"nodeType":"Shape","shapeType":"Multiline"},{"attrs":{"radius":6,"visible":true,"listening":true,"opacity":1,"x":535,"y":402,"scale":{"x":1,"y":1},"rotation":0,"offset":{"x":0,"y":0},"draggable":true,"dragOnTop":true,"id":"origin","fill":"white","stroke":"purple","strokeWidth":2},"nodeType":"Shape","shapeType":"Circle"},{"attrs":{"radius":6,"visible":true,"listening":true,"opacity":1,"x":220,"y":612,"scale":{"x":1,"y":1},"rotation":0,"offset":{"x":0,"y":0},"draggable":true,"dragOnTop":true,"id":"xpoint","fill":"white","stroke":"purple","strokeWidth":2},"nodeType":"Shape","shapeType":"Circle"},{"attrs":{"radius":6,"visible":true,"listening":true,"opacity":1,"x":535,"y":50,"scale":{"x":1,"y":1},"rotation":0,"offset":{"x":0,"y":0},"draggable":true,"dragOnTop":true,"id":"zpoint","fill":"white","stroke":"purple","strokeWidth":2},"nodeType":"Shape","shapeType":"Circle"},{"attrs":{"radius":6,"visible":true,"listening":true,"opacity":1,"x":856,"y":449,"scale":{"x":1,"y":1},"rotation":0,"offset":{"x":0,"y":0},"draggable":true,"dragOnTop":true,"id":"ypoint","fill":"white","stroke":"purple","strokeWidth":2},"nodeType":"Shape","shapeType":"Circle"}]},{"attrs":{"clearBeforeDraw":true,"visible":true,"listening":true,"opacity":1,"x":0,"y":0,"scale":{"x":1,"y":1},"rotation":0,"offset":{"x":0,"y":0},"draggable":false,"dragOnTop":true},"nodeType":"Layer","children":[]}]}'

    void testCreateGeometry() {
        Geometry geometry = service.createGeometry(createJsonObject())

        assert geometry.imageSizeInPixels == new Rectangle(width: 1000, height: 844, topLeftCorner: new Point(x: 50, y: 50))
        assert geometry.json.length() == JSON.length()
        assert geometry.origin == new Point(x: 537, y: 401)

        assert geometry.vectors.contains(createTestVector1())
        assert geometry.vectors.contains(createTestVector2())
        assert geometry.vectors.contains(createTestVector3())
    }

    void testGetDirectionFromIndex() {
        assert service.getDirectionFromIndex(0) == "z"
        assert service.getDirectionFromIndex(1) == "y"
        assert service.getDirectionFromIndex(2) == "x"
    }

    //140,532 -- 125,51
    Vector3D createTestVector1() {
        Point p1 = new Point(x: 537, y: 401)
        Point p2 = new Point(x: 537, y: 50)

        service.createVectorFromPoints(p1, p2, 20, "z")
    }

    //140,532 -- 57,622
    Vector3D createTestVector2() {
        Point p1 = new Point(x: 537, y: 401)
        Point p2 = new Point(x: 193, y: 612)

        service.createVectorFromPoints(p1, p2, 4, "y")
    }

    //140,532 -- 899,541
    Vector3D createTestVector3() {
        Point p1 = new Point(x: 537, y: 401)
        Point p2 = new Point(x: 856, y: 449)

        service.createVectorFromPoints(p1, p2, 0, "x")
    }

    private JSONObject createJsonObject() {
        new JSONObject(JSON)
    }
}
