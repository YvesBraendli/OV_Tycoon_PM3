package ch.zhaw.ovtycoon;

import java.util.ArrayList;

import ch.zhaw.ovtycoon.Config.PlayerColor;
import ch.zhaw.ovtycoon.Config.RegionName;
import ch.zhaw.ovtycoon.data.DiceRoll;
import ch.zhaw.ovtycoon.gui.MapController;
import ch.zhaw.ovtycoon.model.Game;
import ch.zhaw.ovtycoon.model.Player;
import ch.zhaw.ovtycoon.model.Zone;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Interface between ÖVTycoon front- and backend
 * Translates ZoneName String and Player Colors into the corresponding player and zone objects stored in the backend
 */
public class RisikoController{

	private Game game;
	
	/**
	 * Constructor of Interface, instantiates game with number of Players
	 * @param numberOfPlayers - amount of players that will play the game
	 */
	public RisikoController(int numberOfPlayers) {
		game = new Game();
    	game.initGame(numberOfPlayers);
	}

    /**
     * starts a fight, between two zones
     * After the fight a zone gets reassigned if the defender does not have any more troops stationed.
     * Additionally it gets checked if the defender was eliminated and if the attacker now owns a new region.
     * 
     * @param attacker - attacking zone
     * @param defender - defending zone
     * @param numOfAttacker - number of troops, which will attack
     * @param numOfDefender - number of troops, which will defend
     * @return a Data Transfer Object (DTO) of the rolls made
     */
    public DiceRoll runFight(String attacker, String defender, int numOfAttacker, int numOfDefender){
    	return game.runFight(game.getZone(attacker), game.getZone(defender), numOfAttacker, numOfDefender);
    }
    
	/**
	 * Checks for a winner. A winner is defined by owning all zones
	 * @return Color of the winner
	 */
    public PlayerColor getWinner() {
    	if(game.hasWinner()) {
    		return game.getWinner().getColor();
    	}
    	return null;
    }
    
	/**
	 * Checks if a player owns all zones in a region
	 * @param region 
	 * @return Player the owner of the region or null
	 */
    public PlayerColor getRegionOwner(RegionName region) {
    	Player regionOwner = game.getRegionOwner(region);
    	if(regionOwner != null) {
    		return regionOwner.getColor();
    	}
    	return null;
    }
    
	/**
	 * Gets the current owner for a zone
	 * @param zoneName
	 * @return player color
	 */
    public PlayerColor getZoneOwner(String zoneName) {
    	return game.getZoneOwner(game.getZone(zoneName)).getColor();
    }
    
	/**
	 * Gets current player
	 * @return the current player by color
	 */
    public PlayerColor getCurrentPlayer() {
    	return game.getCurrentPlayer().getColor();
    }
    
	/**
	 * Switches to next player in the list and to the first if its currently the last players turn
	 */
    public void switchToNextPlayer() {
    	game.switchToNextPlayer();
    }
    
    /**
     * Gets the number of troops on a zone
     * @param zoneName
     * @return int number of troops
     */
    public int getZoneTroops(String zoneName) {
    	return game.getZone(zoneName).getTroops();
    }
    
	/**
	 * Checks if a player owns a specific zone
	 * @param player color
	 * @param zone name
	 * @return true if the player owns the zone, false if not
	 */
    public boolean isZoneOwner(String zoneName, PlayerColor playerColor) {
    	return game.isZoneOwner(game.getPlayer(playerColor), game.getZone(zoneName));
    }
    
    /**
     * Checks if a planned attack is valid
     * @param zoneNameAttacking
     * @param zoneNameAttacked
     * @param attackerColor
     * @return true if valid else false
     */ //TODO check if neighbour
    public boolean isValidAttack(String zoneNameAttacking, String zoneNameAttacked, PlayerColor attackerColor) {
    	if(isZoneOwner(zoneNameAttacking, attackerColor) && !isZoneOwner(zoneNameAttacked, attackerColor)) return true;
    	return false;
    }
    
	/**
	 * Searches for all zones which are oned by a specific player
	 * @param player to search zones for
	 * @return ArrayList of the zones owned by the player
	 */
    public ArrayList<String> getZonesOwnedbyPlayer(PlayerColor playerColor){
    	return translateZoneListToNameList(game.getZonesOwnedbyPlayer(game.getPlayer(playerColor)));
    }
    
	/**
	 * Gets zones which are classified as "attackable"
	 * An attackable zone needs to be next to a zone owned by the player and must not be owned by the player
	 * @param zone the attacker zone
	 * @return ArrayList of the zones that can be attacked from the current zone
	 */
    public ArrayList<String> getAttackableZones(String attackerZoneName){
    	return translateZoneListToNameList(game.getAttackableZones(game.getZone(attackerZoneName)));
    }
    
	/**
	 * Gets zones which can be used to attack. 
	 * This is defined by having more than one troop on the zone
	 * and being next to a zone which is not owned by the player
	 * @param player to check for
	 * @return ArrayList of possible attacking zones
	 */
    public ArrayList<String> getPossibleAttackerZones(PlayerColor playerColor){
    	return translateZoneListToNameList(game.getPossibleAttackerZones(game.getPlayer(playerColor)));
    }
    
	/**
	 * Gets zones which have movable troops
	 * zones have movable troops if they have more than one troop on them 
	 * and have a neighborzone which is owned by the same player
	 * @param player to check for
	 * @return ArrayList of zones with movable troops
	 */
    public ArrayList<String> getZonesWithMovableTroops(PlayerColor playerColor){
    	return translateZoneListToNameList(game.getZonesWithMovableTroops(game.getPlayer(playerColor)));
    }
    
	/**
	 * gets neighbours of zone
	 * @param zone
	 * @return ArrayList with neighbours
	 */
    public ArrayList<String> getZoneNeighbours(String zoneName){
    	return translateZoneListToNameList(game.getZoneNeighbours(game.getZone(zoneName)));
    }
    
    /**
     * Instantiates a new object of the neighbours Array List, fills it with all the zone, in which a player can move
     * his troops units from the origin zone (If the zone belongs to him) and returns this list.
     * @param zoneName The Zone, from which the player wants to move his troop units away
     * @param playerColor The player instance of the current player.
     * @return A list, in which all the possible movement zones for a player and the specified origin zone are contained.
     */
    public ArrayList<String> getPossibleMovementNeighbours(String zoneName, PlayerColor playerColor) {
    	return translateZoneListToNameList(game.getPossibleMovementNeighbours(game.getZone(zoneName),game.getPlayer(playerColor)));
    }
    
    /**
     * Gets the eliminated player property to implement a listener.
     * The property changes if after a fight a player does not own any more zones
     */
    public ObjectProperty<PlayerColor> getEliminatedPlayerProperty(){
    	return game.getEliminiatedPlayerProperty();
    }
    
    /**
     * Gets the new region owner property to implement a listener.
     * The property changes if after a fight a player owns a new region.
     */
    public ObjectProperty<PlayerColor> getNewRegionOwnerProperty(){
    	return game.getEliminiatedPlayerProperty();
    }
    
    private ArrayList<String> translateZoneListToNameList(ArrayList<Zone> zoneList){
    	ArrayList<String> zoneNameList = new ArrayList<String>();
    	for(Zone zone: zoneList) {
    		zoneNameList.add(zone.getName());
    	}
    	return zoneNameList;
    }
    
    
    
    
    
    
    
    
    
    
    
}
