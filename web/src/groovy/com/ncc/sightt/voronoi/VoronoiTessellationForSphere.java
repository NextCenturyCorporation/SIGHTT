
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

import com.ncc.sightt.voronoi.quickhull3d.QuickHull3D;
import com.ncc.sightt.voronoi.quickhull3d.QuickHull3DPoint3d;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 * VoronoiTessellationForSphere is the interface between QuickHull3D code and code that we can use in SIGHTT.
 * <p/>
 * To use, do new, then call generateConvexHull, then generateTessella.
 */
public class VoronoiTessellationForSphere {
    List<Vector3f> pts;

    // These are the triangular faces that are created by QuickHull3D
    private ArrayList<int[]> faceIndices;

    // Centers of the Voronoi Tessella
    ArrayList<VoronoiVertex> vertexes;

    // The tessella themselves
    ArrayList<Tessella> tessella;

    public void generateConvexHull(List<Vector3f> pts) {
        this.pts = pts;

        // Convert our points to qts
        QuickHull3DPoint3d[] qPoints = new QuickHull3DPoint3d[pts.size()];
        int ctr = 0;
        for (Vector3f pt : pts) {
            QuickHull3DPoint3d qPt = new QuickHull3DPoint3d(pt.x, pt.y, pt.z);
            qPoints[ctr] = qPt;
            ctr++;
        }

        // Get convex hull
        QuickHull3D hull = new QuickHull3D();
        hull.build(qPoints);

        int[][] f = hull.getFaces();
        createFacesFromHullFaceVector(f);

        System.out.println("Number of points: " + pts.size());
        System.out.println("Number of faceIndices: " + faceIndices.size());
    }

    /**
     * QuickHull3D returns an int[][], which is confusing.  It's really a list of three ints
     */
    private void createFacesFromHullFaceVector(int[][] f) {
        faceIndices = new ArrayList<int[]>();
        for (int ii = 0; ii < f.length; ii++) {
            faceIndices.add(f[ii]);
        }
    }

    public void generateTessella() {
        // Get all vertexes of the tessella
        vertexes = new ArrayList<VoronoiVertex>();
        for (int ii = 0; ii < faceIndices.size(); ii++) {
            int[] facePts = faceIndices.get(ii);
            VoronoiVertex vertex = new VoronoiVertex(pts, facePts[0], facePts[1], facePts[2]);
            vertexes.add(vertex);
        }

        // Go through all the points, determine the tessella for each point
        tessella = new ArrayList<Tessella>();
        for (int ii = 0; ii < pts.size(); ii++) {
            Tessella vt = getTessellaForIndex(ii);
            if (vt != null) {
                tessella.add(vt);
            }
        }

        // Make sure that all the tessella are on the unit sphere
        for (Tessella tess : tessella)
        {
            tess.normalize();
        }
    }

    /**
     * Get the tessella that surrounds a given point, identified by index
     */
    private Tessella getTessellaForIndex(int index) {
        List<VoronoiVertex> vertexesForIndex = new ArrayList<VoronoiVertex>();

        // Go through all the vertexes until we find one that uses this point
        for (VoronoiVertex vertex : vertexes) {
            if (vertex.usesPointIndex(index)) {
                // Add this vertex to the list of vertexes for this point index
                vertexesForIndex.add(vertex);

                // Get another point index for this vertex.  We know the vertex has
                // 3 points, one of this is the point we're generating the vertex for.  Find
                // another one
                int indexForOtherCorner = vertex.getIndexForCornerButNotThisOne(index);

                // Walk around the point, adding vertexes.
                Tessella vt = getTessellaForIndexAndVertexes(index, indexForOtherCorner, vertexesForIndex);
                return vt;
            }
        }
        System.err.println("Error: Unable to find any vertexes that use point : " + index);
        return null;
    }

    /**
     * Given a point index, another point index that this is connect to, and the vertexes
     * found to date, keep adding vertexes until we wrap around
     */
    private Tessella getTessellaForIndexAndVertexes(int index, int otherIndex,
                                                    List<VoronoiVertex> vertexesForThisPoint) {
        // Get the last vertexPoint.   Index is one of the points, Get the next one
        VoronoiVertex lastVertex = vertexesForThisPoint.get(vertexesForThisPoint.size() - 1);
        VoronoiVertex nextVertex = getCommonVertexForIndexAndVertex(index, otherIndex, lastVertex);

        // If we wrap around, then we are done
        if (vertexesForThisPoint.contains(nextVertex)) {
            Tessella vt = new Tessella(index, pts.get(index), vertexesForThisPoint);
            return vt;
        }
        vertexesForThisPoint.add(nextVertex);
        int otherIndexToUse = nextVertex.getIndexForCornerButNotThese(index, otherIndex);
        return getTessellaForIndexAndVertexes(index, otherIndexToUse, vertexesForThisPoint);
    }

    private VoronoiVertex getCommonVertexForIndexAndVertex(int index, int otherIndex, VoronoiVertex lastVertex) {
        for (VoronoiVertex vertex : vertexes) {
            if (vertex == lastVertex) {
                continue;
            }
            if (vertex.usesPointIndex(index) && vertex.usesPointIndex(otherIndex)) {
                return vertex;
            }
        }
        System.err.println("Unable to find other vertexPoint that shares index " + index + " " + otherIndex + " with vertexPoint "
                + lastVertex);
        return null;
    }

    // -----------------------------------------------------------------------
    // Getters / setters
    // -----------------------------------------------------------------------

    public List<int[]> getFaces() {
        return faceIndices;
    }

    public List<Tessella> getTessella() {
        return tessella;
    }

    public List<VoronoiVertex> getVertexes() {
        return vertexes;
    }
}
