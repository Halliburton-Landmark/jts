package com.vividsolutions.jtstest.testbuilder.topostretch;

import java.util.*;
import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.*;

class StretchedVertexFinder 
{
	public static List findNear(Collection linestrings, double tolerance)
	{
		StretchedVertexFinder finder = new StretchedVertexFinder(linestrings, tolerance);
		return finder.getNearVertices();
	}
	
	private Collection linestrings;
	private double tolerance = 0.0;
	private Envelope limitEnv;
	private List nearVerts = new ArrayList();
	
	public StretchedVertexFinder(Collection linestrings, double tolerance)
	{
		this.linestrings = linestrings;
		this.tolerance = tolerance;
	}
	
	public StretchedVertexFinder(Collection linestrings, double tolerance, Envelope limitEnv)
	{
		this.linestrings = linestrings;
		this.tolerance = tolerance;
	}
	
	public List getNearVertices()
	{
		findNearVertices();
		return nearVerts;
	}
	
	private void findNearVertices()
	{
		for (Iterator i = linestrings.iterator(); i.hasNext(); ) {
			LineString line = (LineString) i.next();
			findNearVertices(line);
		}
		
	}
	
	private void findNearVertices(LineString targetLine)
	{
		Coordinate[] pts = targetLine.getCoordinates();
		for (int i = 0; i < pts.length; i++) {
			findNearVertex(pts, i);
//				nearVerts.add(new NearVertex(pts, i));
		}
	}
	
	private void findNearVertex(Coordinate[] linePts, int index)
	{
		for (Iterator i = linestrings.iterator(); i.hasNext(); ) {
			LineString testLine = (LineString) i.next();
			findNearVertex(linePts, index, testLine);
		}
	}

  /**
   * Finds a single near vertex.
   * This is simply the first one found, not necessarily 
   * the nearest.  
   * This choice may sub-optimal, resulting 
   * in odd result geometry.
   * It's not clear that this can be done better, however.
   * If there are several near points, the stretched
   * geometry is likely to be distorted anyway.
   * 
   * @param targetPts
   * @param index
   * @param testLine
   */
	private void findNearVertex(Coordinate[] targetPts, int index, LineString testLine)
	{
    Coordinate targetPt = targetPts[index];
		Coordinate[] testPts = testLine.getCoordinates();
		for (int i = 0; i < testPts.length; i++) {
			Coordinate testPt = testPts[i];
			StretchedVertex stretchVert = null;
	
			// is near to vertex?
			double dist = testPt.distance(targetPt);
			if (dist <= tolerance && dist != 0.0) {
				stretchVert = new StretchedVertex(targetPt, targetPts, index, testPt, testPts, i);
			}
      // is near segment?
			else if (i < testPts.length - 1) {
				Coordinate segEndPt = testPts[i + 1];
				
				/**
				 * Check whether pt is near or equal to other segment endpoint.
				 * If near, it will be handled by the near vertex case code.
				 * If equal, don't record it at all
				 */
				double distToOther = segEndPt.distance(targetPt);
				if (distToOther <= tolerance)
					// will be handled as a point-vertex case
					continue;
				
				// here we know point is not near the segment endpoints
				// check if it is near the segment at all
				double segDist = distanceToSeg(targetPt, testPt, segEndPt);
				if (segDist <= tolerance && segDist != 0.0) {
					stretchVert = new StretchedVertex(targetPt, targetPts, i, new LineSegment(testPt, testPts[i + 1]));
				}
			}
			if (stretchVert != null)
				nearVerts.add(stretchVert);
		}
	}
	
	private static LineSegment distSeg = new LineSegment();
	
	private static double distanceToSeg(Coordinate p, Coordinate p0, Coordinate p1)
	{
		distSeg.p0 = p0;
		distSeg.p1 = p1;
		double segDist = distSeg.distance(p);
		
		// robust calculation of zero distance
		if (CGAlgorithms.computeOrientation(p0, p1, p) == CGAlgorithms.COLLINEAR)
			segDist = 0.0;
		
		return segDist;
	}
}
