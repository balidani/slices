package hu.balidani.slices;

import java.util.ArrayList;
import java.util.List;

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

		// Filter for a slice
		ArrayList<Vector3> sliceVertices = new ArrayList<Vector3>();

		for (Vector3 vertex : vertices) {
			if (Math.abs(sliceZ - vertex.z) < epsilonZ) {
				sliceVertices.add(vertex);
			}
		}

		if (sliceVertices.size() >= 6) {

			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

			shape.begin(ShapeType.Filled);
			shape.identity();
			shape.setColor(1, 1, 1, 1);

			/*
			 * Triangulating algorithm test Slow, dumb implementation, obviously
			 */

			// Select a starting point
			Vector3 vertexA = sliceVertices.remove(0);

			// Select the closest point to it
			Vector3 vertexB = closestPoint(sliceVertices, vertexA);

			// Go through all remaining points
			while (sliceVertices.size() > 1) {

				// Select a point C, such that (A, B, C) has the shortest perimeter
				Vector3 vertexC = shortestPerimeter(sliceVertices, vertexA, vertexB);
				
				if (vertexC == null) {
					System.out.println(sliceVertices.size());
				} else {
					triangle(shape, vertexA, vertexB, vertexC);
				}
				
				// Shift the vertices
				vertexA = vertexB;
				vertexB = vertexC;
				sliceVertices.remove(vertexA);
			}

			shape.end();

		}

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

	/**
	 * @param a
	 * @param b
	 * @return squared distance between a and b, ignoring z dimension
	 */
	public float distance(Vector3 a, Vector3 b) {
		return (float) (Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
	}

	public Vector3 closestPoint(List<Vector3> vertices, Vector3 vertex) {

		float bestDistance = Float.MAX_VALUE;
		Vector3 bestVertex = null;

		for (Vector3 otherVertex : vertices) {
			if (otherVertex.equals(vertex)) {
				continue;
			}

			// Compute 2d distance instead of built-in 3d
			float dist = distance(vertex, otherVertex);

			if (dist < bestDistance) {
				bestDistance = dist;
				bestVertex = otherVertex;
			}
		}

		return bestVertex;
	}

	public Vector3 shortestPerimeter(List<Vector3> vertices, Vector3 a,
			Vector3 b) {

		float bestPerimeter = Float.MAX_VALUE;
		float origDistance = distance(a, b);
		Vector3 bestVertex = null;

		for (Vector3 otherVertex : vertices) {
			if (otherVertex.equals(a) || otherVertex.equals(b)) {
				continue;
			}

			// Compute perimeter
			float perimeter = origDistance + distance(b, otherVertex)
					+ distance(otherVertex, a);
			
			if (perimeter < bestPerimeter) {
				bestPerimeter = perimeter;
				bestVertex = otherVertex;
			}
		}

		return bestVertex;
	}
}