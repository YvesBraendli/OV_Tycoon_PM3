package ch.zhaw.ovtycoon.model;

import ch.zhaw.ovtycoon.Config;
import ch.zhaw.ovtycoon.Config.PlayerColor;
import ch.zhaw.ovtycoon.Config.RegionName;
import ch.zhaw.ovtycoon.data.DiceRoll;
import ch.zhaw.ovtycoon.data.Player;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import static ch.zhaw.ovtycoon.Config.MAX_NUMBER_OF_ATTACKER_TROOPS;
import static ch.zhaw.ovtycoon.Config.MAX_NUMBER_OF_DEFENDER_TROOPS;

public class Game implements Serializable {
    private final Config.Action[] actions = {Config.Action.DEFEND, Config.Action.ATTACK, Config.Action.MOVE};
    private transient SimpleObjectProperty<PlayerColor> fightWinner = new SimpleObjectProperty<>(null);
    private transient SimpleBooleanProperty zoneOvertaken = new SimpleBooleanProperty(false);
    private transient SimpleObjectProperty<Player> currentPlayerProperty = new SimpleObjectProperty<>(null);
    private HashMap<Config.RegionName, ArrayList<Zone>> gameMap;
    private HashMap<Zone, Player> zoneOwner = new HashMap<Zone, Player>();
    private Player[] players;
    private int currentPlayerIndex;
    private TroopHandler troopHandler;
    private transient ObjectProperty<PlayerColor> eliminatedPlayer;
    private transient ObjectProperty<PlayerColor> newRegionOwner;
    private int currentActionIndex = 0;

    /**
     * Initializes all the required properties to handle the API between the backend and the frontend of the Programm.
     */
    public void initializeProperties() {
        fightWinner = new SimpleObjectProperty<>(null);
        zoneOvertaken = new SimpleBooleanProperty(false);
        currentPlayerProperty = new SimpleObjectProperty<>(null);
        eliminatedPlayer = new SimpleObjectProperty<>(null);
        newRegionOwner = new SimpleObjectProperty<>(null);
    }


    /**
     * Initializes the gameMap and creates players with their corresponding colors
     *
     * @param colors The list of all the players in the current game
     */
    public void initGame(ArrayList<PlayerColor> colors) {
        if (colors == null || colors.size() <= 1) {
            throw new IllegalArgumentException("Number of chosen colors musst be equal to numbers of players.");
        }

        MapInitializer mapInit = new MapInitializer();

        players = new Player[colors.size()];
        initPlayers(colors);

        gameMap = mapInit.getGameMap();
        zoneOwner = mapInit.getOwnerList(players);

        troopHandler = new TroopHandler(colors.size());
        eliminatedPlayer = new SimpleObjectProperty<PlayerColor>(null);
        newRegionOwner = new SimpleObjectProperty<PlayerColor>(null);
    }

    /**
     * Initializes all selected Players with their colors. The Number of colors
     * need to be the same as the number of players.
     * If not, an IllegalArgumentException will be thorwn.
     *
     * @param colors selected colors for the players
     * @throws IllegalArgumentException
     */
    public void initPlayers(ArrayList<PlayerColor> colors) {
        for (int i = 0; i < colors.size(); i++) {
            players[i] = new Player(colors.get(i));
        }
    }

