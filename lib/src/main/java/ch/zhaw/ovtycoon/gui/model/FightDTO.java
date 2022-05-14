package ch.zhaw.ovtycoon.gui.model;

public class FightDTO {
    private boolean attackerWon;
    private boolean overTookRegion;
    private boolean overtookZone;
    private String attacker;
    private String defender;
    private String fightWinner;
    private String fightLoser;
    private int[] attackerDiceRoll;
    private int[] defenderDiceRoll;
    private int attackerTroops;
    private String overtakenRegionName;

    public String getOvertakenRegionName() {
        return overtakenRegionName;
    }

    public void setOvertakenRegionName(String overtakenRegionName) {
        this.overtakenRegionName = overtakenRegionName;
    }

    public int getAttackerTroops() {
        return attackerTroops;
    }

    public void setAttackerTroops(int attackerTroops) {
        this.attackerTroops = attackerTroops;
    }

    public boolean isOvertookZone() {
        return overtookZone;
    }

    public void setOvertookZone(boolean overtookZone) {
        this.overtookZone = overtookZone;
    }

    public boolean isAttackerWon() {
        return attackerWon;
    }

    public boolean isOverTookRegion() {
        return overTookRegion;
    }

    public String getAttacker() {
        return attacker;
    }

    public String getDefender() {
        return defender;
    }

    public String getFightWinner() {
        return fightWinner;
    }

    public String getFightLoser() {
        return fightLoser;
    }

    public int[] getAttackerDiceRoll() {
        return attackerDiceRoll;
    }

    public int[] getDefenderDiceRoll() {
        return defenderDiceRoll;
    }

    public void setAttackerWon(boolean attackerWon) {
        this.attackerWon = attackerWon;
    }

    public void setOverTookRegion(boolean overTookRegion) {
        this.overTookRegion = overTookRegion;
    }

    public void setAttacker(String attacker) {
        this.attacker = attacker;
    }

    public void setDefender(String defender) {
        this.defender = defender;
    }

    public void setFightWinner(String fightWinner) {
        this.fightWinner = fightWinner;
    }

    public void setFightLoser(String fightLoser) {
        this.fightLoser = fightLoser;
    }

    public void setAttackerDiceRoll(int[] attackerDiceRoll) {
        this.attackerDiceRoll = attackerDiceRoll;
    }

    public void setDefenderDiceRoll(int[] defenderDiceRoll) {
        this.defenderDiceRoll = defenderDiceRoll;
    }
}
