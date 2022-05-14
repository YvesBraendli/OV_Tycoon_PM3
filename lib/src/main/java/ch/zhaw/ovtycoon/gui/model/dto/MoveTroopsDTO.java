package ch.zhaw.ovtycoon.gui.model.dto;

public class MoveTroopsDTO {
    private final int minAmount;
    private final int maxMovableTroops;

    public MoveTroopsDTO(int minAmount, int maxMovableTroops) {
        this.minAmount = minAmount;
        this.maxMovableTroops = maxMovableTroops;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public int getMaxMovableTroops() {
        return maxMovableTroops;
    }
}
