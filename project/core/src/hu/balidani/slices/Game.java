package hu.balidani.slices;

import hu.balidani.slices.utils.Face;
import hu.balidani.slices.utils.Vertex;

import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

public class Game extends ApplicationAdapter {

	AssetManager assets;
	ShapeRenderer shape;
	OrthographicCamera camera;

	ArrayList<Vertex> vertices;
	ArrayList<Face> faces;

	float sliceZ, sliceMin, sliceMax, sliceSpeed;
	float epsilonZ;

	@Override
	public void create() {

		String file = "glass2.g3db";

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

			// Order intentional, due to 3d to 2d conversion later on
			vertices.add(new Vertex(x, z, y));
		}
		
		// Get indices
		int indexSize = mesh.getNumIndices();
		short[] indexBuffer = new short[indexSize * 3];
		mesh.getIndices(indexBuffer);
		
		// Convert indices
		faces = new ArrayList<Face>();
		
		for (int i = 0; i < indexSize; ++i) {
			short a = indexBuffer[i * 3 + 0];
			short b = indexBuffer[i * 3 + 1];
			short c = indexBuffer[i * 3 + 2];

			// Order intentional, due to 3d to 2d conversion later on
			faces.add(new Face(a, b, c));
		}

		System.out.println("Vertex size: " + vertexSize);
		System.out.println("Index size: " + indexSize);

		// Set up the camera and shape renderer
		camera = new OrthographicCamera(2f, 2f);
		camera.translate(0f, 0f);
		camera.update();

		shape = new ShapeRenderer();
		shape.setProjectionMatrix(camera.combined);

		sliceMax = 1.5f;
		sliceZ = sliceMax;
		epsilonZ = 0.01f;
		sliceSpeed = 0.005f;
	}

	@Override
	public void render() {

//		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
//		shape.begin(ShapeType.Filled);
//		shape.identity();
//		shape.setColor(1, 1, 1, 1);
//		shape.end();
		
		sliceZ -= sliceSpeed;
		if (sliceZ < -sliceMax) {
			sliceZ = sliceMax;
		}

	}

	@Override
	public void dispose() {
		assets.dispose();
	}

	/*
	 * Utility methods that will eventually be organized
	 */

	public void triangle(ShapeRenderer shape, Vector3 a, Vector3 b, Vector3 c) {
		shape.triangle(a.x, a.y, b.x, b.y, c.x, c.y);
	}
}