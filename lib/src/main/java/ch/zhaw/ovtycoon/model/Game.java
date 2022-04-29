package ch.zhaw.ovtycoon.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.*;

import ch.zhaw.ovtycoon.Config;
import ch.zhaw.ovtycoon.Config.RegionName;

public class Game {
	private HashMap<Config.RegionName, ArrayList<Zone>> gameMap;
	private HashMap<Zone,Player> zoneOwner = new HashMap<Zone,Player>();
	private Player[] players;
	
	/**
	 * Initializes the gameMap and creates players with their corresponding colors
	 * @param playerAmount number of players
	 */
	public void initGame(int playerAmount) {
		MapInitializer mapInit = new MapInitializer();
		gameMap = mapInit.getGameMap();	
		zoneOwner = mapInit.getOwnerList();
		players = new Player[playerAmount];
		//TODO get player color and name
	}
	
	/**
	 * starts the game
	 */
	public void start() {
		//TODO implement game flow, update JavaDoc
	}
	
	/**
	 * Checks for a winner. A winner is defined by owning all zones
	 * @return
	 */
	public Player getWinner() {
		Player winner = zoneOwner.get(gameMap.get(RegionName.Oberland).get(0));
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
		for (Zone zone : gameMap.get(region)) {
			if (!regionOwner.equals(zoneOwner.get(zone)))
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
}
