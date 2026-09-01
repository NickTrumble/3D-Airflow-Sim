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

        getChildren().addAll(titleLabel, meshInfoPane);
    }

    public void updateMeshInfo(ModelViewport.MeshMetrics metrics) {
        fileSizeValue.setText(formatFileSize(metrics.fileSize()));
        verticesValue.setText(String.format("%,d", metrics.vertices()));
        facesValue.setText(String.format("%,d", metrics.faces()));
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
