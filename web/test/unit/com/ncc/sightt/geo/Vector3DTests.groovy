
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

import grails.test.mixin.TestFor

@TestFor(Vector3D)
class Vector3DTests {

    void testConstraints() {
        Vector3D vector = new Vector3D(x: 1, y: 0, z: 0, slope: 1.0, slopeInfinite: false)
        assert vector.validate()

        vector = new Vector3D(x: 1, y: 0, z: 0, slope: 1.0)
        assert vector.validate()

        vector = new Vector3D(x: 1, y: 0, z: 0)
        assert vector.validate()

        vector = new Vector3D()
        assert vector.validate()
    }

    void testVectorsEqual() {
        Vector3D vector1 = new Vector3D(x: 1, y: 0, z: 0, slope: 1.0, slopeInfinite: false)
        Vector3D vector2 = new Vector3D(x: 1, y: 0, z: 0, slope: 1.0, slopeInfinite: false)

        assert vector1 == vector2
        vector1.setX(2.5)
        assert vector1 != vector2


    }
}
