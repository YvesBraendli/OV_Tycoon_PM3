package ch.zhaw.ovtycoon.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.*;

import ch.zhaw.ovtycoon.Config;
import ch.zhaw.ovtycoon.RisikoController;
import ch.zhaw.ovtycoon.Config.RegionName;
import javafx.application.Application;

public class Game {
	private HashMap<Config.RegionName, ArrayList<Zone>> gameMap;
	private HashMap<Zone,Player> zoneOwner = new HashMap<Zone,Player>();
	private Player[] players;
	private TroopHandler troopHandler;

	public static void main(String[] args) {
		Game game = new Game();
		game.initGame(5);

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
	 * Returns a zone object
	 * @param zoneName enum
	 * @return zone
	 */
	public Zone getZone(Config.ZoneName zoneName) {
		for(Zone zone: zoneOwner.keySet()) {
			if(zone.getName().equals(zoneName.toString())) {
				return zone;
			}
		}
		return null;
	}
}
