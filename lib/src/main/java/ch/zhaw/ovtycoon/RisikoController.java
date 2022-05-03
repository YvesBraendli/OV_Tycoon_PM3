package ch.zhaw.ovtycoon;

import java.util.ArrayList;

import ch.zhaw.ovtycoon.Config.PlayerColor;
import ch.zhaw.ovtycoon.Config.RegionName;
import ch.zhaw.ovtycoon.gui.MapController;
import ch.zhaw.ovtycoon.model.Game;
import ch.zhaw.ovtycoon.model.Player;
import ch.zhaw.ovtycoon.model.Zone;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RisikoController extends Application {

	private Game game;
	
    @Override
    public void start(Stage stage) throws Exception {
    	game = new Game();
    	game.initGame(2);
    	
        FXMLLoader fxmlLoader = new FXMLLoader(MapController.class.getClassLoader().getResource("zones-map-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 1000);
        stage.setTitle("OV-Tycoon");
        stage.setScene(scene);
        stage.show();
    }
    
    public int[][] getLastRolledDie(){
    	return game.getLastRolledDie();
    }
    
    public PlayerColor getWinner() {
    	if(game.hasWinner()) {
    		return game.getWinner().getColor();
    	}
    	return null;
    }
    
    public PlayerColor getRegionOwner(RegionName region) {
    	Player regionOwner = game.getRegionOwner(region);
    	if(regionOwner != null) {
    		return regionOwner.getColor();
    	}
    	return null;
    }
    
    public PlayerColor getZoneOwner(String zoneName) {
    	return game.getZoneOwner(game.getZone(zoneName)).getColor();
    }
    
    public boolean isZoneOwner(String zoneName, PlayerColor playerColor) {
    	return game.isZoneOwner(game.getPlayer(playerColor), game.getZone(zoneName));
    }
    
    public boolean isValidAttack(String zoneNameAttacking, String zoneNameAttacked, PlayerColor attackerColor) {
    	if(isZoneOwner(zoneNameAttacking, attackerColor) && !isZoneOwner(zoneNameAttacked, attackerColor)) return true;
    	return false;
    }
    
    public ArrayList<String> getZonesOwnedbyPlayer(PlayerColor playerColor){
    	return translateZoneListToNameList(game.getZonesOwnedbyPlayer(game.getPlayer(playerColor)));
    }
    
    public ArrayList<String> getAttackableZones(PlayerColor playerColor){
    	return translateZoneListToNameList(game.getAttackableZones(game.getPlayer(playerColor)));
    }
    
    
    
    private ArrayList<String> translateZoneListToNameList(ArrayList<Zone> zoneList){
    	ArrayList<String> zoneNameList = new ArrayList<String>();
    	for(Zone zone: zoneList) {
    		zoneNameList.add(zone.getName());
    	}
    	return zoneNameList;
    }
    
    
    
    
    
    
    
}
