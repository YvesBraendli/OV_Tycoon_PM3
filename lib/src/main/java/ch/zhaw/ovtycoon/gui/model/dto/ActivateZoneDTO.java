package ch.zhaw.ovtycoon.gui.model.dto;

import ch.zhaw.ovtycoon.gui.model.ZoneSquare;
import javafx.scene.paint.Color;

/**
 * DTO for passing information about which zone should be activated.
 */
public class ActivateZoneDTO {
    private final ZoneSquare zoneSquare;
    private final Color overlayColor;
    private final boolean shift;

    public ActivateZoneDTO(ZoneSquare zoneSquare, Color overlayColor, boolean shift) {
        this.zoneSquare = zoneSquare;
        this.overlayColor = overlayColor;
        this.shift = shift;
    }

    public ZoneSquare getZoneSquare() {
        return zoneSquare;
    }

    public Color getOverlayColor() {
        return overlayColor;
    }

    public boolean isShift() {
        return shift;
    }
}
