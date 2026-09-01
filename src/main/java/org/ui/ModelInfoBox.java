package org.ui;

import javafx.scene.control.Button;
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

    private Consumer<Path> onMeshSelected;

    public ModelInfoBox(int width, int height){
        updateHeight(height);
        updateWidth(width);

        importMeshButton.setOnAction(event -> chooseMesh());
        importMeshButton.getStyleClass().add("secondary-button");
        simPlaybackButton.getStyleClass().add("primary-button");
        importMeshButton.setMaxWidth(Double.MAX_VALUE);
        simPlaybackButton.setMaxWidth(Double.MAX_VALUE);

        getChildren().addAll(importMeshButton, simPlaybackButton);
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
