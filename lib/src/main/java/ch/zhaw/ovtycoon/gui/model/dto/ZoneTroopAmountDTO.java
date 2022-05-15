package ch.zhaw.ovtycoon.gui.model.dto;

public class ZoneTroopAmountDTO {
    private final String zoneName;
    private final int troopAmount;

    public ZoneTroopAmountDTO(String zoneName, int troopAmount) {
        this.zoneName = zoneName;
        this.troopAmount = troopAmount;
    }

    public String getZoneName() {
        return zoneName;
    }

    public int getTroopAmount() {
        return troopAmount;
    }
}
