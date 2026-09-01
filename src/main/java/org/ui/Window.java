package org.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Window extends Application {
    private final int DEFAULT_WINDOW_WIDTH = 1000;
    private final int DEFAULT_WINDOW_HEIGHT = 800;

    private int windowWidth = DEFAULT_WINDOW_WIDTH;
    private int windowHeight = DEFAULT_WINDOW_HEIGHT;

    private final double statsWidth = 0.2d;

    public static void launch(){
        Application.launch(Window.class);
    }

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();

        int modelViewportWidth = (int) (windowWidth * (1-statsWidth));
        ModelViewport modelViewport = new ModelViewport(modelViewportWidth, windowHeight);

        int sideBarWidth = (int) (windowWidth * statsWidth);
        SideBar sideBar = new SideBar(sideBarWidth, windowHeight);

        StackPane viewportContainer = new StackPane(modelViewport);
        viewportContainer.setMinSize(0, 0);
        modelViewport.setManaged(false);
        Label meshTitle = new Label("No mesh loaded");
        meshTitle.getStyleClass().add("mesh-title");
        meshTitle.setMouseTransparent(true);
        StackPane.setAlignment(meshTitle, Pos.TOP_CENTER);

        MenuButton fileMenu = new MenuButton();
        fileMenu.getStyleClass().add("file-menu");
        fileMenu.setGraphic(createFileIcon());
        fileMenu.setTooltip(new Tooltip("Imported meshes"));
        fileMenu.setAccessibleText("Imported meshes");
        StackPane.setAlignment(fileMenu, Pos.TOP_LEFT);
        StackPane.setMargin(fileMenu, new javafx.geometry.Insets(10));

        viewportContainer.getChildren().addAll(meshTitle, fileMenu);

        modelViewport.widthProperty().bind(viewportContainer.widthProperty());
        modelViewport.heightProperty().bind(viewportContainer.heightProperty());

        Function<Path, Boolean> loadMesh = path -> {
            ModelViewport.MeshMetrics metrics = modelViewport.addMeshFile(path);
            if (metrics != null) {
                sideBar.getStatsBox().updateMeshInfo(metrics);
                meshTitle.setText(removeFileExtension(metrics.fileName()));
                return true;
            }
            return false;
        };

        List<Path> importedMeshes = new ArrayList<>();
        modelViewport.setOnSimulationMetricsChanged(sideBar.getStatsBox()::updateSimulationMetrics);
        sideBar.getModelInfoBox().setOnDebugModeChanged(enabled -> {
            modelViewport.setDebugAirflowEnabled(enabled);
            sideBar.getModelInfoBox().setSimulationRunning(false);
            if (enabled) {
                sideBar.getModelInfoBox().clearSimulationTooltip();
            }
        });
        sideBar.getModelInfoBox().setOnSimulationAction(() -> {
            if (modelViewport.isSimulationRunning()) {
                modelViewport.stopSimulation();
                sideBar.getModelInfoBox().setSimulationRunning(false);
            } else if (modelViewport.startSimulation()) {
                sideBar.getModelInfoBox().clearSimulationTooltip();
                sideBar.getModelInfoBox().setSimulationRunning(true);
            } else {
                sideBar.getModelInfoBox().showOpenFoamPending();
            }
        });
        sideBar.getModelInfoBox().setOnMeshSelected(path -> {
            if (!loadMesh.apply(path)) {
                return;
            }
            sideBar.getModelInfoBox().setMeshLoaded(true);
            sideBar.getModelInfoBox().setSimulationRunning(false);

            Path normalisedPath = path.toAbsolutePath().normalize();
            if (!importedMeshes.contains(normalisedPath)) {
                importedMeshes.add(normalisedPath);
                MenuItem meshItem = new MenuItem(removeFileExtension(path.getFileName().toString()));
                meshItem.setOnAction(event -> {
                    if (loadMesh.apply(normalisedPath)) {
                        sideBar.getModelInfoBox().setSimulationRunning(false);
                    }
                });
                fileMenu.getItems().add(meshItem);
            }
        });

        root.setCenter(viewportContainer);
        root.setRight(sideBar);

        Scene scene = new Scene(root, windowWidth, windowHeight);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        root.getStyleClass().add("app-root");
        viewportContainer.getStyleClass().add("app-model");
        sideBar.getStyleClass().add("app-side");
        sideBar.getStatsBox().getStyleClass().add("app-stats");
        sideBar.getModelInfoBox().getStyleClass().add("app-info");

        stage.setScene(scene);
        stage.setTitle("Airflow around 3D models simulation");
        stage.getIcons().add(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/app-icon.png"))
        ));
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();



    }

    private String removeFileExtension(String fileName) {
        int extensionIndex = fileName.lastIndexOf('.');
        return extensionIndex > 0 ? fileName.substring(0, extensionIndex) : fileName;
    }

    private SVGPath createFileIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M2 3h7l2 2h11v14H2z M2 7h20");
        icon.getStyleClass().add("file-icon");
        return icon;
    }

}