    /**
     * starts a fight, between two zones
     * After the fight a zone gets reassigned if the defender does not have any more troops stationed.
     * Additionally it gets checked if the defender was eliminated and if the attacker now owns a new region.
     *
     * @param attacker       - attacking zone
     * @param defender       - defending zone
     * @param numOfAttackers - number of troops, which will attack
     * @param numOfDefenders - number of troops, which will defend
     * @return a Data Transfer Object (DTO) of the rolls made
     */
    public DiceRoll runFight(Zone attacker, Zone defender, int numOfAttackers, int numOfDefenders) {
        fightWinner.set(null); // resetting after each fight
        zoneOvertaken.setValue(false); // set value called here for being able to set it to null
        Player defendingPlayer = getZoneOwner(defender);
        int initialAttackerTroops = attacker.getTroops();
        Fight fight = new Fight(attacker, defender);
        DiceRoll diceRoll = fight.fight(numOfAttackers, numOfDefenders);
        Player winner = attacker.getTroops() < initialAttackerTroops ? getZoneOwner(defender) : getZoneOwner(attacker);
        fightWinner.set(winner.getColor());
        if (defender.getTroops() <= 0) {
            Player attackingPlayer = getZoneOwner(attacker);
            setZoneOwner(attackingPlayer, defender);
            tryEliminatePlayer(defendingPlayer);
            zoneOvertaken.set(true);

            if (getRegionOwner(getRegionOfZone(defender)) == attackingPlayer) {
                setNewRegionOwner(attackingPlayer.getColor());
                setNewRegionOwner(null); // reset
            }
        } 
        return diceRoll;
    }

    /**
     * moves troops from one zone to the other
     * no validation is made if the move is valid. for this the method getPossibleMovementNeighbours() should be used
     *
     * @param from           zone which the troops get moved from
     * @param to             zone which the troops get moved to
     * @param numberOfTroops The number of troop units, the player wants to move from the origin to the target zone.
     */
    public void moveUnits(Zone from, Zone to, int numberOfTroops) {
        troopHandler.moveUnits(from, to, numberOfTroops);
    }

    /**
     * calculates the amount of reinforcements a player gets
     * amount of reinforcements is defined as follows:
     * (#ofZones/3) + (#ofRegions * 2) = #newTroops
     * <p>
     * min. amount of reinforcement is 3
     *
     * @return amount of reinforcement
     */
    public int getAmountOfReinforcements() {
        Player player = players[currentPlayerIndex];
        int reinforcements = (getZonesOwnedbyPlayer(player).size() / 3)
                + (getRegionsOwnedByPlayer(player).size() * 2);

        if (reinforcements >= 3) {
            return reinforcements;
        } else {
            return Config.MIN_NUMBER_OF_REINFORCEMENTS;
        }
    }

    /**
     * Allows current player to add an amount of troops to an owned zone
     *
     * @param amount number of troops to add to zone
     * @param zone   zone to reinforce
     */
    public void reinforce(int amount, Zone zone) {
        troopHandler.reinforce(amount, zone);
    }


    /**
     * Checks for a winner. A winner is defined by owning all zones
     *
     * @return Player which won
     */
    public Player getWinner() {
        Player winner = zoneOwner.get(gameMap.get(RegionName.Oberland).get(0));
        if (winner == null) return null;
        for (Entry<Zone, Player> zone : zoneOwner.entrySet()) {
            if (!winner.equals(zone.getValue())) return null;
        }
        return winner;
    }

    /**
     * Checks if a winner exists
     *
     * @return true if a player one, false if not
     */
    public boolean hasWinner() {
        if (getWinner() != null) return true;
        return false;
    }

    /**
     * Checks if a player owns all zones in a region
     *
     * @param region The region that needs to be checked if it belongs to one player.
     * @return Player the owner of the region or null
     */
    public Player getRegionOwner(RegionName region) {
        Player regionOwner = zoneOwner.get(gameMap.get(region).get(0));
        if (regionOwner == null) return null;
        for (Zone zone : gameMap.get(region)) {
            if (regionOwner != zoneOwner.get(zone))
                return null;
        }
        return regionOwner;
    }

    /**
     * Generates a list with all the regions owned by one player.
     *
     * @param player The player for which a region-list should be generated
     * @return A list which contains all the regions a single player owns
     */
    public ArrayList<RegionName> getRegionsOwnedByPlayer(Player player) {
        ArrayList<RegionName> regionsOwnedByPlayer = new ArrayList<RegionName>();
        for (RegionName region : gameMap.keySet()) {
            if (getRegionOwner(region) == player) {
                regionsOwnedByPlayer.add(region);
            }
        }

        return regionsOwnedByPlayer;
    }

