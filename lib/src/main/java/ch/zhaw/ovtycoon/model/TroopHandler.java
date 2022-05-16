package ch.zhaw.ovtycoon.model;

import ch.zhaw.ovtycoon.Config;

public class TroopHandler {
    private int numberOfTroopsPerPlayer;

    public TroopHandler(int numberOfPlayers) {
        numberOfTroopsPerPlayer = Config.NUMBER_OF_TROOPS_TOTAL_IN_GAME / numberOfPlayers;
    }

    /**
     * This method moves units from a player from one zone of the gameMap to another.
     *
     * @param zoneToRemoveUnitsFrom    The zone, from which the troop units needs to be removed.
     * @param zoneToMoveUnitsTo        The zone, to which the player desires to move his troop units.
     * @param numberOfTroopUnitsToMove The number of troops, the player desires to move to another zone.
     */
    public void moveUnits(Zone zoneToRemoveUnitsFrom, Zone zoneToMoveUnitsTo,int numberOfTroopUnitsToMove) {
            zoneToMoveUnitsTo.addTroops(numberOfTroopUnitsToMove);
            zoneToRemoveUnitsFrom.decreaseZone(numberOfTroopUnitsToMove);
    }

    /**
     * Allows current player to add an amount of troops to an owned zone
     * @param amount number of troops to add to zone
     * @param zone zone to reinforce
     */
    public void reinforce(int amount, Zone zone){
        zone.addTroops(amount);
    }

    /**
     * Gives the current number of troops back, which on the correspondents on the actual number of Players in game.
     *
     * @return The int-value of the troops, every player has in the current game.
     */
    public int getNumberOfTroopsPerPlayer() {
        return numberOfTroopsPerPlayer;
    }
}
