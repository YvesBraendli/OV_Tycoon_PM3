package ch.zhaw.ovtycoon.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class Zone implements Serializable {
	private String name;
	private int troops;
	private ArrayList<Zone> neighbours;
	private boolean alreadyVisited;

	public Zone(String name) {
		this.name = name;
		neighbours = new ArrayList<Zone>();
		troops = 0;
		alreadyVisited = false;
	}

	public void addTroops(int amount){
		this.troops += amount;
	}

	public boolean getAlreadyVisited(){
		return alreadyVisited;
	}

	public void setAlreadyVisited(boolean alreadyVisitedState){
		alreadyVisited = alreadyVisitedState;
	}
	
	public String getName() {
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
	
	public void setNeighbours(ArrayList<Zone> neighbours) {
		this.neighbours = neighbours;
	}
	
	public ArrayList<Zone> getNeighbours(){
		return neighbours;
	}
	
	public boolean isNeighbour(Zone zone) {
		if(neighbours.contains(zone)) return true;
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null) return false;
		if (getClass() != o.getClass()) return false;
		Zone zone = (Zone) o;
		return Objects.equals(name, zone.name);
	}
	
	
}
