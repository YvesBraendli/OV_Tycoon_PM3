package ch.zhaw.ovtycoon.model;

import java.util.HashMap;

import ch.zhaw.ovtycoon.Config;
import jdk.jshell.spi.ExecutionControl;

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
            zoneToMoveUnitsTo.setTroops(zoneToMoveUnitsTo.getTroops() + numberOfTroopUnitsToMove);
            zoneToRemoveUnitsFrom.setTroops(zoneToRemoveUnitsFrom.getTroops() - numberOfTroopUnitsToMove);
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
