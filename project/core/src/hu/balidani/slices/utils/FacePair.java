package hu.balidani.slices.utils;

public class FacePair {

	public Face a;
	public Face b;
	
	public FacePair(Face a, Face b) {
		
		// Always ordered
		if (a.id < b.id) {
			this.a = a;
			this.b = b;
		} else {
			this.a = b;
			this.b = a;
		}
	}

	public boolean contains(Face face) {
		return a.equals(face) || b.equals(face);
	}
	
	@Override
	public boolean equals(Object obj) {
		FacePair other = (FacePair) obj;
		
		return other.a.equals(a) && other.b.equals(b);
	}

	@Override
	public String toString() {
		return String.format("%s %s", a, b);
	}
}
