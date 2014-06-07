package hu.balidani.slices.utils;

public class Edge {

	public Vertex a;
	public Vertex b;
	
	public Edge(Vertex a, Vertex b) {
		this.a = a;
		this.b = b;
	}
	
	/**
	 * 
	 * Assumes that a is closer to the bottom than b
	 * 
	 * @param sliceZ
	 * @return
	 */
	public Vertex interpolate(float sliceZ) {
		
		float diff = Math.abs(a.z - b.z);
		float ratio = Math.abs(sliceZ - a.z) / diff;
		
		return new Vertex(a.x + ratio * (b.x - a.x), a.y + ratio * (b.y - a.y), sliceZ);
	}
	
	@Override
	public String toString() {
		return String.format("%s, %s", a, b); 
	}
	
}
