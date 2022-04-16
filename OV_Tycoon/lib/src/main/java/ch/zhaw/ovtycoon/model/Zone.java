package ch.zhaw.ovtycoon.model;

import ch.zhaw.ovtycoon.Config.ZoneName;

public class Zone {
	private ZoneName name;
	
	public Zone(ZoneName name) {
		this.name = name;
	}
	
	public ZoneName getName() {
		return name;
	}
}
