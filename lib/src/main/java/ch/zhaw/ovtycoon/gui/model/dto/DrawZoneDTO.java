package ch.zhaw.ovtycoon.gui.model.dto;

import ch.zhaw.ovtycoon.gui.model.ZoneSquare;
import javafx.scene.paint.Color;

/**
 * DTO for drawing a certain zone in a certain color
 */
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
