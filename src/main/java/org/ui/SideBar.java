package org.ui;

import javafx.scene.layout.VBox;

public class SideBar extends VBox {
    private int width;
    private int height;

    private ModelInfoBox modelInfoBox;
    private StatsBox statsBox;

    public SideBar(int width, int height){
        this.width = width - 15; //padding
        this.height = height;
        statsBox = new StatsBox(this.width, (int) (height * 0.55));
        modelInfoBox = new ModelInfoBox(this.width, (int) (height * 0.25));

        getChildren().addAll(statsBox, modelInfoBox);
    }


    public StatsBox getStatsBox() {
        return statsBox;
    }

    public ModelInfoBox getModelInfoBox() {
        return modelInfoBox;
    }
}
