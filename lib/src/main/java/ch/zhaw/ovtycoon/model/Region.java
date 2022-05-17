package ch.zhaw.ovtycoon.model;

import java.io.Serializable;
import java.util.ArrayList;

import ch.zhaw.ovtycoon.Config.RegionName;

public class Region implements Serializable {
    private RegionName name;
    private ArrayList<Zone> zones;

    public Region(RegionName name, ArrayList<Zone> zones) {
        this.name = name;
        this.zones = zones;
    }

    public ArrayList<Zone> getZones() {
        return zones;
    }
}
