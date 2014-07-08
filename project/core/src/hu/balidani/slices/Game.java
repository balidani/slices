package hu.balidani.slices;

import hu.balidani.slices.utils.Triangle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class Game extends ApplicationAdapter {

	ShapeRenderer shape;
	OrthographicCamera camera;

	ArrayList<ArrayList<Triangle>> allTriangles;

	float time, speed;

	@SuppressWarnings("unchecked")
	@Override
	public void create() {

		String file = "orange_big.slc";

		try {
			FileHandle handle = Gdx.files.internal(file);
			ObjectInputStream ois = new ObjectInputStream(handle.read());
			allTriangles = (ArrayList<ArrayList<Triangle>>) ois.readObject();

			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}

		System.out.println("Slice count: " + allTriangles.size());

		// Set up the camera and shape renderer
		camera = new OrthographicCamera(3f, 3f);
		camera.translate(0f, 0f);
		camera.update();

		shape = new ShapeRenderer();
		shape.setProjectionMatrix(camera.combined);

		time = 0f;
		speed = 20f;
	}

	@Override
	public void render() {

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		float dt = Gdx.graphics.getDeltaTime();
		time += dt;

		int frame = (int) (time * speed);
		// System.out.println(frame);
		
		ArrayList<Triangle> slice;
		try {
			slice = allTriangles.get(frame);
		} catch (IndexOutOfBoundsException e) {
			time = 0f;
			slice = allTriangles.get(0);
		}

		for (Triangle triangle : slice) {
			
			shape.begin(ShapeType.Filled);
			shape.identity();

			if (triangle.isHollow) {
				shape.setColor(1, 0, 0, 1);
			} else {
				shape.setColor(0, 0, 1, 1);
			}

			shape.triangle(triangle.a.x, triangle.a.y, triangle.b.x,
					triangle.b.y, triangle.c.x, triangle.c.y);
			
			shape.end();
		}
	}

	@Override
	public void dispose() {
	}
}