package org.ui;

import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import org.model.ModelRenderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModelViewport extends SubScene {
    private final Group world;
    private final Group models = new Group();

    private final ModelRenderer modelRenderer;

    private Rotate yaw = new Rotate(0, Rotate.Y_AXIS);
    private Rotate pitch = new Rotate(-15, Rotate.X_AXIS);

    private double lastMouseX;
    private double lastMouseY;

    private double cameraDistance = 50;
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private Group cameraRig = new Group();

    public ModelViewport(double width, double height) {
        this(new Group(), width, height);
    }

    private ModelViewport(Group world, double width, double height){
        super(world, width, height, true, SceneAntialiasing.BALANCED);

        this.world = world;
        modelRenderer = new ModelRenderer();

        camera.setTranslateZ(-cameraDistance);
        camera.setFarClip(10000);

        cameraRig.getTransforms().addAll(yaw,pitch);
        cameraRig.getChildren().add(camera);

        setCamera(camera);

        AmbientLight ambientLight = new AmbientLight(Color.web("#667085"));

        PointLight keyLight1 = new PointLight(Color.web("#B8C0CC"));
        keyLight1.setTranslateX(-100);
        keyLight1.setTranslateY(-150);
        keyLight1.setTranslateZ(-150);

        PointLight keyLight2 = new PointLight(Color.web("#788391"));
        keyLight2.setTranslateX(100);
        keyLight2.setTranslateY(-150);
        keyLight2.setTranslateZ(150);

        world.getChildren().addAll(models, keyLight1, keyLight2, ambientLight,cameraRig);

        setFill(Color.web("#0B1220"));
        addCameraControls();
    }

    public void addMesh(MeshView meshView){
        models.getChildren().add(meshView);
    }

    public MeshMetrics addMeshFile(Path path) {
        try {
            TriangleMesh mesh = modelRenderer.loadOBJFile(path);
            MeshView meshView = new MeshView(mesh);
            meshView.setMaterial(new PhongMaterial(Color.web("#626872")));
            meshView.setCullFace(CullFace.NONE);
            meshView.setScaleY(-1);
            clearModels();
            addMesh(meshView);
            return new MeshMetrics(
                    path.getFileName().toString(),
                    Files.size(path),
                    mesh.getPoints().size() / 3,
                    mesh.getFaces().size() / 6
            );
        } catch (Exception e){
            System.out.println("ERROR" + e);
            return null;
        }
    }

    public record MeshMetrics(String fileName, long fileSize, int vertices, int faces) {}

    public void clearModels(){
        models.getChildren().clear();
    }


    private void addCameraControls(){
        setFocusTraversable(true);

        setOnMouseClicked(event -> requestFocus());

        setOnScroll(event -> {
            cameraDistance -= event.getDeltaY() *.1;
            cameraDistance = Math.max(5, Math.min(1000, cameraDistance));
            camera.setTranslateZ(-cameraDistance);
            System.out.println(camera.getTranslateZ());
        });

        setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                setCursor(Cursor.NONE);
                lastMouseX = event.getSceneX();
                lastMouseY = event.getSceneY();
                requestFocus();
            }
        });

        setOnMouseDragged(event -> {
            if(event.isPrimaryButtonDown()) {
                double sensitivity = .2;
                double deltaX = event.getSceneX() - lastMouseX;
                double deltaY = event.getSceneY() - lastMouseY;

                yaw.setAngle(yaw.getAngle() + deltaX * sensitivity);
                pitch.setAngle(
                        Math.max(-90, Math.min(90, pitch.getAngle() - deltaY * sensitivity))
                );
                lastMouseX = event.getSceneX();
                lastMouseY = event.getSceneY();
            }
        });

        setOnMouseReleased(event -> {
            setCursor(Cursor.DEFAULT);
        });

        setOnKeyPressed(event -> {
            double speed = 2;
            switch (event.getCode()){
                case W:
                    cameraRig.setTranslateY(cameraRig.getTranslateY() + speed);
                    break;
                case S:
                    cameraRig.setTranslateY(cameraRig.getTranslateY() - speed);
                    break;
                case A:
                    cameraRig.setTranslateX(cameraRig.getTranslateX() - speed);
                    break;
                case D:
                    cameraRig.setTranslateX(cameraRig.getTranslateX() + speed);
                    break;
            }
        });


    }
}
