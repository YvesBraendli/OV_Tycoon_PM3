package ch.zhaw.ovtycoon.gui.model;

public class ReinforcementDTO {
    private final ZoneSquare zoneSquare;
    private final int maxPlacableTroopAmount;

    public ReinforcementDTO(ZoneSquare zoneSquare, int maxPlacableTroopAmount) {
        this.zoneSquare = zoneSquare;
        this.maxPlacableTroopAmount = maxPlacableTroopAmount;
    }

    public ZoneSquare getZoneSquare() {
        return zoneSquare;
    }

    public int getMaxPlacableTroopAmount() {
        return maxPlacableTroopAmount;
    }
}
