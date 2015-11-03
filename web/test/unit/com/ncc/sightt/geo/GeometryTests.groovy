
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

import com.ncc.sightt.Background
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

/**
 * @author tbrooks
 */
@TestFor(Geometry)
@Mock([Geometry, Vector3D])
class GeometryTests {

    void testConstraints() {

        def rectangle = new Rectangle(width: 400, height: 400)
        def origin = new Point(x: 200, y: 200)
        Geometry geometry = new Geometry(background: new Background(), json: "{}", imageSizeInPixels: rectangle, origin: origin)
        assert geometry.validate()

        geometry = new Geometry(background: new Background())
        assert !geometry.validate()

        geometry = new Geometry(json: "{}")
        assert !geometry.validate()

        geometry = new Geometry()
        assert !geometry.validate()
    }

    void testAddVectors() {
        Geometry geometry = new Geometry(background: new Background(), json: "{}")

        geometry.addToVectors(new Vector3D())
        geometry.addToVectors(new Vector3D(x: 5, y: 5, z: 5))
        geometry.save()

        assert geometry.vectors
        assert geometry.vectors.size() == 2
    }
}