    /**
     * Returns the region name which contains a specific zone
     *
     * @param zone
     * @return region name of region which contains zone
     */
    public RegionName getRegionOfZone(Zone zone) {
        for (Entry<RegionName, ArrayList<Zone>> region : gameMap.entrySet()) {
            if (region.getValue().contains(zone)) {
                return region.getKey();
            }
        }
        return null;
    }

    /**
     * Gets current player
     *
     * @return the current player (crazy i know)
     */
    public Player getCurrentPlayer() {
        return players[currentPlayerIndex];
    }

    /**
     * Switches to next player in the list and to the first if its currently the last players turn
     *
     * @return true if the switch was successfull, false if no active player could be found
     */
    public boolean switchToNextPlayer() {
        return switchToNextPlayer(0, true);
    }

    private boolean switchToNextPlayer(int rec, boolean switched) {
        if (rec == players.length) {
            currentPlayerIndex = 0;
            return false;
        }
        currentPlayerIndex = currentPlayerIndex + 1 == players.length ? 0 : currentPlayerIndex + 1;
        if (players[currentPlayerIndex].isEliminated()) switched = switchToNextPlayer(rec + 1, true);
        currentPlayerProperty.set(players[currentPlayerIndex]);
        return switched;
    }

    /**
     * Sets a new owner for a zone
     *
     * @param owner The player, which should be set as new zone owner
     * @param zone  The zone for which the owner should be updated
     */
    public void setZoneOwner(Player owner, Zone zone) {
        zoneOwner.put(zone, owner);
    }

    /**
     * Gets the current owner for a zone
     *
     * @param zone The zone from which the current owner is desired
     * @return player The current owner of the specified zone
     */
    public Player getZoneOwner(Zone zone) {
        return zoneOwner.get(zone);
    }

    /**
     * Checks if a player owns a specific zone
     *
     * @param player The player for which it should be checked if he is the current owner of the zone.
     * @param zone   The zone for which it should be checked if the player is the current owner.
     * @return true if the player owns the zone, false if not
     */
    public Boolean isZoneOwner(Player player, Zone zone) {
        return (zoneOwner.get(zone) == player);
    }

    /**
     * Searches for all zones which are owned by a specific player
     *
     * @param player to search zones for
     * @return ArrayList of the zones owned by the player
     */
    public ArrayList<Zone> getZonesOwnedbyPlayer(Player player) {
        ArrayList<Zone> zonesOwnedByPlayer = new ArrayList<Zone>();
        for (Zone zone : zoneOwner.keySet()) {
            if (isZoneOwner(player, zone)) {
                zonesOwnedByPlayer.add(zone);
            }
        }
        zonesOwnedByPlayer.trimToSize();
        return zonesOwnedByPlayer;
    }

    /**
     * Gets zones which are classified as "attackable"
     * An attackable zone needs to be next to a zone owned by the player and must not be owned by the player
     *
     * @param zone the attacker zone
     * @return ArrayList of the zones that can be attacked from the attacker zone
     */
    public ArrayList<Zone> getAttackableZones(Zone zone) {
        ArrayList<Zone> neighbourZones = new ArrayList<Zone>();
        if (zone.getTroops() <= 1) return neighbourZones; // TODO can ev be removed
        Player player = getZoneOwner(zone);
        ArrayList<Zone> zonesOwnedByPlayer = getZonesOwnedbyPlayer(player);
        neighbourZones = new ArrayList<>(zone.getNeighbours());
        neighbourZones.removeAll(zonesOwnedByPlayer);
        neighbourZones.trimToSize();
        return neighbourZones;

    }

