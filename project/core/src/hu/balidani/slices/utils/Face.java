package hu.balidani.slices.utils;

import java.util.ArrayList;

public class Face {

	private static float epsilon = 0.00001f;
	
	// Top: a, Middle: b, Bottom: c,
	public Vertex a;
	public Vertex b;
	public Vertex c;
	
	private int topCount;

	public Face(Vertex a, Vertex b, Vertex c) {
		
		this.a = Face.max(a, b, c);
		this.c = Face.min(a, b, c);
		this.b = Face.mid(a, b, c);
		
		if (Math.abs(a.z - c.z) < epsilon) {
			topCount = 3;
		} else if (Math.abs(a.z - b.z) < epsilon) {
			topCount = 2;
		} else {
			topCount = 1;
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

	public Vertex[] intersectedEdges(float z) {

		int aAbove = (z - a.z) > 0 ? 1 : 0;
		int bAbove = (z - b.z) > 0 ? 1 : 0;
		int cAbove = (z - c.z) > 0 ? 1 : 0;
		
		// TODO
		return null;
	}
	
	/**
	 * 
	 * @param faces - list of all faces
	 * @param z - the z coordinate of the slice
	 * @return - list of neighboring faces
	 */
	public ArrayList<Face> findNeighbors(ArrayList<Face> faces, float z) {

		ArrayList<Face> results = new ArrayList<Face>();
		results.add(this);
		
		findNeighbors(faces, z, results);
		return results;
	}
	
	private void findNeighbors(ArrayList<Face> faces, float z, ArrayList<Face> results) {
		
		for (Face face : faces) {
			if (results.contains(face)) {
				continue;
			}
			
			if (this.neighborOf(face) && face.intersectsZ(z)) {
				
				results.add(face);
				face.findNeighbors(faces, z, results);
			}
		}
	}
	
	/**
	 * 
	 * @param i - the other face to check
	 * @return true if the two faces are neighbors
	 */
	public boolean neighborOf(Face i) {
		
		int matchingIds = 
			(i.contains(a) ? 1 : 0) + 
			(i.contains(b) ? 1 : 0) +
			(i.contains(c) ? 1 : 0);

		return matchingIds > 0;
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
