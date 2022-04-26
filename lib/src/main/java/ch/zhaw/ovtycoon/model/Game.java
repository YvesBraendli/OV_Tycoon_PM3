package ch.zhaw.ovtycoon.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ch.zhaw.ovtycoon.Config;
import ch.zhaw.ovtycoon.Config.RegionName;
import ch.zhaw.ovtycoon.Config.ZoneName;

public class Game {
	private ArrayList<Region> regions;
	
	
	public void initGame(int playerAmount) {
		regions = new ArrayList<Region>();
		ArrayList<Zone> Unterland = new ArrayList<Zone>();
		Unterland.add(new Zone(ZoneName.Zone114));
		Unterland.add(new Zone(ZoneName.Zone113));
		Unterland.add(new Zone(ZoneName.Zone112));
		Unterland.add(new Zone(ZoneName.Zone121));
		Unterland.add(new Zone(ZoneName.Zone111));
		Unterland.add(new Zone(ZoneName.Zone117));
		Unterland.add(new Zone(ZoneName.Zone154));
		Unterland.add(new Zone(ZoneName.Zone184));
		regions.add(new Region(RegionName.Unterland, Unterland));
		
		ArrayList<Zone> Weinland = new ArrayList<Zone>();
		Weinland.add(new Zone(ZoneName.Zone115));
		Weinland.add(new Zone(ZoneName.Zone124));
		Weinland.add(new Zone(ZoneName.Zone123));
		Weinland.add(new Zone(ZoneName.Zone120));
		Weinland.add(new Zone(ZoneName.Zone170));
		Weinland.add(new Zone(ZoneName.Zone171));
		Weinland.add(new Zone(ZoneName.Zone164));
		Weinland.add(new Zone(ZoneName.Zone163));
		Weinland.add(new Zone(ZoneName.Zone160));
		Weinland.add(new Zone(ZoneName.Zone161));
		Weinland.add(new Zone(ZoneName.Zone162));
		regions.add(new Region(RegionName.Weinland, Weinland));
		
		ArrayList<Zone> Oberland = new ArrayList<Zone>();
		Oberland.add(new Zone(ZoneName.Zone122));
		Oberland.add(new Zone(ZoneName.Zone135));
		Oberland.add(new Zone(ZoneName.Zone172));
		Oberland.add(new Zone(ZoneName.Zone173));
		Oberland.add(new Zone(ZoneName.Zone134));
		Oberland.add(new Zone(ZoneName.Zone133));
		Oberland.add(new Zone(ZoneName.Zone132));
		Oberland.add(new Zone(ZoneName.Zone131));
		Oberland.add(new Zone(ZoneName.Zone130));
		regions.add(new Region(RegionName.Oberland, Oberland));
		
		ArrayList<Zone> MeilenZurich = new ArrayList<Zone>();
		MeilenZurich.add(new Zone(ZoneName.Zone143));
		MeilenZurich.add(new Zone(ZoneName.Zone141));
		MeilenZurich.add(new Zone(ZoneName.Zone130));
		MeilenZurich.add(new Zone(ZoneName.Zone140));
		MeilenZurich.add(new Zone(ZoneName.Zone180));
		MeilenZurich.add(new Zone(ZoneName.Zone110));
		regions.add(new Region(RegionName.MeilenZurich, MeilenZurich));
		
		
		ArrayList<Zone> HorgenAlbis = new ArrayList<Zone>();
		HorgenAlbis.add(new Zone(ZoneName.Zone155));
		HorgenAlbis.add(new Zone(ZoneName.Zone156));
		HorgenAlbis.add(new Zone(ZoneName.Zone151));
		HorgenAlbis.add(new Zone(ZoneName.Zone152));
		HorgenAlbis.add(new Zone(ZoneName.Zone153));
		HorgenAlbis.add(new Zone(ZoneName.Zone150));
		HorgenAlbis.add(new Zone(ZoneName.Zone181));
		regions.add(new Region(RegionName.HorgenAlbis, HorgenAlbis));
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
