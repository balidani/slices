package hu.balidani.slices;

import hu.balidani.slices.utils.Face;
import hu.balidani.slices.utils.FacePair;
import hu.balidani.slices.utils.Line;
import hu.balidani.slices.utils.Vertex;

import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class Game extends ApplicationAdapter {

	AssetManager assets;
	ShapeRenderer shape;
	OrthographicCamera camera;

	ArrayList<Vertex> vertices;
	ArrayList<Face> faces;

	float sliceZ, sliceMin, sliceMax, sliceSpeed;

	@Override
	public void create() {

		String file = "mug.g3db";

		// Load assets
		assets = new AssetManager();
		assets.load(file, Model.class);
		assets.finishLoading();

		// Load mesh
		Model glass = assets.get(file, Model.class);
		Mesh mesh = glass.meshes.get(0);
		
		// Get vertices
		int vertexSize = mesh.getNumVertices();
		float[] vertexBuffer = new float[vertexSize * 3];
		mesh.getVertices(vertexBuffer);

		// Convert vertices
		vertices = new ArrayList<Vertex>();

		for (int i = 0; i < vertexSize; ++i) {
			float x = vertexBuffer[i * 3 + 0];
			float y = vertexBuffer[i * 3 + 1];
			float z = vertexBuffer[i * 3 + 2];

			// Order intentional, due to 3d-2d conversion later on
			vertices.add(new Vertex(x, z, y));
		}
		
		// Get indices
		int indexSize = mesh.getNumIndices();
		short[] indexBuffer = new short[indexSize];
		mesh.getIndices(indexBuffer);
		
		// Convert indices
		faces = new ArrayList<Face>();
		
		for (int i = 0; i < indexSize / 3; ++i) {
			short a = indexBuffer[i * 3 + 0];
			short b = indexBuffer[i * 3 + 1];
			short c = indexBuffer[i * 3 + 2];

			Vertex vertexA = vertices.get(a);
			Vertex vertexB = vertices.get(b);
			Vertex vertexC = vertices.get(c);
			
			// Skip flat faces
			if (Math.abs(vertexA.z - vertexB.z) < 0.001f && Math.abs(vertexB.z - vertexC.z) < 0.001f) {
				continue;
			}
			
			// Order intentional, due to 3d to 2d conversion later on
			faces.add(new Face(i, vertexA, vertexB, vertexC));
		}

		System.out.println("Vertex size: " + vertices.size());
		System.out.println("Index size: " + faces.size());

		// Set up the camera and shape renderer
		camera = new OrthographicCamera(3f, 3f);
		camera.translate(0f, 0f);
		camera.update();

		shape = new ShapeRenderer();
		shape.setProjectionMatrix(camera.combined);

		sliceMax = 1.0f;
		sliceZ = sliceMax;
		sliceSpeed = 0.01f;
	}

	@Override
	public void render() {
		
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		ArrayList<FacePair> facePairs = new ArrayList<FacePair>();
		ArrayList<ArrayList<Vertex>> slices = new ArrayList<ArrayList<Vertex>>();
		
		for (Face face : faces) {
			
			// Check if face is contained by any seen pair
			// This should be much more efficient
			boolean faceSeen = false;
			for (FacePair facePair : facePairs) {
				if (facePair.contains(face)) {
					faceSeen = true;
					break;
				}
			}
			if (faceSeen) {
				continue;
			}
			
			if (face.intersectsZ(sliceZ)) {
				ArrayList<Line> edgeGroup = new ArrayList<Line>();
				face.findNeighbors(faces, sliceZ, facePairs, edgeGroup);

				if (edgeGroup == null || edgeGroup.size() == 0) {
					moveSlice();
					return;
				}
				
				// Process edge group into a slice
				ArrayList<Vertex> slice = new ArrayList<Vertex>();
				
				for (Line edge : edgeGroup) {
					Vertex v = edge.interpolate(sliceZ);
					
					// Check for duplicate
					if (!slice.isEmpty()) {
						Vertex last = slice.get(slice.size() - 1);
						if (v.resembles(last)) {
							continue;
						}
					}
					
					// Optimization (and a bug fix):
					// If the new vertex is the continuation of the previous two, 
					// simply remove the last element before adding the new
					
					if (slice.size() > 1) {
						// Get the last two vertices
						Vertex last1 = slice.get(slice.size() - 1);
						Vertex last2 = slice.get(slice.size() - 2);
						
						if (last2.pointTo(last1).resembles(last2.pointTo(v))) {
							slice.remove(slice.size() - 1);
						}
					}
					
					slice.add(v);
				}
				
				// Final step of vertex optimization for between the beginning and end of the slice
				if (slice.size() > 2) {
					
					Vertex last1 = slice.get(slice.size() - 1);
					Vertex first1 = slice.get(0);
					Vertex first2 = slice.get(1);
					
					if (first2.pointTo(last1).resembles(first2.pointTo(first1))) {
						slice.remove(first1);
					}
				}

				slices.add(slice);
			}
		}
		
		// Determine hollowness
		ArrayList<Boolean> hollowness = new ArrayList<Boolean>();

		for (ArrayList<Vertex> slice : slices) {

			// Determine if the slice is a "hollow" part or not
			boolean hollow = isHollow(slice, slices);
			hollowness.add(hollow);
		}

		// Render the non-hollow slices first
		for (int i = 0; i < slices.size(); ++i) {
			if (!hollowness.get(i)) {
				earClipping(slices.get(i), false);
			}
		}

		// Render the hollow slices after that
		for (int i = 0; i < slices.size(); ++i) {
			if (hollowness.get(i)) {
				earClipping(slices.get(i), true);
			}
		}
		
		// Move the slice
		moveSlice();
	}

	@Override
	public void dispose() {
		assets.dispose();
	}

	/*
	 * Utility methods that will eventually be organized
	 */

	public void moveSlice() {
		
		sliceZ -= sliceSpeed;
		if (sliceZ < -sliceMax) {
			sliceZ = sliceMax;
		}
	}
	
	public void earClipping(ArrayList<Vertex> slice, boolean hollow) {

		ArrayList<Vertex> originalSlice = new ArrayList<Vertex>();
		
		for (Vertex vertex : slice) {
			originalSlice.add(new Vertex(vertex));
		}
		
		while (slice.size() > 2) {
			
			Vertex ear = null;
			boolean foundEar = false;
			
			for (int i = 0; i < slice.size(); ++i) {
	
				Vertex before = i > 0 ? slice.get(i - 1) : slice.get(slice.size() - 1);
				ear = slice.get(i);
				Vertex after = i < slice.size() - 1 ? slice.get(i + 1) : slice.get(0);
				
				// Check if before -- after is a real diagonal
				Line diagonal = new Line(before, after);
				boolean isDiagonal = true;
				
				// To do that, we have to see if it intersects any of the edges of our _original_ slice
				for (int j = 0; j < originalSlice.size(); ++j) {
					Vertex a = originalSlice.get(j);
					Vertex b = j < originalSlice.size() - 1 ? originalSlice.get(j + 1) : originalSlice.get(0);
					
					Line edge = new Line(a, b);
					
					if (diagonal.intersects(edge, false)) {
						isDiagonal = false;
						break;
					}
				}
	
				if (!isDiagonal) {
					// Intersects other edges,  not a diagonal
					continue; 
				}
				
				// If the line is a real diagonal, we have to check if it's inside the slice or not
				// To do that, we have to launch a line from a point to infinity, and count the times it intersects the slice
				
				// Take the center of the diagonal
				Vertex center = diagonal.center();
				Vertex infinity = new Vertex(3e5f, 7e5f);
				Line check = new Line(center, infinity);
				int intersectionCount = 0;
				
				for (int j = 0; j < originalSlice.size(); ++j) {
					Vertex a = originalSlice.get(j);
					Vertex b = j < originalSlice.size() - 1 ? originalSlice.get(j + 1) : originalSlice.get(0);
	
					Line edge = new Line(a, b);
					
					if (check.intersects(edge, true)) {
						intersectionCount++;
					}
				}
				
				if (intersectionCount % 2 == 0) {
					// Not a real diagonal, it's outside of the slice
					continue;
				}
				
				// Found an ear, render and cut
				
				shape.begin(ShapeType.Filled);
				shape.identity();
				
				if (hollow) {
					shape.setColor(0, 0, 0, 1);
				} else {
					shape.setColor(0, 0, 1, 1);
				}
				
				shape.triangle(before.x, before.y, ear.x, ear.y, after.x, after.y);
				shape.end();

//				shape.begin(ShapeType.Line);
//				shape.identity();
//				shape.setColor(1, 1, 1, 1);
//				shape.line(before.x, before.y, ear.x, ear.y);
//				shape.line(ear.x, ear.y, after.x, after.y);
//				shape.line(before.x, before.y, after.x, after.y);
//				shape.end();
				
				foundEar = true;
				break;
			}
			
			if (foundEar) {
				slice.remove(ear);
			} else {
				System.out.println("No ear found, " + sliceZ);
				break;
			}
		}
	}

	private boolean isHollow(ArrayList<Vertex> slice,
			ArrayList<ArrayList<Vertex>> slices) {
		
		Vertex start = slice.get(0);
		Vertex infinity = new Vertex(3e5f, 7e5f);
		Line check = new Line(start, infinity);
		
		int intersectionCount = 0;
		
		// This is N^2 complexity, and it must not be done at runtime in the future
		for (ArrayList<Vertex> otherSlice : slices) {
			if (otherSlice.equals(slice)) {
				continue;
			}
			
			for (int j = 0; j < otherSlice.size(); ++j) {
				Vertex a = otherSlice.get(j);
				Vertex b = j < otherSlice.size() - 1 ? otherSlice.get(j + 1) : otherSlice.get(0);

				Line edge = new Line(a, b);
				
				if (check.intersects(edge, true)) {
					intersectionCount++;
				}
			}
		}
		
		return (intersectionCount % 2 != 0);
	}
	
	public void renderOutline(ArrayList<Vertex> slice) {
		// Draw the frame for debugging
		for (int i = 0; i < slice.size(); i++) {

			Vertex a = slice.get(i);
			Vertex b;
			
			if (i < slice.size() - 1) {
				b = slice.get(i + 1);
			} else {
				b = slice.get(0);
			}
			
			shape.begin(ShapeType.Filled);
			shape.identity();
			shape.setColor(0, 1, 0, 1);
			shape.rect(a.x - 0.01f, a.y - 0.01f, 0.02f, 0.02f);
			shape.end();

			shape.begin(ShapeType.Line);
			shape.identity();
			shape.setColor(1, 1, 1, 1);
			shape.line(a.x, a.y, b.x, b.y);
			shape.end();
		}
	}
}