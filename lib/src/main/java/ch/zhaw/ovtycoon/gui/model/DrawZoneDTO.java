package ch.zhaw.ovtycoon.gui.model;

import javafx.scene.paint.Color;

public class DrawZoneDTO {
    private final ZoneSquare zoneToDraw;
    private final Color color;

    public DrawZoneDTO(ZoneSquare zoneToDraw, Color color) {
        this.zoneToDraw = zoneToDraw;
        this.color = color;
    }

    public ZoneSquare getZoneToDraw() {
        return zoneToDraw;
    }

    public Color getColor() {
        return color;
    }
}
