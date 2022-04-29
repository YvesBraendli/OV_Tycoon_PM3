package ch.zhaw.ovtycoon.model;

import java.util.HashMap;

import ch.zhaw.ovtycoon.Config;

public class TroopHandler {
    private int numberOfTroopsPerPlayer;
    private HashMap<Zone, Integer> numberOfTroopsPerZone = new HashMap<Zone, Integer>();

    public TroopHandler(int numberOfPlayers) {
        numberOfTroopsPerPlayer = Config.NUMBER_OF_TROOPS_TOTAL_IN_GAME / numberOfPlayers;
    }

    /**
     * This method moves units from a player from one zone of the gameMap to another. It checks,
     * if the player is the zone owner of both zones, if there are enough troop units in the
     * "RemoveUnitsFrom" zone and also if there is an valid connection between those two zones.
     * If the tests are valid, the method removes the troop units from the desired zone of the player
     * and ads the same amount to the target zone.
     *
     * @param zoneToRemoveUnitsFrom    The zone, from which the troop units needs to be removed.
     * @param zoneToMoveUnitsTo        The zone, to which the player desires to move his troop units.
     * @param player                   The instance of the current player, which wants to move his troops.
     * @param numberOfTroopUnitsToMove The number of troops, the player desires to move to another zone.
     * @param game                     The instance of the current game.
     * @return true, if the movement of the troop units passed successfully
     */
    public boolean moveUnits(Zone zoneToRemoveUnitsFrom, Zone zoneToMoveUnitsTo, Player player, int numberOfTroopUnitsToMove, Game game) {
        boolean successful = false;
        boolean isOwner = false;
        if (game.getZoneOwner(zoneToMoveUnitsTo) == player
                && game.getZoneOwner(zoneToRemoveUnitsFrom) == player) {
            isOwner = true;
        }

        return successful;
    }

    /**
     * Gives the current number of troops back, which on the correspondents on the actual number of Players in game.
     *
     * @return The int-value of the troops, every player has in the current game.
     */
    public int getNumberOfTroopsPerPlayer() {
        return numberOfTroopsPerPlayer;
    }

    /**
     * Returns the int value of the number of the troop units in the specified zone.
     *
     * @param zone The zone, from which on want to know the actual unit number.
     * @return The int-value, which represents the number of current units in the zone.
     */
    public int getNumberOfTroopsInZone(Zone zone) {
        return numberOfTroopsPerZone.get(zone);
    }

}
