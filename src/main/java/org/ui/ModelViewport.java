package org.ui;

import javafx.scene.*;
import javafx.animation.AnimationTimer;
import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import org.model.ModelRenderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModelViewport extends SubScene {
    private final Group world;
    private final Group models = new Group();
    private final Group debugAirflow = new Group();
    private final List<DebugArrow> debugArrows = new ArrayList<>();
    private boolean debugAirflowEnabled;
    private SimulationState simulationState = SimulationState.IDLE;
    private Consumer<SimulationMetrics> onSimulationMetricsChanged;
    private long simulationStartNanos;
    private long fpsWindowStartNanos;
    private long lastMetricsUpdateNanos;
    private int framesInWindow;
    private double currentFps;
    private double averageAirflowSpeed;

    private final AnimationTimer debugTimer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            updateDebugSimulation(now);
        }
    };

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

        debugAirflow.setVisible(false);
        debugAirflow.setMouseTransparent(true);
        world.getChildren().addAll(models, debugAirflow, keyLight1, keyLight2, ambientLight,cameraRig);

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
            resetSimulation();
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
    public record SimulationMetrics(boolean running, int particleCount, double airflowSpeed,
                                    double elapsedSeconds, double fps) {}
    public enum SimulationState { IDLE, RUNNING }
    private record DebugArrow(PhongMaterial material, double speedMetric) {}

    public void clearModels(){
        models.getChildren().clear();
    }

    public void setDebugAirflowEnabled(boolean enabled) {
        stopSimulation();
        debugAirflowEnabled = enabled;
        if (enabled) {
            rebuildDebugAirflow();
        }
        debugAirflow.setVisible(false);
        publishSimulationMetrics(0);
    }

    public boolean startSimulation() {
        if (simulationState == SimulationState.RUNNING || models.getChildren().isEmpty()) {
            return false;
        }
        if (!debugAirflowEnabled) {
            return startOpenFoamSimulation();
        }

        if (debugArrows.isEmpty()) {
            rebuildDebugAirflow();
        }
        simulationState = SimulationState.RUNNING;
        simulationStartNanos = System.nanoTime();
        fpsWindowStartNanos = simulationStartNanos;
        lastMetricsUpdateNanos = 0;
        framesInWindow = 0;
        currentFps = 0;
        debugAirflow.setVisible(true);
        debugTimer.start();
        publishSimulationMetrics(0);
        return true;
    }

    public void stopSimulation() {
        debugTimer.stop();
        simulationState = SimulationState.IDLE;
        debugAirflow.setVisible(false);
        framesInWindow = 0;
        currentFps = 0;
        publishSimulationMetrics(0);
    }

    public boolean isSimulationRunning() {
        return simulationState == SimulationState.RUNNING;
    }

    private boolean startOpenFoamSimulation() {
        // Integration hook: launch and monitor OpenFOAM here when that backend is added.
        return false;
    }

    public void setOnSimulationMetricsChanged(Consumer<SimulationMetrics> consumer) {
        onSimulationMetricsChanged = consumer;
        publishSimulationMetrics(0);
    }

    private void resetSimulation() {
        stopSimulation();
        debugArrows.clear();
        debugAirflow.getChildren().clear();
        if (debugAirflowEnabled) {
            rebuildDebugAirflow();
        }
        simulationStartNanos = System.nanoTime();
        fpsWindowStartNanos = simulationStartNanos;
        lastMetricsUpdateNanos = 0;
        framesInWindow = 0;
        currentFps = 0;
        publishSimulationMetrics(0);
    }

    private void rebuildDebugAirflow() {
        debugAirflow.getChildren().clear();
        debugArrows.clear();

        Bounds bounds = models.getBoundsInParent();
        double spacing = 1.25;
        double minX = bounds.isEmpty() ? -6 : Math.floor(bounds.getMinX() - 6);
        double maxX = bounds.isEmpty() ? 6 : Math.ceil(bounds.getMaxX() + 6);
        double minY = bounds.isEmpty() ? -6 : Math.floor(bounds.getMinY() - 6);
        double maxY = bounds.isEmpty() ? 6 : Math.ceil(bounds.getMaxY() + 6);
        double minZ = bounds.isEmpty() ? -4 : Math.floor(bounds.getMinZ() - 4);
        double maxZ = bounds.isEmpty() ? 4 : Math.ceil(bounds.getMaxZ() + 4);
        double spanX = maxX - minX;
        double spanY = maxY - minY;
        double spanZ = maxZ - minZ;
        double arrowLength = 0.45;
        double speedTotal = 0;

        for (double px = minX; px <= maxX; px += spacing) {
            for (double py = minY; py <= maxY; py += spacing) {
                for (double pz = minZ; pz <= maxZ; pz += spacing) {
                    double radialDistance = Math.sqrt(
                            Math.pow((py - (minY + maxY) / 2) / (spanY / 2), 2)
                                    + Math.pow((pz - (minZ + maxZ) / 2) / (spanZ / 2), 2));
                    double centreInfluence = 1 - clamp(radialDistance, 0, 1);
                    double downstreamInfluence = (px - minX) / spanX;
                    double speedMetric = clamp(0.12 + centreInfluence * 0.68
                            + downstreamInfluence * 0.20, 0, 1);

                    PhongMaterial material = new PhongMaterial(colourForMetric(speedMetric));
                    material.setSpecularColor(Color.web("#D8FBFF"));
                    Group arrow = createArrow(arrowLength, material);
                    arrow.setTranslateX(px);
                    arrow.setTranslateY(py);
                    arrow.setTranslateZ(pz);
                    debugAirflow.getChildren().add(arrow);
                    debugArrows.add(new DebugArrow(material, speedMetric));
                    speedTotal += 5 + speedMetric * 20;
                }
            }
        }
        averageAirflowSpeed = debugArrows.isEmpty() ? 0 : speedTotal / debugArrows.size();
    }

    private Group createArrow(double length, PhongMaterial material) {
        double headLength = length * 0.28;
        double shaftLength = length - headLength;
        double thickness = Math.max(0.012, length * 0.018);

        Box shaft = new Box(shaftLength, thickness, thickness);
        shaft.setTranslateX(shaftLength / 2);
        shaft.setMaterial(material);

        TriangleMesh headMesh = new TriangleMesh();
        float h = (float) headLength;
        float r = (float) (thickness * 2.2);
        headMesh.getPoints().addAll(
                h, 0, 0,
                0, -r, -r,
                0, r, -r,
                0, r, r,
                0, -r, r
        );
        headMesh.getTexCoords().addAll(0, 0);
        headMesh.getFaces().addAll(
                0, 0, 1, 0, 2, 0,
                0, 0, 2, 0, 3, 0,
                0, 0, 3, 0, 4, 0,
                0, 0, 4, 0, 1, 0,
                1, 0, 4, 0, 3, 0,
                1, 0, 3, 0, 2, 0
        );

        MeshView head = new MeshView(headMesh);
        head.setTranslateX(shaftLength);
        head.setCullFace(CullFace.NONE);
        head.setMaterial(material);

        return new Group(shaft, head);
    }

    private void updateDebugSimulation(long now) {
        framesInWindow++;
        double fpsWindowSeconds = (now - fpsWindowStartNanos) / 1_000_000_000.0;
        if (fpsWindowSeconds >= 0.5) {
            currentFps = framesInWindow / fpsWindowSeconds;
            framesInWindow = 0;
            fpsWindowStartNanos = now;
        }

        double elapsed = (now - simulationStartNanos) / 1_000_000_000.0;
        for (int i = 0; i < debugArrows.size(); i++) {
            DebugArrow arrow = debugArrows.get(i);
            double animatedMetric = clamp(
                    arrow.speedMetric() + Math.sin(elapsed * 1.4 + i * 0.31) * 0.08, 0, 1);
            arrow.material().setDiffuseColor(colourForMetric(animatedMetric));
        }

        if (now - lastMetricsUpdateNanos >= 250_000_000L) {
            publishSimulationMetrics(elapsed);
            lastMetricsUpdateNanos = now;
        }
    }

    private Color colourForMetric(double metric) {
        Color slow = Color.web("#2563EB");
        Color medium = Color.web("#22D3EE");
        Color fast = Color.web("#F97316");
        return metric < 0.5
                ? slow.interpolate(medium, metric * 2)
                : medium.interpolate(fast, (metric - 0.5) * 2);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void publishSimulationMetrics(double elapsedSeconds) {
        if (onSimulationMetricsChanged != null) {
            onSimulationMetricsChanged.accept(new SimulationMetrics(
                    simulationState == SimulationState.RUNNING,
                    simulationState == SimulationState.RUNNING ? debugArrows.size() : 0,
                    simulationState == SimulationState.RUNNING ? averageAirflowSpeed : 0,
                    simulationState == SimulationState.RUNNING ? elapsedSeconds : 0,
                    simulationState == SimulationState.RUNNING ? currentFps : 0
            ));
        }
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
