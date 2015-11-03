
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
import java.util.Collections;
import java.util.List;

/**
 * A Tessella is a 'face' of the overall Voronoi diagram.  It consists of some number
 * of Voronoi Centers, listed in order.
 */
public class Tessella
{

    // The index of the point that the tessellation is around
    int index;

    // This is the point that the tesselation is around
    Vector3f point;

    // These are the Delaunay circumcenter points
    List<VoronoiVertex> vertexes;

    public Tessella(int index, Vector3f point, List<VoronoiVertex> vertexes)
    {
        this.index = index;
        this.point = point;
        this.vertexes = vertexes;

        // The vertexes should be in counter-clockwise order
        orderVertexes();

        for (VoronoiVertex vertex : vertexes)
        {
            vertex.addTessella(this);
        }
    }

    /**
     * The vertexes should be in 'order', but we don't know if they are in CW or CCW order.  Make
     * them all in CCW order.   If C is the center, and A and B are points on the edge, then
     * compute n dot ( (A-C) x (B-C) ).  This will be > 0 for CW, and < 0 for CCW.
     */
    private void orderVertexes()
    {
        // Use the center point as a normal
        Vector3f normal = point;

        // Determine if the first 2 points of the face are in CW or CCW order
        Vector3f AC = Vector3dUtil.vector3fSubtract(vertexes.get(0).getVertexPoint(), point);
        Vector3f BC = Vector3dUtil.vector3fSubtract(vertexes.get(1).getVertexPoint(), point);
        Vector3f ACxBC = Vector3dUtil.vector3fCross(AC, BC);
        float dot = Vector3dUtil.vector3fDot(normal, ACxBC);

        if (dot > 0)
        {
            // then CCW, do not do anything
            return;
        }

        // Change the order of the vertexes.
        Collections.reverse(vertexes);
    }

    /**
     * Return the point that the tesselation is around
     */
    public Vector3f getPoint()
    {
        return point;
    }

    public int getIndex()
    {
        return index;
    }

    /**
     * Return the ordered vertexes that make up this tessella
     */
    public List<VoronoiVertex> getVertexes()
    {
        return vertexes;
    }

    public void normalize()
    {
        for (VoronoiVertex vertex : vertexes)
        {
            vertex.normalize();
        }

    }
}
