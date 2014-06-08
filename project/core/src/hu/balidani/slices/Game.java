package hu.balidani.slices;

import hu.balidani.slices.utils.Edge;
import hu.balidani.slices.utils.Face;
import hu.balidani.slices.utils.FacePair;
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

		String file = "mug2.g3db";

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

		sliceMax = 1.25f;
		sliceZ = sliceMax;
		sliceSpeed = 0.005f;
		
		sliceZ = 0.0f;
	}

	@Override
	public void render() {
		
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		ArrayList<FacePair> facePairs = new ArrayList<FacePair>();
		
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
				ArrayList<Edge> edgeGroup = new ArrayList<Edge>();
				face.findNeighbors(faces, sliceZ, facePairs, edgeGroup);

				if (edgeGroup == null || edgeGroup.size() == 0) {
					moveSlice();
					return;
				}
				
				// Render edge group
				ArrayList<Vertex> slice = new ArrayList<Vertex>();
				
				for (Edge edge : edgeGroup) {
					Vertex v = edge.interpolate(sliceZ);
					slice.add(v);
				}
				
				for (int i = 0; i < slice.size(); i++) {

					Vertex a = slice.get(i);
					Vertex b;
					
					if (i < slice.size() - 1) {
						b = slice.get(i + 1);
					} else {
						b = slice.get(0);
					}
					
//					shape.begin(ShapeType.Filled);
//					shape.identity();
//					shape.setColor(1, 1, 1, 1);
//					shape.rect(a.x - 0.02f, a.y - 0.02f, 0.04f, 0.04f);
//					shape.end();
//					
					shape.begin(ShapeType.Line);
					shape.identity();
					shape.setColor(1, 1, 1, 1);
					shape.line(a.x, a.y, b.x, b.y);
					shape.end();
				}
			}
		}
		
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
}