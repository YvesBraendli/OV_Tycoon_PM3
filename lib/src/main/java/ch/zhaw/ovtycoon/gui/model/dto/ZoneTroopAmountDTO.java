package ch.zhaw.ovtycoon.gui.model.dto;

/**
 * DTO for the amount of troops on a certain zone
 */
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
