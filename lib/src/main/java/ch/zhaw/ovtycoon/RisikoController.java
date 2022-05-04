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

public class RisikoController{

	private Game game;
	
	public RisikoController() {
		game = new Game();
    	game.initGame(2);
	}

    
    public int[][] runFight(String attacker, String defender, int numOfAttacker, int numOfDefender){
    	return game.runFight(game.getZone(attacker), game.getZone(defender), numOfAttacker, numOfDefender);
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
    
    public PlayerColor getCurrentPlayer() {
    	return game.getCurrentPlayer().getColor();
    }
    
    public int getZoneTroops(String zoneName) {
    	return game.getZone(zoneName).getTroops();
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
    
    public ArrayList<String> getAttackableZones(String attackerZoneName, PlayerColor playerColor){
    	return translateZoneListToNameList(game.getAttackableZones(game.getZone(attackerZoneName), game.getPlayer(playerColor)));
    }
    
    public ArrayList<String> getPossibleAttackerZones(PlayerColor playerColor){
    	return translateZoneListToNameList(game.getPossibleAttackerZones(game.getPlayer(playerColor)));
    }
    
    public ArrayList<String> getZonesWithMovableTroops(PlayerColor playerColor){
    	return translateZoneListToNameList(game.getZonesWithMovableTroops(game.getPlayer(playerColor)));
    }
    
    public ArrayList<String> getZoneNeighbours(String zoneName){
    	return translateZoneListToNameList(game.getZoneNeighbours(game.getZone(zoneName)));
    }
    
    private ArrayList<String> translateZoneListToNameList(ArrayList<Zone> zoneList){
    	ArrayList<String> zoneNameList = new ArrayList<String>();
    	for(Zone zone: zoneList) {
    		zoneNameList.add(zone.getName());
    	}
    	return zoneNameList;
    }
    
    
    
    
    
    
    
    
    
}
