package org.model;

import javafx.scene.shape.TriangleMesh;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModelRenderer {

    public TriangleMesh loadOBJFile(Path objFile) throws IOException {
        TriangleMesh triangleMesh = new TriangleMesh();

        triangleMesh.getTexCoords().addAll(0, 0);

        for (String line : Files.readAllLines(objFile)){
            line = line.trim();

            if (line.isEmpty() || line.startsWith("#"))
                continue;

            String[] parts = line.split("\\s+");

            if (parts[0].equals("v")){//vertex
                triangleMesh.getPoints().addAll(
                        Float.parseFloat(parts[1]),
                        Float.parseFloat(parts[2]),
                        Float.parseFloat(parts[3])
                );
            }

            if(parts[0].equals("f")){//faces

                List<Integer> verts = new ArrayList<>();

                for (int i = 1; i < parts.length; i++) {
                    String[] vertexParts = parts[i].split("/", -1);
                    String vertex = vertexParts[0];
                    //add normals
                    //String normal = vertexParts[2];

                    int vIndex = Integer.parseInt(vertex) - 1;

                    verts.add(vIndex);
                }

                for (int i = 1; i < verts.size() - 1; i++) {
                    triangleMesh.getFaces().addAll(
                            verts.get(0), 0,
                            verts.get(i), 0,
                            verts.get(i + 1), 0
                            );
                }
            }
        }

        System.out.println("vertices: " + triangleMesh.getPoints().size() + ", faces: " + triangleMesh.getFaces().size());

        centerMesh(triangleMesh);
        return triangleMesh;
    }

    private void centerMesh(TriangleMesh mesh) {
        var points = mesh.getPoints();

        if (points.size() == 0) {
            return;
        }

        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;

        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;

        for (int i = 0; i < points.size(); i += 3) {
            float x = points.get(i);
            float y = points.get(i + 1);
            float z = points.get(i + 2);

            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);

            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }

        float centerX = (minX + maxX) / 2f;
        float centerY = (minY + maxY) / 2f;
        float centerZ = (minZ + maxZ) / 2f;

        for (int i = 0; i < points.size(); i += 3) {
            points.set(i, points.get(i) - centerX);
            points.set(i + 1, points.get(i + 1) - centerY);
            points.set(i + 2, points.get(i + 2) - centerZ);
        }
    }
}
