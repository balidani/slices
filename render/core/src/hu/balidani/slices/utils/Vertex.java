package hu.balidani.slices.utils;

import java.io.Serializable;
import java.util.List;

public class Vertex implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public float x;
	public float y;
	public float z;

	public Vertex(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vertex(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Vertex(Vertex vertex) {
		this.x = vertex.x;
		this.y = vertex.y;
		this.z = vertex.z;
	}

	public Vertex sub(Vertex v) {
		return new Vertex(x - v.x, y - v.y, v.z - z);
	}

	public Vertex add(Vertex v) {
		return new Vertex(x + v.x, y + v.y, v.z + z);
	}

	public Vertex mul(float f) {
		return new Vertex(x * f, y * f, z * f);
	}

	/**
	 * @param v
	 * @return squared distance between this vertex and v, ignoring the z
	 *         dimension
	 */
	public float distanceTo(Vertex v) {
		return (float) (Math.pow(v.x - x, 2) + Math.pow(v.y - y, 2));
	}

	/**
	 * 
	 * @param vertices
	 *            - list of vertices to search in
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

	@Override
	public String toString() {
		return String.format("(%f %f %f)", x, y, z);
	}

	public boolean resembles(Vertex last) {
		return (Math.abs(last.x - this.x) < 0.001f)
				&& (Math.abs(last.y - this.y) < 0.001f);
	}

	public Vertex pointTo(Vertex v) {
		float x = v.x - this.x;
		float y = v.y - this.y;

		// Transform to eigenvector
		float s = (float) Math.sqrt(x * x + y * y);

		return new Vertex(x/s, y/s);
	}
}
