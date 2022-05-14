package ch.zhaw.ovtycoon.gui.model.customnodes;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;

public class ZoneTooltip extends VBox {
    private static final double TOOLTIP_WIDTH = 60.0d;
    private static final double TOOLTIP_HEIGHT = 15.0d;
    private static final double TRIANGLE_SIDE_LENGTH = 10.0d;


    public ZoneTooltip(String text) {
        setWidth(TOOLTIP_WIDTH);
        setHeight(TOOLTIP_HEIGHT);
        setId(text); // TODO ev only use number
        Polygon triangle = new Polygon();
        triangle.getPoints().addAll(new Double[]{0.0d, 0.0d, TRIANGLE_SIDE_LENGTH, 0.0d, TRIANGLE_SIDE_LENGTH / 2.0d, TRIANGLE_SIDE_LENGTH});
        Label label = new Label();
        label.setPrefWidth(TOOLTIP_WIDTH);
        label.setPrefHeight(TOOLTIP_HEIGHT - (TRIANGLE_SIDE_LENGTH / 2.0d));
        label.setText(text);
        label.getStyleClass().add("zone-tooltip");
        setAlignment(Pos.TOP_LEFT);
        getChildren().addAll(label, triangle);
    }
}
