package hu.balidani.slices.utils;

public class Face {

	public short a;
	public short b;
	public short c;

	public Face(short a, short b, short c) {
		this.a = a;
		this.b = b;
		this.c = c;
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

	private boolean contains(int id) {
		return (a == id || b == id || c == id);
	}

}
