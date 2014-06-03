package hu.balidani.slices;

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
import com.badlogic.gdx.math.Vector3;

public class Game extends ApplicationAdapter {

	AssetManager assets;
	ShapeRenderer shape;
	OrthographicCamera camera;

	ArrayList<Vector3> vertices;
	float[] points;

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

		// Convert vertices to vectors
		vertices = new ArrayList<Vector3>();

		for (int i = 0; i < vertexSize; ++i) {
			float x = vertexBuffer[i * 3 + 0];
			float y = vertexBuffer[i * 3 + 1];
			float z = vertexBuffer[i * 3 + 2];

			// Order intentional, due to 3d to 2d conversion later on
			vertices.add(new Vector3(x, z, y));
		}

		System.out.println("Vertex size: " + vertexSize);

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

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		// Filter for a slice
		ArrayList<Vector3> sliceVertices = new ArrayList<Vector3>();

		for (Vector3 vertex : vertices) {
			if (Math.abs(sliceZ - vertex.z) < epsilonZ) {
				sliceVertices.add(vertex);
			}
		}

		// Convert list to float array

		if (sliceVertices.size() >= 6) {

			points = new float[sliceVertices.size() * 2];
			int pointCount = 0;
			
			/*
			 * Fancy drawing algorithm 
			 * Slow, dumb implementation, obviously
			 */

			// Select a starting point
			Vector3 currentVertex = sliceVertices.remove(0);

			points[pointCount++] = currentVertex.x;
			points[pointCount++] = currentVertex.y;
			// System.out.printf("Start: %f %f\n", points[0], points[1]);

			// Go through all remaining points
			while (!sliceVertices.isEmpty()) {

				Vector3 closestVertex = null;
				float bestDistance = Float.MAX_VALUE;

				// Find closest point
				for (Vector3 otherVertex : sliceVertices) {
					
					if (otherVertex.equals(currentVertex)) {
						continue;
					}

					// Compute 2d distance instead of built-in 3d
					float distance = (float) (Math.pow(currentVertex.x - otherVertex.x,
							2) + Math.pow(currentVertex.y - otherVertex.y, 2));

					if (distance < bestDistance) {
						bestDistance = distance;
						closestVertex = otherVertex;
					}
				}

				points[pointCount++] = closestVertex.x;
				points[pointCount++] = closestVertex.y;

				currentVertex = closestVertex;
				sliceVertices.remove(currentVertex);
				// System.out.printf("[%d]: %f %f\n", i, closestVertex.x, closestVertex.y);
			}

		}

		if (points != null) {
			
			shape.begin(ShapeType.Line);
			shape.identity();
			shape.setColor(1, 1, 1, 1);
			shape.polygon(points);
			shape.end();
		}

		sliceZ -= sliceSpeed;
		if (sliceZ < -sliceMax) {
			sliceZ = sliceMax;
			points = null;
		}
		
//		shape.begin(ShapeType.Filled);
//		shape.identity();
//		shape.setColor(1, 1, 1, 1);
//		shape.triangle(-0.5f, 0.0f, 0.0f, 0.5f, 0.5f, 0.0f);
//		shape.end();
	}

	@Override
	public void dispose() {
		assets.dispose();
	}
}