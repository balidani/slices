package hu.balidani.slices.utils;

public class Line {

	public Vertex a;
	public Vertex b;

	public Line(Vertex a, Vertex b) {
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

		return new Vertex(a.x + ratio * (b.x - a.x), a.y + ratio * (b.y - a.y));
	}

	@Override
	public String toString() {
		return String.format("%s, %s", a, b);
	}

	public boolean intersects(Line e, boolean forgiving) {

		// Find the intersection of the lines
		// http://en.wikipedia.org/wiki/Line-line_intersection
		float q = ((a.x - b.x) * (e.a.y - e.b.y) - (a.y - b.y)
				* (e.a.x - e.b.x));

		// Epsilon
		float p = 0.00001f;
		
		// Lines are parallel if q == 0
		if (Math.abs(q) < p) {
			return false;
		}

		float x = ((a.x * b.y - a.y * b.x) * (e.a.x - e.b.x) - (a.x - b.x)
				* (e.a.x * e.b.y - e.a.y * e.b.x))
				/ q;
		float y = ((a.x * b.y - a.y * b.x) * (e.a.y - e.b.y) - (a.y - b.y)
				* (e.a.x * e.b.y - e.a.y * e.b.x))
				/ q;
		
		// Check if the intersection is inside the sections' bounds
		
		if (!forgiving) {
			p = -p;
		}
		
		boolean inThis = ((a.x >= x - p && b.x <= x + p) || (a.x <= x + p && b.x >= x - p))
				&& ((a.y >= y - p && b.y <= y + p) || (a.y <= y + p && b.y >= y - p));
		boolean inE = ((e.a.x >= x - p && e.b.x <= x + p) || (e.a.x <= x + p && e.b.x >= x - p))
				&& ((e.a.y >= y - p && e.b.y <= y + p) || (e.a.y <= y + p && e.b.y >= y - p));

		return inThis && inE;
	}

	public Vertex center() {
		float x = (a.x + b.x) / 2f;
		float y = (a.y + b.y) / 2f;
		
		return new Vertex(x, y);
	}

}
