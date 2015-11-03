
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
package com.ncc.sightt.voronoi.quickhull3d;

import javax.vecmath.Vector3f;

/**
 * Class to handle various computations in 3D.
 */
public class Vector3dUtil {
    /**
     * Given vector3f a,b,c, this finds the circumcenter.
     * See: http://gamedev.stackexchange.com/questions/60630/how-do-i-find-the-circumcenter-of-a-triangle-in-3d
     * Which points to Geometry Junkyard: http://www.ics.uci.edu/~eppstein/junkyard/circumcenter.html
     * <p/>
     * <pre>
     *     Triangle in R^3:
     *
     *                |c-a|^2 [(b-a)x(c-a)]x(b-a) + |b-a|^2 (c-a)x[(b-a)x(c-a)]
     *       m = a + ---------------------------------------------------------.
     *                                2 | (b-a)x(c-a) |^2
     *
     * </pre>
     * <p/>
     * Note:  Assumes that the points are NOT colinear.   Also, note that we have to convert from C++ to Java
     * and C++ is much more concise because of operator overloading.
     */
    public static Vector3f getCircumcenter(Vector3f a, Vector3f b, Vector3f c) {
        /* In C++:
           Vector3f ac = c - a ;
           Vector3f ab = b - a ;
           Vector3f abXac = ab.cross( ac ) ;
         */
        Vector3f ab = vector3fSubtract(b, a);
        Vector3f ac = vector3fSubtract(c, a);
        Vector3f abXac = vector3fCross(ab, ac);

        /* In C++:
           Vector3f toCircumsphereCenter = (abXac.cross( ab )*ac.len2() + ac.cross( abXac )*ab.len2()) / (2.f*abXac.len2()) ;
           float circumsphereRadius = toCircumsphereCenter.len() ;
         */
        // this is the vector from a TO the circumcenter
        Vector3f part1 = vector3fCross(abXac, ab);
        part1.scale(ac.lengthSquared());
        Vector3f part2 = vector3fCross(ac, abXac);
        part2.scale(ab.lengthSquared());
        Vector3f toCircumcenter = vector3fAdd(part1, part2);
        toCircumcenter.scale(1 / (2.f * abXac.lengthSquared()));
        float circumsphereRadius = toCircumcenter.length();

        // The 3 space coords of the circumsphere center then:
        Vector3f ccs = vector3fAdd(a, toCircumcenter);
        return ccs;
    }

    /**
     * Compute a-b in 3D.  This really should be part of Vector3f ....
     */
    public static Vector3f vector3fSubtract(Vector3f a, Vector3f b) {
        Vector3f c = new Vector3f(a);
        c.x = a.x - b.x;
        c.y = a.y - b.y;
        c.z = a.z - b.z;
        return c;
    }

    /**
     * Compute a-b in 3D.  This really should be part of Vector3f ....
     */
    public static Vector3f vector3fAdd(Vector3f a, Vector3f b) {
        Vector3f c = new Vector3f(a);
        c.x = a.x + b.x;
        c.y = a.y + b.y;
        c.z = a.z + b.z;
        return c;
    }

    public static Vector3f vector3fCross(Vector3f v1, Vector3f v2) {
        float x = v1.y * v2.z - v1.z * v2.y;
        float y = v1.z * v2.x - v1.x * v2.z;
        float z = v1.x * v2.y - v1.y * v2.x;

        return new Vector3f(x, y, z);

    }

    public static float vector3fDot(Vector3f v1, Vector3f v2) {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }

}
