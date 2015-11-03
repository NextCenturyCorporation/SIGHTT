
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

class Vector3D {

    static constraints = { slope nullable: true }

    double x
    double y
    double z

    boolean slopeInfinite = false
    Double slope = Double.valueOf(0)

    double lengthInPixels
    double lengthInMeters


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof Vector3D)) return false

        Vector3D vector3D = (Vector3D) o

        if (Double.compare(vector3D.lengthInMeters, lengthInMeters) != 0) return false
        if (Double.compare(vector3D.lengthInPixels, lengthInPixels) != 0) return false
        if (slopeInfinite != vector3D.slopeInfinite) return false
        if (Double.compare(vector3D.x, x) != 0) return false
        if (Double.compare(vector3D.y, y) != 0) return false
        if (Double.compare(vector3D.z, z) != 0) return false
        if (slope != vector3D.slope) return false

        return true
    }

    int hashCode() {
        int result
        long temp
        temp = x != +0.0d ? Double.doubleToLongBits(x) : 0L
        result = (int) (temp ^ (temp >>> 32))
        temp = y != +0.0d ? Double.doubleToLongBits(y) : 0L
        result = 31 * result + (int) (temp ^ (temp >>> 32))
        temp = z != +0.0d ? Double.doubleToLongBits(z) : 0L
        result = 31 * result + (int) (temp ^ (temp >>> 32))
        result = 31 * result + (slopeInfinite ? 1 : 0)
        result = 31 * result + (slope != null ? slope.hashCode() : 0)
        temp = lengthInPixels != +0.0d ? Double.doubleToLongBits(lengthInPixels) : 0L
        result = 31 * result + (int) (temp ^ (temp >>> 32))
        temp = lengthInMeters != +0.0d ? Double.doubleToLongBits(lengthInMeters) : 0L
        result = 31 * result + (int) (temp ^ (temp >>> 32))
        return result
    }
}
