package ch.zhaw.ovtycoon.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.*;

import ch.zhaw.ovtycoon.Config;
import ch.zhaw.ovtycoon.RisikoController;
import ch.zhaw.ovtycoon.Config.RegionName;
import javafx.application.Application;


public class Game {
	private HashMap<Config.RegionName, ArrayList<Zone>> gameMap;
	private HashMap<Zone,Player> zoneOwner = new HashMap<Zone,Player>();
	private Player[] players;
	private int[][] lastRolledDie;
	private TroopHandler troopHandler;

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
		//TODO get player color and name
	}
	

	
	/**
	 * starts the game
	 */
	public void start() {
		//TODO implement game flow, update JavaDoc
		
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
	 * @return
	 */
	public Player getWinner() {
		Player winner = zoneOwner.get(gameMap.get(RegionName.Oberland).get(0));
		if(winner == null) return null;
		for(Entry<Zone,Player> zone: zoneOwner.entrySet()) {
			if(!winner.equals(zone.getValue())) return null;
		}
		return winner;
	}
	
	public boolean hasWinner() {
		if(getWinner() != null) return true;
		return false;
	}
	/**
	 * Checks if a player owns all zones in a region
	 * @param region
	 * @return Player
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
	
	public ArrayList<Zone> getZonesOwnedbyPlayer(Player player){
		ArrayList<Zone> zonesOwnedByPlayer = new ArrayList<Zone>();
		for(Zone zone: zoneOwner.keySet()) {
			if(isZoneOwner(player, zone)) {
				zonesOwnedByPlayer.add(zone);
			}
		}
		return zonesOwnedByPlayer;
	}
	
	public ArrayList<Zone> getAttackableZones(Player player){
		ArrayList<Zone> zonesOwnedByPlayer = getZonesOwnedbyPlayer(player);
		ArrayList<Zone> neighbourZones = new ArrayList<Zone>();
		ArrayList<Zone> currentNeighbours = new ArrayList<Zone>();
		
		for(Zone zone: zonesOwnedByPlayer) {
			if(zone.getTroops()>1) {
				currentNeighbours = zone.getNeighbours();
				currentNeighbours.removeAll(zonesOwnedByPlayer);
				neighbourZones.addAll(currentNeighbours);
			}
		}
		
		return neighbourZones;
		
	}
	
	public ArrayList<Zone> getPossibleAttackerZones(Player player){
		ArrayList<Zone> zonesOwnedByPlayer = getZonesOwnedbyPlayer(player);
		ArrayList<Zone> possibleAttackerZones = new ArrayList<Zone>();
		for(Zone zone: zonesOwnedByPlayer) {
			if(zone.getTroops()>1) {
				for(Zone neighbour : zone.getNeighbours()) {
					if(zoneOwner.get(neighbour) != player) {
						possibleAttackerZones.add(zone);
					}
				}
			}
		}
		return possibleAttackerZones;
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
	 * Returs the last set of rolled die
	 * @return a 2d array containing the defenders roll in the first column and the attackers in the second
	 */
	public int[][] getLastRolledDie(){
		return lastRolledDie;
	}
}
