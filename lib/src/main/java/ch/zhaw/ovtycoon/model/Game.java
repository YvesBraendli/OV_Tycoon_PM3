package ch.zhaw.ovtycoon.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ch.zhaw.ovtycoon.Config;

public class Game {
	private int currentRound = 0;
	private HashMap<Player,ArrayList<Zone>> ownedZones;
	
	
	public void initGame(int playerAmount) {
		ownedZones = new HashMap<Player,ArrayList<Zone>>();
		
		//for(int i = 0; i <= playerAmount; i++) {
		//	addPlayer(new Player(null)); //TODO create players with name and color
		//}	
	}
	
	public void start() {
		//TODO implement game flow
	}
	
	public boolean hasWinner() {
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
	
	public boolean addPlayer(Player player) {
		if(!ownedZones.containsKey(player)) {
			ownedZones.put(player, new ArrayList<Zone>());
			return true;
		}
		return false;
	}
	
	public void assignZone(Player player, Zone zone) {
		 Player currentOwner = getZoneOwner(zone);
		 if(currentOwner != null) {
			 ownedZones.get(currentOwner).remove(zone);
		 }
		 ownedZones.get(player).add(zone);
	}
	
	public Player getZoneOwner(Zone zone) {
		for(Map.Entry<Player, ArrayList<Zone>> entry: ownedZones.entrySet()) {
			if(entry.getValue().contains(zone)) {
				return entry.getKey();
			}
		}
		return null;
	}
}
