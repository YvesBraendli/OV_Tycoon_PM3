package ch.zhaw.ovtycoon.gui.model;

public class AttackDTO {
    private final int maxAttackerTroops;
    private final int maxDefenderTroops;

    public AttackDTO(int maxAttackerTroops, int maxDefenderTroops) {
        this.maxAttackerTroops = maxAttackerTroops;
        this.maxDefenderTroops = maxDefenderTroops;
    }

    public int getMaxAttackerTroops() {
        return maxAttackerTroops;
    }

    public int getMaxDefenderTroops() {
        return maxDefenderTroops;
    }
}
