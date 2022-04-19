package ch.zhaw.ovtycoon.model;

import java.util.ArrayList;
import ch.zhaw.ovtycoon.Config.RegionName;

public class Region {
	private RegionName name;
	private ArrayList<Zone> zones = new ArrayList<Zone>();
	
	public Region(RegionName name, ArrayList<Zone> zones ) {
		this.name = name;
		this.zones = zones;
	}
	
	public ArrayList<Zone> getZones() {
		return zones;
	}
}
