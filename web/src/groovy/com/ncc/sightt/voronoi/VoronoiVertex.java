
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
package com.ncc.sightt.voronoi;

import com.ncc.sightt.voronoi.quickhull3d.Vector3dUtil;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 * The Voronoi Vertex is the circumcenter of three triangulation points.  It is a member of 3 Voronoi Tessella,
 * through 3 connections with other Voronoi Vertexes
 */
public class VoronoiVertex {
    // 3D coordinates of this vertex point
    Vector3f vertexPoint;

    // The Tessella (3 of them) that this vertex point is a corner point of
    List<Tessella> tess;

    // These are the <b>index</b> values for the original points from which this vertex was constructed
    int p1;
    int p2;
    int p3;

    public VoronoiVertex(List<Vector3f> pts, int p1, int p2, int p3) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;

        Vector3f pt1 = pts.get(p1);
        Vector3f pt2 = pts.get(p2);
        Vector3f pt3 = pts.get(p3);

        vertexPoint = Vector3dUtil.getCircumcenter(pt1, pt2, pt3);

        tess = new ArrayList<Tessella>();
    }

    public Vector3f getVertexPoint() {
        return vertexPoint;
    }

    public boolean usesPointIndex(int ii) {
        if (p1 == ii || p2 == ii || p3 == ii) {
            return true;
        }
        return false;
    }

    public void addTessella(Tessella tessella) {
        tess.add(tessella);
    }

    /**
     * Give back an index (p1, p2, p3), but not the passed one
     */
    public int getIndexForCornerButNotThisOne(int index) {
        if (p1 != index) {
            return p1;
        }
        if (p2 != index) {
            return p2;
        }
        if (p3 != index) {
            return p3;
        }

        System.err.println("Problem.  Asked vertexPoint for index, but not " + index + " but only indicies we have are: "
                + p1 + " " + p2 + " " + p3);
        return p1;
    }

    /**
     * Give back an index (p1, p2, p3), but not the passed one
     */
    public int getIndexForCornerButNotThese(int index, int index2) {
        if (p1 != index && p1 != index2) {
            return p1;
        }
        if (p2 != index && p2 != index2) {
            return p2;
        }
        if (p3 != index && p3 != index2) {
            return p3;
        }

        System.err.println("Problem.  Asked vertexPoint for index, but not " + index + " or " + index2 +
                " but only indicies we have are: " + p1 + " " + p2 + " " + p3);
        return p1;
    }

    public void normalize()
    {
        vertexPoint.normalize();
    }
}
