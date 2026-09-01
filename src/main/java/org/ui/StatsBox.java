package org.ui;

import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class StatsBox extends VBox {
    private int width;
    private int height;

    //region nodes
    private Label titleLabel = new Label("Metrics");
    private final Label fileSizeValue = new Label("—");
    private final Label verticesValue = new Label("—");
    private final Label facesValue = new Label("—");
    private final TitledPane meshInfoPane = new TitledPane();
    private final Label modeValue = new Label("Idle");
    private final Label airflowSpeedValue = new Label("0.0 m/s");
    private final Label particleCountValue = new Label("0");
    private final Label elapsedTimeValue = new Label("00:00.0");
    private final Label fpsValue = new Label("0.0");
    private final TitledPane simulationInfoPane = new TitledPane();

    //endregion

    public StatsBox(int width, int height){
        this.width = width;
        updateWidth(this.width);
        this.height = height;
        updateHeight(this.height);

        titleLabel.getStyleClass().add("section-title");
        meshInfoPane.setText("Mesh information");
        meshInfoPane.setExpanded(true);
        meshInfoPane.setAnimated(false);
        meshInfoPane.setMaxWidth(Double.MAX_VALUE);
        meshInfoPane.getStyleClass().add("metrics-pane");

        GridPane meshInfoGrid = new GridPane();
        meshInfoGrid.getStyleClass().add("metrics-grid");
        addMetric(meshInfoGrid, 0, "File size", fileSizeValue);
        addMetric(meshInfoGrid, 1, "Vertices", verticesValue);
        addMetric(meshInfoGrid, 2, "Faces", facesValue);
        meshInfoPane.setContent(meshInfoGrid);

        simulationInfoPane.setText("Simulation");
        simulationInfoPane.setExpanded(true);
        simulationInfoPane.setAnimated(false);
        simulationInfoPane.setMaxWidth(Double.MAX_VALUE);
        simulationInfoPane.getStyleClass().add("metrics-pane");

        GridPane simulationGrid = new GridPane();
        simulationGrid.getStyleClass().add("metrics-grid");
        addMetric(simulationGrid, 0, "Mode", modeValue);
        addMetric(simulationGrid, 1, "Airflow", airflowSpeedValue);
        addMetric(simulationGrid, 2, "Samples", particleCountValue);
        addMetric(simulationGrid, 3, "Elapsed", elapsedTimeValue);
        addMetric(simulationGrid, 4, "FPS", fpsValue);
        simulationInfoPane.setContent(simulationGrid);

        setSpacing(8);
        getChildren().addAll(titleLabel, meshInfoPane, simulationInfoPane);
    }

    public void updateMeshInfo(ModelViewport.MeshMetrics metrics) {
        fileSizeValue.setText(formatFileSize(metrics.fileSize()));
        verticesValue.setText(String.format("%,d", metrics.vertices()));
        facesValue.setText(String.format("%,d", metrics.faces()));
    }

    public void updateSimulationMetrics(ModelViewport.SimulationMetrics metrics) {
        modeValue.setText(metrics.running() ? "Debug" : "Idle");
        airflowSpeedValue.setText(String.format("%.1f m/s", metrics.airflowSpeed()));
        particleCountValue.setText(String.format("%,d", metrics.particleCount()));
        elapsedTimeValue.setText(formatElapsedTime(metrics.elapsedSeconds()));
        fpsValue.setText(String.format("%.1f", metrics.fps()));
    }

    private String formatElapsedTime(double seconds) {
        int minutes = (int) seconds / 60;
        double remainingSeconds = seconds - minutes * 60;
        return String.format("%02d:%04.1f", minutes, remainingSeconds);
    }

    private void addMetric(GridPane grid, int row, String name, Label value) {
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("metric-name");
        value.getStyleClass().add("metric-value");
        grid.add(nameLabel, 0, row);
        grid.add(value, 1, row);
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kilobytes = bytes / 1024.0;
        if (kilobytes < 1024) return String.format("%.1f KB", kilobytes);
        return String.format("%.1f MB", kilobytes / 1024.0);
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
