package ch.zhaw.ovtycoon.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ch.zhaw.ovtycoon.Config;

public class Game {
	private Player[] players;
	private int currentRound = 0;
	private HashMap<Player,ArrayList<Zone>> ownedZones;
	
	
	public void initGame(int playerAmount) {
		players = new Player[playerAmount];
		//TODO create players with name and color
		
	}
	
	public void start() {
		//TODO implement game flow
	}
	
	public Boolean hasWinner() {
		if(null != getWinner()) return true;
		return false;
	}
	
	public Player getWinner() {
		for(Map.Entry<Player, ArrayList<Zone>> entry: ownedZones.entrySet()) {
			if(entry.getValue().size() == Config.NUMBER_OF_ZONES) {
				return entry.getKey();
			}
		}
		return null;
	}
}