    /**
     * Gets zones which can be used to attack.
     * This is defined by having more than one troop on the zone
     * and being next to a zone which is not owned by the player
     *
     * @param player to check for
     * @return ArrayList of possible attacking zones
     */
    public ArrayList<Zone> getPossibleAttackerZones(Player player) {
        ArrayList<Zone> zonesOwnedByPlayer = getZonesOwnedbyPlayer(player);
        ArrayList<Zone> possibleAttackerZones = new ArrayList<Zone>();
        for (Zone zone : zonesOwnedByPlayer) {
            if (zone.getTroops() > 1) {
                for (Zone neighbour : zone.getNeighbours()) {
                    if (zoneOwner.get(neighbour) != player) {
                        possibleAttackerZones.add(zone);
                        break;
                    }
                }
            }
        }
        possibleAttackerZones.trimToSize();
        return possibleAttackerZones;
    }

    /**
     * Gets zones which have movable troops
     * zones have movable troops if they have more than one troop on them
     * and have a neighborzone which is owned by the same player
     *
     * @param player to check for
     * @return ArrayList of zones with movable troops
     */
    public ArrayList<Zone> getZonesWithMovableTroops(Player player) {
        ArrayList<Zone> zonesOwnedByPlayer = getZonesOwnedbyPlayer(player);
        ArrayList<Zone> zonesWithMovableTroops = new ArrayList<Zone>();
        for (Zone zone : zonesOwnedByPlayer) {
            if (zone.getTroops() > 1) {
                if (zone.getNeighbours().stream().anyMatch((neighbour -> zonesOwnedByPlayer.contains(neighbour)))) {
                    zonesWithMovableTroops.add(zone);
                }
            }
        }
        zonesWithMovableTroops.trimToSize();
        return zonesWithMovableTroops;
    }

    /**
     * gets neighbours of zone
     *
     * @param zone The zone for which the neighbours should be given.
     * @return ArrayList with neighbours
     */
    public ArrayList<Zone> getZoneNeighbours(Zone zone) {
        return zone.getNeighbours();
    }

    /**
     * Returns a zone object by name
     *
     * @param zoneName The string representative of the name from the zone.
     * @return zone The corresponding zone object to the specified zone name
     */
    public Zone getZone(String zoneName) {
        for (Zone zone : zoneOwner.keySet()) {
            if (zone.getName().equals(zoneName)) {
                return zone;
            }
        }
        return null;
    }

    /**
     * Returns a player by color
     *
     * @param playerColor The player color for the player of which should be returned a player object.
     * @return player The player object to the corresponding color.
     */
    public Player getPlayer(Config.PlayerColor playerColor) {
        for (Player player : players) {
            if (player.getColor() == playerColor) {
                return player;
            }
        }
        return null;
    }

    /**
     * Sets the eliminated flag in the player object and updates the property
     * if the player does not own any zones anymore
     *
     * @param player to check for elimination
     */
    public void tryEliminatePlayer(Player player) {
        if (getZonesOwnedbyPlayer(player).isEmpty()) {
            player.setEliminated();
            setEliminiatedPlayer(player);
        }
    }


    /**
     * Instantiates a new object of the neighbours Array List, fills it with all the zone, in which a player can move
     * his troops units from the origin zone (If the zone belongs to him) and returns this list.
     *
     * @param originZone The Zone, from which the player wants to move his troop units away
     * @param player     The player instance of the current player.
     * @return A list, in which all the possible movement zones for a player and the specified origin zone are contained.
     */
    public ArrayList<Zone> getPossibleMovementNeighbours(Zone originZone, Player player) {
        return getMovementNeighbours(originZone, originZone, player, new ArrayList<>());
    }

    private ArrayList<Zone> getMovementNeighbours(Zone initial, Zone originZone, Player player, ArrayList<Zone> neighbours) {
        for (Zone zone : originZone.getNeighbours()) {
            if (zoneOwner.get(zone) == player && !neighbours.contains(zone) && zone != initial) {
                neighbours.add(zone);
                getMovementNeighbours(initial, zone, player, neighbours);
            }
        }
        return neighbours;
    }

