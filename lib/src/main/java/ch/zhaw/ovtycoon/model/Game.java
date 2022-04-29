package ch.zhaw.ovtycoon.model;

import java.util.ArrayList;
import java.util.HashMap;
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
	
	public Player getWinner() {
		Player winner = zoneOwner.get(gameMap.get(RegionName.Oberland).get(0));
		for(Entry<Config.RegionName, ArrayList<Zone>> region: gameMap.entrySet()) {
			if(!winner.equals(getRegionOwner(region.getKey()))) return null;
		}
		return winner;
	}
	
	public Player getRegionOwner(RegionName region) {
		Player regionOwner = zoneOwner.get(gameMap.get(region).get(0));
		for (Zone zone : gameMap.get(region)) {
			if (!regionOwner.equals(zoneOwner.get(zone)))
				return null;
		}
		return regionOwner;
	}
	
	public void setZoneOwner(Player owner) {
		//TODO Implement setZoneOwner method
	}
}
