package org.ui;

import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

public class ModelInfoBox extends VBox {
    private int width;
    private int height;

    private Button importMeshButton = new Button("Import Mesh");
    private Button simPlaybackButton = new Button("Start Simulation");
    private final ToggleButton debugModeButton = new ToggleButton("Debug airflow");

    private Consumer<Path> onMeshSelected;
    private Consumer<Boolean> onDebugModeChanged;
    private Runnable onSimulationAction;

    public ModelInfoBox(int width, int height){
        updateHeight(height);
        updateWidth(width);

        importMeshButton.setOnAction(event -> chooseMesh());
        importMeshButton.getStyleClass().add("secondary-button");
        simPlaybackButton.getStyleClass().add("primary-button");
        simPlaybackButton.setDisable(true);
        simPlaybackButton.setOnAction(event -> {
            if (onSimulationAction != null) {
                onSimulationAction.run();
            }
        });
        debugModeButton.getStyleClass().add("debug-toggle");
        importMeshButton.setMaxWidth(Double.MAX_VALUE);
        simPlaybackButton.setMaxWidth(Double.MAX_VALUE);
        debugModeButton.setMaxWidth(Double.MAX_VALUE);
        debugModeButton.setOnAction(event -> {
            if (onDebugModeChanged != null) {
                onDebugModeChanged.accept(debugModeButton.isSelected());
            }
        });

        getChildren().addAll(importMeshButton, simPlaybackButton, debugModeButton);
    }

    private void chooseMesh(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("3D model files", "*.obj")
        );
        fileChooser.setInitialDirectory(new File("C:\\Users\\iantr\\IdeaProjects\\3d model airflow sim\\src\\main\\resources\\models"));

        File file = fileChooser.showOpenDialog(getScene().getWindow());

        if (file != null && onMeshSelected != null)
            onMeshSelected.accept(file.toPath());
    }

    public void setOnMeshSelected(Consumer<Path> consumer){
        onMeshSelected = consumer;
    }

    public void setOnDebugModeChanged(Consumer<Boolean> consumer) {
        onDebugModeChanged = consumer;
    }

    public void setOnSimulationAction(Runnable action) {
        onSimulationAction = action;
    }

    public void setMeshLoaded(boolean loaded) {
        simPlaybackButton.setDisable(!loaded);
    }

    public void setSimulationRunning(boolean running) {
        simPlaybackButton.setText(running ? "Stop Simulation" : "Start Simulation");
        simPlaybackButton.getStyleClass().removeAll("primary-button", "stop-button");
        simPlaybackButton.getStyleClass().add(running ? "stop-button" : "primary-button");
    }

    public void showOpenFoamPending() {
        simPlaybackButton.setTooltip(new Tooltip("OpenFOAM integration is not connected yet. Enable Debug airflow to run locally."));
    }

    public void clearSimulationTooltip() {
        simPlaybackButton.setTooltip(null);
    }

    private void updateWidth(int width){
        setMinWidth(width);
        setMaxWidth(width);
        setPrefWidth(width);
    }

    private void updateHeight(int Height){
        setMinHeight(Height);
        setMaxHeight(Height);
        setPrefHeight(Height);
    }
}