    /**
     * Returns the eliminated player property
     *
     * @return The ObjectProperty of the eliminated player
     */
    public ObjectProperty<PlayerColor> getEliminiatedPlayerProperty() {
        return eliminatedPlayer;
    }

    /**
     * Sets the object property of the eliminated player to the specified player object.
     *
     * @param player The player object to which the eliminated player property should be set.
     */
    public void setEliminiatedPlayer(Player player) {
        eliminatedPlayer.set(player.getColor());
    }

    /**
     * Returns the property of the new region owner.
     *
     * @return The ObjectProperty of the new region owner.
     */
    public ObjectProperty<PlayerColor> getNewRegionOwnerProperty() {
        return newRegionOwner;
    }

    /**
     * Sets the object property of the new region owner to the desired player color.
     *
     * @param playerColor The player color which should be set as new region owner.
     */
    public void setNewRegionOwner(PlayerColor playerColor) {
        newRegionOwner.set(playerColor);
    }

    /**
     * Gets the players array
     *
     * @return array of all players
     */
    public Player[] getPlayers() {
        return players;
    }

    /**
     * Sets the owner of a zone
     *
     * @param owner    Player which should become the owner of the zone
     * @param zoneName name of the player which should be the owner of the passed zone
     */
    public void setZoneOwner(Player owner, String zoneName) {
        zoneOwner.put(getZone(zoneName), owner);
    }

    /**
     * Updates the amount of troops of a zone
     *
     * @param zoneName name of the zone of which the troops should be updated
     * @param troops   amount of troops which should be added to the current troop amount
     */
    public void updateZoneTroops(String zoneName, int troops) {
        Zone zone = getZone(zoneName);
        zone.setTroops(zone.getTroops() + troops);
    }

    /**
     * Gets the current action
     *
     * @return current action
     */
    public Config.Action getCurrentAction() {
        return actions[currentActionIndex];
    }

    /**
     * Switches to the next action in the actions array. If the current action is
     * equal to the last action in the array, the player gets switched as well.
     */
    public void nextAction() {
        if (currentActionIndex == actions.length - 1) {
            switchToNextPlayer();
            currentActionIndex = 0;
        } else {
            currentActionIndex = currentActionIndex + 1;
        }
    }

    /**
     * Property indicating if a zone has been overtaken during an attack
     *
     * @return whether the zone has been overtaken or not
     */
    public SimpleBooleanProperty getZoneOvertaken() {
        return zoneOvertaken;
    }

    /**
     * Gets the winner of a fight
     *
     * @return PlayerColor of the player who won the fight
     */
    public SimpleObjectProperty<PlayerColor> getFightWinner() {
        return fightWinner;
    }

    /**
     * Calculates the maximal amount of troops a zone can provide for an attack.
     *
     * @param zoneName Name of the zone
     * @return maximal available troop amount
     */
    public int getMaxTroopsForAttack(String zoneName) {
        return Math.min(getMaxMovableTroops(zoneName), MAX_NUMBER_OF_ATTACKER_TROOPS);
    }

    /**
     * Calculates the maximal amount of troops a zone can provide for defending.
     *
     * @param zoneName Name of the zone
     * @return maximal available troop amount
     */
    public int getMaxTroopsForDefending(String zoneName) {
        return Math.min(getZone(zoneName).getTroops(), MAX_NUMBER_OF_DEFENDER_TROOPS);
    }

    /**
     * Calculates the maximal amount of troops that can be moved away from the specified
     * zone to another zone.
     *
     * @param zoneName Name of the zone
     * @return maximal available troop amount
     */
    public int getMaxMovableTroops(String zoneName) {
        return getZone(zoneName).getTroops() - 1;
    }
}
