package hu.balidani.slices.utils;

import java.io.Serializable;

public class Triangle extends Face implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public boolean isHollow;
	
	public Triangle(Vertex a, Vertex b, Vertex c, boolean isHollow) {
		super(a, b, c);
		
		this.isHollow = isHollow;
	}

}
