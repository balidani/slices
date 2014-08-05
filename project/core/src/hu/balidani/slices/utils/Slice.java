package hu.balidani.slices.utils;

import java.util.ArrayList;

public class Slice {

	public ArrayList<Triangle> mesh;
	public ArrayList<Slice> children;
	
	public Slice(ArrayList<Triangle> mesh) {
		this.mesh = mesh;
		
		children = new ArrayList<Slice>(); 
	}
}
