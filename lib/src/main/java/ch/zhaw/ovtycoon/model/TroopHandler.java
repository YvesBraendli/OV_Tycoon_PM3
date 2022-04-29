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
        if (moveIsValid(zoneToRemoveUnitsFrom, zoneToMoveUnitsTo, player, numberOfTroopUnitsToMove, game)) {
            successful = true;
            zoneToMoveUnitsTo.setTroops(zoneToMoveUnitsTo.getTroops() + numberOfTroopUnitsToMove);
            zoneToRemoveUnitsFrom.setTroops(zoneToRemoveUnitsFrom.getTroops() - numberOfTroopUnitsToMove);
        }
        return successful;
    }

    private boolean moveIsValid(Zone zoneToRemoveUnitsFrom, Zone zoneToMoveUnitsTo, Player player, int numberOfTroopUnitsToMove, Game game) {
        return game.getZoneOwner(zoneToMoveUnitsTo) == player
                && game.getZoneOwner(zoneToRemoveUnitsFrom) == player
                && (zoneToRemoveUnitsFrom.getTroops() >= numberOfTroopUnitsToMove + Config.MIN_NUMBER_OF_TROOPS_IN_ZONE)
                && zonesHaveDirectConnection(zoneToRemoveUnitsFrom, zoneToMoveUnitsTo, game, player);
    }

    /**
     * Gives the current number of troops back, which on the correspondents on the actual number of Players in game.
     *
     * @return The int-value of the troops, every player has in the current game.
     */
    public int getNumberOfTroopsPerPlayer() {
        return numberOfTroopsPerPlayer;
    }

    private boolean zonesHaveDirectConnection(Zone originZone, Zone targetZone, Game game, Player player) {
        originZone.setAlreadyVisitedTrue();
        if (originZone == targetZone) {
            return true;
        } else {
            for (Zone adjacentZone : originZone.getNeighbours()) {
                if (!adjacentZone.getAlreadyVisited()
                        && game.getZoneOwner(adjacentZone) == player) {
                    if (zonesHaveDirectConnection(adjacentZone, targetZone, game, player)) {
                        return true;
                    }
                }
            }
        }
        originZone.setAlreadyVisitedFalse();
        return false;
    }

}
