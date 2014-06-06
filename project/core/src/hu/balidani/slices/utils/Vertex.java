package hu.balidani.slices.utils;

import java.util.List;

public class Vertex {

	public float x;
	public float y;
	public float z;
	
	public Vertex(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * @param v
	 * @return squared distance between this vertex and v, ignoring the z dimension
	 */
	public float distanceTo(Vertex v) {
		return (float) (Math.pow(v.x - x, 2) + Math.pow(v.y - y, 2));
	}

	/**
	 * 
	 * @param vertices - list of vertices to search in
	 * @return the closest point to this vertex
	 */
	public Vertex closestPoint(List<Vertex> vertices) {

		float bestDistance = Float.MAX_VALUE;
		Vertex bestVertex = null;

		for (Vertex otherVertex : vertices) {
			if (otherVertex.equals(this)) {
				continue;
			}

			// Compute 2d distance instead of built-in 3d
			float dist = this.distanceTo(otherVertex);

			if (dist < bestDistance) {
				bestDistance = dist;
				bestVertex = otherVertex;
			}
		}

		return bestVertex;
	}
}
