package ch.zhaw.ovtycoon.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.zhaw.ovtycoon.Config;

/**
 * The MapInitializer is used to initialize the regions and zones at the start of a game
 * from corresponding config files 
 *
 */
public class MapInitializer {
	
	private final static String regionFileName = "regions.txt";
	private final static String neighboursFileName = "neighbours.txt";
	
	/**
	 * Generates a HashMap with the Regions as Keys and the corresponding zones as values
	 * @return HashMap<Config.RegionName, ArrayList<Zone>> 
	 */
    public static HashMap<Config.RegionName, ArrayList<Zone>> initGameMap() {
    	HashMap<Config.RegionName, ArrayList<Zone>> gameMap = new HashMap<Config.RegionName, ArrayList<Zone>>();
        Pattern regionData = Pattern.compile("name=([a-zA-Z]+)");
        Pattern zoneData = Pattern.compile("Zone[0-9]{3}");
        HashMap<String,Zone> zones = initZoneNeighbours(initZones());
        
        try {
            File regionFile = getRessource(regionFileName);
            try (BufferedReader br = new BufferedReader(new FileReader(regionFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                	
                	ArrayList<Zone> zonesInRegion = new ArrayList<Zone>();
                    Matcher matcherZones = zoneData.matcher(line);
                    while(matcherZones.find()) {
                    	zonesInRegion.add(zones.get(matcherZones.group(0)));
                    }
                    
                    Matcher matcherRegions = regionData.matcher(line);
                    if(matcherRegions.find()) {
                    	for(int i = 0; i < Config.NUMBER_OF_REGIONS;i++) {
                    		if(Config.RegionName.values()[i].toString().equals(matcherRegions.group(1))) {
                    			gameMap.put(Config.RegionName.values()[i], new ArrayList<Zone>(zonesInRegion));
                    		}
                    	}
                    }
                }
            }
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return gameMap;
    }

	private static File getRessource(String file) {
		String dir = System.getProperty("user.dir");
		if (!dir.contains("lib")) {
		    dir += "/lib";
		}
		String regionTxtPath = dir + "/src/main/resources/"+file;
		File regionFile = new File(regionTxtPath);
		return regionFile;
	}
	
	private static HashMap<String,Zone> initZones() {
		HashMap<String,Zone> zonesByName = new HashMap<String,Zone>();
		Pattern zoneName = Pattern.compile("zone=(\\w+)");
		try {
            File file = getRessource(neighboursFileName);
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    Matcher matcherZone = zoneName.matcher(line);
                    if(matcherZone.find()) {
                    	zonesByName.put(matcherZone.group(1), new Zone(matcherZone.group(1)));
                    }
                }
            }
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
		return zonesByName;
	}
    
    private static HashMap<String,Zone> initZoneNeighbours(HashMap<String,Zone> zones) {
    	Pattern zoneName = Pattern.compile("zone=(\\w+)");
    	Pattern neighbourData = Pattern.compile("Zone[0-9]{3}");
    	try {
            File regionFile = getRessource(neighboursFileName);
            try (BufferedReader br = new BufferedReader(new FileReader(regionFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                	
                	ArrayList<Zone> neighbours = new ArrayList<Zone>();
                    Matcher matcherNeighbours = neighbourData.matcher(line);
                    boolean firstMatch = true;
                    while(matcherNeighbours.find()) {
                    	if(!firstMatch) neighbours.add(zones.get(matcherNeighbours.group(0)));
                    	firstMatch = false;
                    }
                    Matcher matcherZone = zoneName.matcher(line);
                    if(matcherZone.find()) {
                    	zones.get(matcherZone.group(1)).setNeighbours(neighbours);;
                    }
                }
                
            }
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
    	return zones;
    }
}
