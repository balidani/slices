package hu.balidani.slices.render;

import hu.balidani.slices.utils.Face;
import hu.balidani.slices.utils.FacePair;
import hu.balidani.slices.utils.Line;
import hu.balidani.slices.utils.Triangle;
import hu.balidani.slices.utils.Vertex;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.TreeMap;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;

public class Renderer extends ApplicationAdapter {

    ArrayList<ArrayList<Triangle>> allTriangles;
    ArrayList<Vertex> vertices;
    ArrayList<Face> faces;
    AssetManager assets;
    String filename;
    
    public Renderer(String file) {
        filename = file;
    }
    
    @Override
    public void create() {
        super.create();
        
        // Load assets
        assets = new AssetManager();
        assets.load(filename, Model.class);
        assets.finishLoading();

        // Load mesh
        Model model = assets.get(filename, Model.class);
        
        /*
        for (Node node : model.nodes) {
            for (NodePart part : node.parts) {
                part.meshPart
            }
        }
        */
        
        // TODO: model.nodes traverse
        // model.nodes.get(0)...
        
        renderModel(model);

        // Write file
        String outFile = filename.split("\\.")[0].concat(".slc");

        try {

            FileOutputStream fos = new FileOutputStream(outFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(allTriangles);
            oos.close();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.exit(0);
    }

    public void renderModel(Model model) {

        long start = System.currentTimeMillis();
        
        Mesh mesh = model.meshes.get(0);
        
        System.out.println("Mesh count: " + model.meshParts.size);
        
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
            if (Math.abs(vertexA.z - vertexB.z) < 0.001f
                    && Math.abs(vertexB.z - vertexC.z) < 0.001f) {
                continue;
            }

            // Order intentional, due to 3d to 2d conversion later on
            faces.add(new Face(i, vertexA, vertexB, vertexC));
        }

        System.out.println("Vertex count: " + vertices.size());
        System.out.println("Face count: " + faces.size());

        allTriangles = new ArrayList<ArrayList<Triangle>>();
        
        float sliceMax = 1.0f;
        float sliceSpeed = 0.02f;

        System.out.print("Progress: ");

        for (float z = sliceMax; z > -sliceMax; z -= sliceSpeed) {

            System.out.print(".");

            // The triangles in a slice
            // Provided by earClipping at the end
            ArrayList<Triangle> triangles = new ArrayList<Triangle>();

            ArrayList<FacePair> facePairs = new ArrayList<FacePair>();
            ArrayList<ArrayList<Vertex>> slices = new ArrayList<ArrayList<Vertex>>();

            for (Face face : faces) {

                // Check if face is contained by any seen pair
                // This should be much more efficiently implemented
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

                if (face.intersectsZ(z)) {
                    ArrayList<Line> edgeGroup = new ArrayList<Line>();
                    face.findNeighbors(faces, z, facePairs, edgeGroup);

                    if (edgeGroup == null || edgeGroup.size() == 0) {
                        continue;
                    }

                    // Process edge group into a slice
                    ArrayList<Vertex> slice = new ArrayList<Vertex>();

                    for (Line edge : edgeGroup) {
                        Vertex v = edge.interpolate(z);

                        // Check for duplicate
                        if (!slice.isEmpty()) {
                            Vertex last = slice.get(slice.size() - 1);
                            if (v.resembles(last)) {
                                continue;
                            }
                        }

                        // Optimization (and a bug fix):
                        // If the new vertex is the continuation of the previous
                        // two,
                        // simply remove the last element before adding the new

                        if (slice.size() > 1) {
                            // Get the last two vertices
                            Vertex last1 = slice.get(slice.size() - 1);
                            Vertex last2 = slice.get(slice.size() - 2);

                            if (last2.pointTo(last1)
                                    .resembles(last2.pointTo(v))) {
                                slice.remove(slice.size() - 1);
                            }
                        }

                        slice.add(v);
                    }

                    // Final step of vertex optimization for between the
                    // beginning
                    // and end of the slice
                    if (slice.size() > 2) {

                        Vertex last1 = slice.get(slice.size() - 1);
                        Vertex first1 = slice.get(0);
                        Vertex first2 = slice.get(1);

                        if (first2.pointTo(last1).resembles(
                                first2.pointTo(first1))) {
                            slice.remove(first1);
                        }
                    }

                    slices.add(slice);
                }
            }

            // Determine hollowness levels and organize slices accordingly
            // Holy shit this is bad, but premature optimization and blah blah
            // blah
            TreeMap<Integer, ArrayList<ArrayList<Vertex>>> sliceMap = new TreeMap<Integer, ArrayList<ArrayList<Vertex>>>();

            for (ArrayList<Vertex> slice : slices) {

                // Determine if the slice is a "hollow" part or not
                int level = hollownessLevel(slice, slices);

                if (!sliceMap.containsKey(level)) {
                    ArrayList<ArrayList<Vertex>> levelList = new ArrayList<ArrayList<Vertex>>();
                    sliceMap.put(level, levelList);
                }

                sliceMap.get(level).add(slice);
            }

            for (int i : sliceMap.keySet()) {
                for (ArrayList<Vertex> slice : sliceMap.get(i)) {
                    // renderOutline(slice);

                    boolean isHollow = (i % 2 != 0);
                    ArrayList<Triangle> res = earClipping(slice, isHollow);
                    triangles.addAll(res);
                }
            }

            allTriangles.add(triangles);
        }

        System.out.println();
        
        long end = System.currentTimeMillis();
        System.out.println("Elapsed time: " + (end - start) + " ms");
        System.out.println("Slice count: " + allTriangles.size());

        int sum = 0;
        for (ArrayList<Triangle> t : allTriangles) {
            sum += t.size();
        }

        System.out.println("Triangle count: " + sum);
    }

    public ArrayList<Triangle> earClipping(ArrayList<Vertex> slice,
            boolean hollow) {

        // The triangles processed by ear clipping
        ArrayList<Triangle> triangles = new ArrayList<Triangle>();

        ArrayList<Vertex> originalSlice = new ArrayList<Vertex>();

        for (Vertex vertex : slice) {
            originalSlice.add(new Vertex(vertex));
        }

        while (slice.size() > 2) {

            Vertex ear = null;
            boolean foundEar = false;

            for (int i = 0; i < slice.size(); ++i) {

                Vertex before = i > 0 ? slice.get(i - 1) : slice.get(slice
                        .size() - 1);
                ear = slice.get(i);
                Vertex after = i < slice.size() - 1 ? slice.get(i + 1) : slice
                        .get(0);

                // Check if before -- after is a real diagonal
                Line diagonal = new Line(before, after);
                boolean isDiagonal = true;

                // To do that, we have to see if it intersects any of the edges
                // of our _original_ slice
                for (int j = 0; j < originalSlice.size(); ++j) {
                    Vertex a = originalSlice.get(j);
                    Vertex b = j < originalSlice.size() - 1 ? originalSlice
                            .get(j + 1) : originalSlice.get(0);

                    Line edge = new Line(a, b);

                    if (diagonal.intersects(edge, false)) {
                        isDiagonal = false;
                        break;
                    }
                }

                if (!isDiagonal) {
                    // Intersects other edges, not a diagonal
                    continue;
                }

                // If the line is a real diagonal, we have to check if it's
                // inside the slice or not
                // To do that, we have to launch a line from a point to
                // infinity, and count the times it intersects the slice

                // Take the center of the diagonal
                Vertex center = diagonal.center();
                Vertex infinity = new Vertex(3e5f, 7e5f);
                Line check = new Line(center, infinity);
                int intersectionCount = 0;

                for (int j = 0; j < originalSlice.size(); ++j) {
                    Vertex a = originalSlice.get(j);
                    Vertex b = j < originalSlice.size() - 1 ? originalSlice
                            .get(j + 1) : originalSlice.get(0);

                    Line edge = new Line(a, b);

                    if (check.intersects(edge, true)) {
                        intersectionCount++;
                    }
                }

                if (intersectionCount % 2 == 0) {
                    // Not a real diagonal, it's outside of the slice
                    continue;
                }

                // Found an ear, save it in the list
                Triangle triangle = new Triangle(before, ear, after, hollow);
                triangles.add(triangle);

                foundEar = true;
                break;
            }

            if (foundEar) {
                slice.remove(ear);
            } else {
                // System.out.println("No ear found, " + sliceZ);
                break;
            }
        }

        return triangles;
    }

    private int hollownessLevel(ArrayList<Vertex> slice,
            ArrayList<ArrayList<Vertex>> slices) {

        Vertex start = slice.get(0);

        // Find the highest point for the starting point
        for (Vertex vertex : slice) {
            if (vertex.y > start.y) {
                start = vertex;
            }
        }

        Vertex infinity = new Vertex(1e5f, 7e5f);
        Line check = new Line(start, infinity);

        int intersectionCount = 0;

        // This is N^2 complexity, but it will not be done at runtime in the
        // future
        for (ArrayList<Vertex> otherSlice : slices) {
            if (otherSlice.equals(slice)) {
                continue;
            }

            for (int j = 0; j < otherSlice.size(); ++j) {
                Vertex a = otherSlice.get(j);
                Vertex b = j < otherSlice.size() - 1 ? otherSlice.get(j + 1)
                        : otherSlice.get(0);

                Line edge = new Line(a, b);

                if (check.intersects(edge, true)) {
                    intersectionCount++;
                }
            }
        }

        return intersectionCount;
    }
    
    @Override
    public void dispose() {
        assets.dispose();
    }
}
