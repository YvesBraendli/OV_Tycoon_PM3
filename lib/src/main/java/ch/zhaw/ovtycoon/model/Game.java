package ch.zhaw.ovtycoon.model;

import ch.zhaw.ovtycoon.Config;
import ch.zhaw.ovtycoon.Config.PlayerColor;
import ch.zhaw.ovtycoon.Config.RegionName;
import ch.zhaw.ovtycoon.data.DiceRoll;
import ch.zhaw.ovtycoon.data.Player;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class Game implements Serializable {
	private HashMap<Config.RegionName, ArrayList<Zone>> gameMap;
	private HashMap<Zone, Player> zoneOwner = new HashMap<Zone,Player>();
	private Player[] players;
	private int currentPlayerIndex;
	private TroopHandler troopHandler;
	private ObjectProperty<PlayerColor> eliminatedPlayer;
	private ObjectProperty<PlayerColor> newRegionOwner;
	
	/**
	 * TODO REMOVE AFTER PLAYER INIT IMPLEMENTATION
	 * Helperfunction for testing until player implementation is complete
	 * @param players
	 */
	public void setPlayerList(Player[] players) {
		this.players = players;
	}

	/**
	 * Initializes the gameMap and creates players with their corresponding colors
	 * @param playerAmount number of players
	 */
	public void initGame(int playerAmount) {
		MapInitializer mapInit = new MapInitializer();
		gameMap = mapInit.getGameMap();	
		zoneOwner = mapInit.getOwnerList();
		players = new Player[playerAmount];
		troopHandler = new TroopHandler(playerAmount);
		eliminatedPlayer = new SimpleObjectProperty<PlayerColor>(null);
		newRegionOwner = new SimpleObjectProperty<PlayerColor>(null);
	}

	/**
	 * Initializes all selected Players with their colors. The Number of colors
	 * need to be the same as the number of players.
	 * If not, an IllegalArgumentException will be thorwn.
	 * @param colors selected colors for the players
	 * @throws IllegalArgumentException
	 */
	public void initPlayers(ArrayList<PlayerColor> colors){
		if(colors.size() != players.length){
			throw new IllegalArgumentException("Number of chosen colors musst be equal to numbers of players.");
		}
		for (int i = 0; i < colors.size(); i++){
			players[i] = new Player(colors.get(i));
		}
	}
	
    /**
     * starts a fight, between two zones
     * After the fight a zone gets reassigned if the defender does not have any more troops stationed.
     * Additionally it gets checked if the defender was eliminated and if the attacker now owns a new region.
     * 
     * @param attacker - attacking zone
     * @param defender - defending zone
     * @param numOfAttackers - number of troops, which will attack
     * @param numOfDefenders - number of troops, which will defend
     * @return a Data Transfer Object (DTO) of the rolls made
     */
	public DiceRoll runFight(Zone attacker, Zone defender, int numOfAttackers, int numOfDefenders) {
		Fight fight = new Fight(attacker, defender);
		DiceRoll diceRoll = fight.fight(numOfAttackers, numOfDefenders);
		if(defender.getTroops() == 0) {
			
			tryEliminatePlayer(getZoneOwner(defender));
			Player attackingPlayer = getZoneOwner(attacker);
			setZoneOwner(attackingPlayer, defender);
			defender.setTroops(numOfAttackers);
			
			if(getRegionOwner(getRegionOfZone(defender)) == attackingPlayer) {
				setNewRegionOwner(attackingPlayer);
			}
		}
		return diceRoll;
	}
	
    /**
     * moves troops from one zone to the other
     * no validation is made if the move is valid. for this the method getPossibleMovementNeighbours() should be used
     * @param from zone which the troops get moved from
     * @param to zone which the troops get moved to
     * @param numberOfTroops
     */
	public void moveUnits(Zone from, Zone to, int numberOfTroops) {
		troopHandler.moveUnits(from, to, currentPlayerIndex);
	}
	
	/**
	 * This method is responsible for the initial troop setting, which starts at the beginning of the game.
	 * It sets one troop of the player in the zone, afterwards it has to be called again for the next player.
	 * 
	 * @param player The player instance which wants to add troops to a zone.
	 * @param zoneToPlaceTroop The zone, in which the player wants to add his troop unit.
	 * 
	 * @return true, if the troop was successfully added to the wished zone.
	 */
	public boolean setInitialTroops(Player player, Zone zoneToPlaceTroop) {
		boolean wasSuccessfull = false;
		return wasSuccessfull;
	}
	
	/**
	 * Checks for a winner. A winner is defined by owning all zones
	 * @return Player which won
	 */
	public Player getWinner() {
		Player winner = zoneOwner.get(gameMap.get(RegionName.Oberland).get(0));
		if(winner == null) return null;
		for(Entry<Zone,Player> zone: zoneOwner.entrySet()) {
			if(!winner.equals(zone.getValue())) return null;
		}
		return winner;
	}
	
	/**
	 * Checks if a winner exists
	 * @return true if a player one, false if not
	 */
	public boolean hasWinner() {
		if(getWinner() != null) return true;
		return false;
	}
	
	/**
	 * Checks if a player owns all zones in a region
	 * @param region 
	 * @return Player the owner of the region or null
	 */
	public Player getRegionOwner(RegionName region) {
		Player regionOwner = zoneOwner.get(gameMap.get(region).get(0));
		if(regionOwner == null) return null;
		for (Zone zone : gameMap.get(region)) {
			if (regionOwner != zoneOwner.get(zone))
				return null;
		}
		return regionOwner;
	}
	
	/**
	 * Returns the region name which contains a specific zone
	 * @param zone
	 * @return region name of region which contains zone
	 */
	public RegionName getRegionOfZone(Zone zone) {
		for(Entry<RegionName,ArrayList<Zone>> region : gameMap.entrySet()) {
			if(region.getValue().contains(zone)){
				return region.getKey();
			}
		}
		return null;
	}
	
	/**
	 * Gets current player
	 * @return the current player (crazy i know)
	 */
	public Player getCurrentPlayer() {
		return players[currentPlayerIndex];
	}
	
	/**
	 * Switches to next player in the list and to the first if its currently the last players turn
	 * @return true if the switch was successfull, false if no active player could be found
	 */
	public boolean switchToNextPlayer() {
		return switchToNextPlayer(0, true);
	}
	private boolean switchToNextPlayer(int rec, boolean switched) {
		if(rec == players.length) {
			currentPlayerIndex = 0;
			return false;
		}
		currentPlayerIndex = currentPlayerIndex+1 == players.length ? 0 : currentPlayerIndex+1;
		if(players[currentPlayerIndex].isEliminated()) switched = switchToNextPlayer(rec+1, true);
		return switched;
	}

	/**
	 * Sets a new owner for a zone
	 * @param owner
	 * @param zone
	 */
	public void setZoneOwner(Player owner, Zone zone) {
		zoneOwner.put(zone, owner);
	}
	
	/**
	 * Gets the current owner for a zone
	 * @param zone
	 * @return player
	 */
	public Player getZoneOwner(Zone zone) {
		return zoneOwner.get(zone);
	}
	
	/**
	 * Checks if a player owns a specific zone
	 * @param player
	 * @param zone
	 * @return true if the player owns the zone, false if not
	 */
	public Boolean isZoneOwner(Player player, Zone zone) {
		return (zoneOwner.get(zone) == player);
	}
	
	/**
	 * Searches for all zones which are oned by a specific player
	 * @param player to search zones for
	 * @return ArrayList of the zones owned by the player
	 */
	public ArrayList<Zone> getZonesOwnedbyPlayer(Player player){
		ArrayList<Zone> zonesOwnedByPlayer = new ArrayList<Zone>();
		for(Zone zone: zoneOwner.keySet()) {
			if(isZoneOwner(player, zone)) {
				zonesOwnedByPlayer.add(zone);
			}
		}
		zonesOwnedByPlayer.trimToSize();
		return zonesOwnedByPlayer;
	}
	
	/**
	 * Gets zones which are classified as "attackable"
	 * An attackable zone needs to be next to a zone owned by the player and must not be owned by the player
	 * @param zone the attacker zone
	 * @return ArrayList of the zones that can be attacked from the attacker zone
	 */
	public ArrayList<Zone> getAttackableZones(Zone zone){
		ArrayList<Zone> neighbourZones = new ArrayList<Zone>();
		if(zone.getTroops()<=1) return neighbourZones;
		Player player = getZoneOwner(zone);
		ArrayList<Zone> zonesOwnedByPlayer = getZonesOwnedbyPlayer(player);
		neighbourZones = zone.getNeighbours();
		neighbourZones.removeAll(zonesOwnedByPlayer);
		neighbourZones.trimToSize();
		return neighbourZones;
	
	}
	
	/**
	 * Gets zones which can be used to attack. 
	 * This is defined by having more than one troop on the zone
	 * and being next to a zone which is not owned by the player
	 * @param player to check for
	 * @return ArrayList of possible attacking zones
	 */
	public ArrayList<Zone> getPossibleAttackerZones(Player player){
		ArrayList<Zone> zonesOwnedByPlayer = getZonesOwnedbyPlayer(player);
		ArrayList<Zone> possibleAttackerZones = new ArrayList<Zone>();
		for(Zone zone: zonesOwnedByPlayer) {
			if(zone.getTroops()>1) {
				for(Zone neighbour : zone.getNeighbours()) {
					if(zoneOwner.get(neighbour) != player) {
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
	 * @param player to check for
	 * @return ArrayList of zones with movable troops
	 */
	public ArrayList<Zone> getZonesWithMovableTroops(Player player) {
		ArrayList<Zone> zonesOwnedByPlayer = getZonesOwnedbyPlayer(player);
		ArrayList<Zone> zonesWithMovableTroops = new ArrayList<Zone>();
		for(Zone zone: zonesOwnedByPlayer) {
			if(zone.getTroops() > 1) {
				if(zone.getNeighbours().removeAll(zonesOwnedByPlayer)) {
					zonesWithMovableTroops.add(zone);
				}
			}
		}
		zonesWithMovableTroops.trimToSize();
		return zonesWithMovableTroops;
	}
	
	/**
	 * gets neighbours of zone
	 * @param zone
	 * @return ArrayList with neighbours
	 */
	public ArrayList<Zone> getZoneNeighbours(Zone zone){
		return zone.getNeighbours();
	}
	
	/**
	 * Returns a zone object by name
	 * @param zoneName enum
	 * @return zone
	 */
	public Zone getZone(String zoneName) {
		for(Zone zone: zoneOwner.keySet()) {
			if(zone.getName().equals(zoneName)) {
				return zone;
			}
		}
		return null;
	}
	
	/**
	 * Returns a player by color
	 * @param playerColor
	 * @return player
	 */
	public Player getPlayer(Config.PlayerColor playerColor) {
		for(Player player: players) {
			if(player.getColor() == playerColor) {
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
		if(getZonesOwnedbyPlayer(player).isEmpty()) {
			player.setEliminated();
			setEliminiatedPlayer(player);	
		}
	}


    /**
     * Instantiates a new object of the neighbours Array List, fills it with all the zone, in which a player can move
     * his troops units from the origin zone (If the zone belongs to him) and returns this list.
     * @param originZone The Zone, from which the player wants to move his troop units away
     * @param player The player instance of the current player.
     * @return A list, in which all the possible movement zones for a player and the specified origin zone are contained.
     */
    public ArrayList<Zone> getPossibleMovementNeighbours(Zone originZone, Player player) {
        ArrayList<Zone> movementNeighbours = new ArrayList<Zone>();
        createNeighboursList(originZone, player, movementNeighbours);
        for(int i=0; i<movementNeighbours.size();i++){
            if(movementNeighbours.get(i)==originZone){
                movementNeighbours.remove(i);
            }
        }
        return movementNeighbours;
    }

    private void createNeighboursList(Zone originZone, Player player, ArrayList<Zone> movementNeighbours) {
        originZone.setAlreadyVisited(true);
        for (Zone adjacentZone : originZone.getNeighbours()) {
            if (!adjacentZone.getAlreadyVisited()
                    && getZoneOwner(adjacentZone) == player) {
                createNeighboursList(adjacentZone, player, movementNeighbours);
            }
        }
        if (originZone.getAlreadyVisited()&&!alreadyInList(originZone, movementNeighbours)) {
            originZone.setAlreadyVisited(false);
            movementNeighbours.add(originZone);
        }
    }

    private boolean alreadyInList (Zone zone, ArrayList<Zone> movementNeighbours){
        boolean alreadyAdded = false;
        for (int i = 0;i<movementNeighbours.size();i++){
            if(movementNeighbours.get(i)==zone){
                alreadyAdded=true;
            }
        }
        return alreadyAdded;
    }
    
    public PlayerColor getEliminatedPlayer() {
    	return eliminatedPlayer.get();
    }   
    public ObjectProperty<PlayerColor> getEliminiatedPlayerProperty() {
    	return eliminatedPlayer;
    }   
    public void setEliminiatedPlayer(Player player) {
    	eliminatedPlayer.set(player.getColor());
    }

    public PlayerColor getNewRegionOwner() {
    	return newRegionOwner.get();
    }
    public ObjectProperty<PlayerColor> getNewRegionOwnerProperty(){
    	return newRegionOwner;
    }
    public void setNewRegionOwner(Player player) {
    	newRegionOwner.set(player.getColor());
    }
}
