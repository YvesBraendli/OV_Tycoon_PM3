package ch.zhaw.ovtycoon.model;

import ch.zhaw.ovtycoon.Config.ZoneName;

public class Zone {
	private ZoneName name;
	private int troops;
	private Player owner;
	
	public Zone(ZoneName name) {
		this.name = name;
	}
	
	public Player getOwner() {
		return owner;
	}
	
	public void setOwner(Player player) {
		owner = player;
	}
	
	public ZoneName getName() {
		return name;
	}
	
	public void decreaseZone(int amount) {
		troops -= amount;
	}
	
	public int getTroops() {
		return troops;
	}

	public void setTroops(int troops) {
		this.troops = troops;
	}
}
