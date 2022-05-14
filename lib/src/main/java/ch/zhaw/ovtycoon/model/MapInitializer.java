package ch.zhaw.ovtycoon.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.zhaw.ovtycoon.Config;

/**
 * The MapInitializer is used to initialize the regions and zones at the start of a game
 * from corresponding config files 
 *
 */
public class MapInitializer {

    private final String regionFileName = "regions.txt";
    private final String neighboursFileName = "neighbours.txt";
    HashMap<String,Zone> zonesByName;
    ArrayList<Zone> neighbours;

    /**
     * Constructor of MapInitializer
     * Generates a Map of the Zones by their Names
     */
    public MapInitializer() {
        zonesByName = new HashMap<String,Zone>();
        initZones();
        initZoneNeighbours();
    }
    /**
     * Generates a HashMap with the Regions as Keys and the corresponding zones as values
     * @return HashMap<Config.RegionName, ArrayList<Zone>>
     */
    public HashMap<Config.RegionName, ArrayList<Zone>> getGameMap() {
        HashMap<Config.RegionName, ArrayList<Zone>> gameMap = new HashMap<Config.RegionName, ArrayList<Zone>>();;
        Pattern regionData = Pattern.compile("name=([a-zA-Z]+)");
        Pattern zoneData = Pattern.compile("Zone[0-9]{3}");

        try {
            File regionFile = getRessource(regionFileName);
            try (BufferedReader br = new BufferedReader(new FileReader(regionFile))) {
                String line;
                while ((line = br.readLine()) != null) {

                    ArrayList<Zone> zonesInRegion = new ArrayList<Zone>();
                    Matcher matcherZones = zoneData.matcher(line);
                    while(matcherZones.find()) {
                        zonesInRegion.add(zonesByName.get(matcherZones.group(0)));
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

    /**
     * Generates a Zone to Player HashMap, which can be used to determine ownership over a zone
     * @return HashMap<Zone,Player>
     */
    public HashMap<Zone,Player> getOwnerList(){
        HashMap<Zone,Player> owner = new HashMap<Zone,Player>();
        for(Map.Entry<String, Zone> entry : zonesByName.entrySet()) {
            owner.put(entry.getValue(), null);
        }
        return owner;
    }

    private File getRessource(String file) {
        String dir = System.getProperty("user.dir");
        if (!dir.contains("lib")) {
            dir += "/lib";
        }
        String regionTxtPath = dir + "/src/main/resources/"+file;
        File regionFile = new File(regionTxtPath);
        return regionFile;
    }

    private void initZones() {
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
    }

    private void initZoneNeighbours() {
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
                        if(!firstMatch) neighbours.add(zonesByName.get(matcherNeighbours.group(0)));
                        firstMatch = false;
                    }
                    Matcher matcherZone = zoneName.matcher(line);
                    if(matcherZone.find()) {
                        zonesByName.get(matcherZone.group(1)).setNeighbours(neighbours);;
                    }
                }

            }
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void placetroopsForPlayerAmounts(int playerAmount) {
    	int numberOfTroopsPerPlayer = Config.TROOPS_PER_PLAYER_AMOUNT.get(playerAmount);
    	ArrayList<Zone> zonesWithNoTroops = new ArrayList<>(zonesByName.values());
    	for(int i=0; i < Config.NUMBER_OF_ZONES;i++) {
    		int playerIndex = i%playerAmount;
    		Zone settingZone = zonesWithNoTroops.get(new Random().nextInt(zonesWithNoTroops.size()));
    		settingZone.setTroops(1);
    		
    	}
    }
}