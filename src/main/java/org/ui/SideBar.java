package org.ui;

import javafx.scene.layout.VBox;

public class SideBar extends VBox {
    private int width;
    private int height;

    private ModelInfoBox modelInfoBox;
    private StatsBox statsBox;

    public SideBar(int width, int height){
        this.width = width - 15; //padding
        this.height = (int) (height * 0.25);
        statsBox = new StatsBox(this.width, this.height);
        modelInfoBox = new ModelInfoBox(this.width, this.height);

        getChildren().addAll(statsBox, modelInfoBox);
    }


    public StatsBox getStatsBox() {
        return statsBox;
    }

    public ModelInfoBox getModelInfoBox() {
        return modelInfoBox;
    }
}
