package hu.balidani.slices.utils;

import java.util.ArrayList;

public class Face {
	
	// Top: a, Middle: b, Bottom: c,
	public Vertex a;
	public Vertex b;
	public Vertex c;

	public Face(Vertex a, Vertex b, Vertex c) {
		
		this.a = Face.max(a, b, c);
		this.b = Face.mid(a, b, c);
		this.c = Face.min(a, b, c);
		
		if (Math.abs(a.z - c.z) < 0.00001f) {
			// This face is flat!
		}
	}

	/**
	 * 
	 * @param z - the z coordinate of the slice to check
	 * @return true if the face intersects the plane with given z
	 */
	public boolean intersectsZ(float z) {
		
		int aboveCount = verticesAboveZ(z);
		return aboveCount > 0 && aboveCount < 3;
	}
	
	private int verticesAboveZ(float z) {

		int aAbove = (z - a.z) > 0 ? 1 : 0;
		int bAbove = (z - b.z) > 0 ? 1 : 0;
		int cAbove = (z - c.z) > 0 ? 1 : 0;
		
		return aAbove + bAbove + cAbove;
	}
	
	public void findNeighbors(ArrayList<Face> faces, float z, ArrayList<Face> facesSeen, ArrayList<Edge> results) {
		
		for (Face face : faces) {
			if (facesSeen.contains(face)) {
				continue;
			}
			
			Edge edge = this.commonEdge(face);
			if (edge != null && face.intersectsZ(z)) {
				
				facesSeen.add(face);
				results.add(edge);
				face.findNeighbors(faces, z, facesSeen, results);
			}
		}
	}

	public Edge commonEdge(Face f) {
		
		Vertex v1 = null;
		Vertex v2 = null;
		
		if (f.contains(a)) {
			v1 = a;
		} else if (f.contains(b)) {
			v1 = b;
		} else {
			return null;
		}
		
		if (f.contains(c)) {
			v2 = c;
		} else if (f.contains(b) && !v1.equals(b)) {
			v2 = b;
		} else {
			return null;
		}
		
		// v1 is always supposed to be the greater value
		// which means it's closer to the bottom (z=1)
		return new Edge(v1, v2);
	}
	
	/**
	 * 
	 * @param v - the vertex to check
	 * @return true if the face contains the given vertex
	 */
	public boolean contains(Vertex v) {
		return (a.equals(v) || b.equals(v) || c.equals(v));
	}
	
	@Override
	public String toString() {
		return String.format("%s %s %s", a, b, c);
	}
	
	public static Vertex min(Vertex... vertices) {
		Vertex min = vertices[0];
		for (Vertex vertex : vertices) {
			if (vertex.z < min.z){
				min = vertex;
			}
		}
		
		return min;
	}
	
	public static Vertex max(Vertex... vertices) {
		Vertex max = vertices[0];
		for (Vertex vertex : vertices) {
			if (vertex.z > max.z){
				max = vertex;
			}
		}
		
		return max;
	}

	private static Vertex mid(Vertex... vertices) {

		Vertex max = Face.max(vertices);
		Vertex min = Face.min(vertices);
		
		for (Vertex vertex : vertices) {
			if (vertex.equals(max)) {
				continue;
			}
			
			if (vertex.equals(min)) {
				continue;
			}
			
			return vertex;
		}
		
		return null;
	}
	
}
