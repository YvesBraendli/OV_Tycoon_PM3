package ch.zhaw.ovtycoon.model;

import java.util.HashMap;

import ch.zhaw.ovtycoon.Config;

public class TroopHandler {
	private int numberOfTroopsPerPlayer;
	private HashMap<Zone,Integer> numberOfTroopsPerZone = new HashMap<Zone,Integer>();
	
	public TroopHandler (int numberOfPlayers) {
		numberOfTroopsPerPlayer = Config.NUMBER_OF_TROOPS_TOTAL_IN_GAME/numberOfPlayers;
	}
	
	public boolean moveUnits() {
		return true;
	}
	
	/**
	 * Gives the current number of troops back, which on the correspondents on the actual number of Players in game.
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
