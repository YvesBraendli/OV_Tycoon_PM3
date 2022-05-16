package ch.zhaw.ovtycoon;

import ch.zhaw.ovtycoon.Config.PlayerColor;
import ch.zhaw.ovtycoon.Config.RegionName;
import ch.zhaw.ovtycoon.data.DiceRoll;
import ch.zhaw.ovtycoon.gui.model.Action;
import ch.zhaw.ovtycoon.model.Game;
import ch.zhaw.ovtycoon.data.Player;
import ch.zhaw.ovtycoon.model.Zone;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
	 * Initializes the players with the chosen colors.
	 * The Number of Colors need to be the same as the number of players.
	 * Otherwise, an IllegalArgumentException will be thrown.
	 * @param colors
	 * @throws IllegalArgumentException
	 */
	public void initPlayers(ArrayList<PlayerColor> colors){
		 try {
			 game.initPlayers(colors);
		 }
		 catch (IllegalArgumentException e){
			 throw e;
		 }
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
     * moves troops from one zone to the other
     * no validation is made if the move is valid. for this the method getPossibleMovementNeighbours() should be used
     * @param from zone which the troops get moved from
     * @param to zone which the troops get moved to
     * @param numberOfTroops
     */
    public void moveUnits(String from, String to, int numberOfTroops) {
    	game.moveUnits(game.getZone(from), game.getZone(to), numberOfTroops);
    }

	/**
	 * Adds troops to a zone if owned by current player
	 * @param amount amount of troops to add
	 * @param zone name of the zone to add troops to
	 */
	public void reinforce(int amount, String zone) {
		game.reinforce(amount, game.getZone(zone));
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
	 * @param playerColor color
	 * @param zoneName name
	 * @return true if the player owns the zone, false if not
	 */
    public boolean isZoneOwner(String zoneName, PlayerColor playerColor) {
    	return game.isZoneOwner(game.getPlayer(playerColor), game.getZone(zoneName));
    }
    
    /**
     * Checks if a planned attack is valid
     * An attack is valid if the zone is owned by the player, the attacked zone is not owned by the player
     * and the zones are neighbours
     * 
     * @param zoneNameAttacking The Attacker
     * @param zoneNameAttacked The Defender
     * @param attackerColor Player Color of attacker
     * @return true if valid else false
     */
    public boolean isValidAttack(String zoneNameAttacking, String zoneNameAttacked, PlayerColor attackerColor) {
    	if(isZoneOwner(zoneNameAttacking, attackerColor) 
    			&& !isZoneOwner(zoneNameAttacked, attackerColor)
    					&& getZoneNeighbours(zoneNameAttacking).contains(zoneNameAttacked)) return true;
    	return false;
    }
    
	/**
	 * Searches for all zones which are oned by a specific player
	 * @param playerColor to search zones for
	 * @return ArrayList of the zones owned by the player
	 */
    public ArrayList<String> getZonesOwnedbyPlayer(PlayerColor playerColor){
    	return translateZoneListToNameList(game.getZonesOwnedbyPlayer(game.getPlayer(playerColor)));
    }
    
	/**
	 * Gets zones which are classified as "attackable"
	 * An attackable zone needs to be next to a zone owned by the player and must not be owned by the player
	 * @param attackerZoneName the attacker zone
	 * @return ArrayList of the zones that can be attacked from the current zone
	 */
    public ArrayList<String> getAttackableZones(String attackerZoneName){
    	return translateZoneListToNameList(game.getAttackableZones(game.getZone(attackerZoneName)));
    }
    
	/**
	 * Gets zones which can be used to attack. 
	 * This is defined by having more than one troop on the zone
	 * and being next to a zone which is not owned by the player
	 * @param playerColor to check for
	 * @return ArrayList of possible attacking zones
	 */
    public ArrayList<String> getPossibleAttackerZones(PlayerColor playerColor){
    	return translateZoneListToNameList(game.getPossibleAttackerZones(game.getPlayer(playerColor)));
    }
    
	/**
	 * Gets zones which have movable troops
	 * zones have movable troops if they have more than one troop on them 
	 * and have a neighborzone which is owned by the same player
	 * @param playerColor to check for
	 * @return ArrayList of zones with movable troops
	 */
    public ArrayList<String> getZonesWithMovableTroops(PlayerColor playerColor){
    	return translateZoneListToNameList(game.getZonesWithMovableTroops(game.getPlayer(playerColor)));
    }
    
	/**
	 * gets neighbours of zone
	 * @param zoneName
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
     * Gets the eliminated player color property to implement a listener.
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
    	return game.getNewRegionOwnerProperty(); // TODO doc
    }

	// TODO doc for new methods -------------------------------------------------------------------------------------

	public int getMaxTroopsForAttack(String zoneName) {
		return game.getMaxTroopsForAttack(zoneName);
	}

	public int getMaxTroopsForDefending(String zoneName) {
		return game.getMaxTroopsForDefending(zoneName);
	}

	public int getMaxMovableTroops(String zoneName) {
		return game.getMaxMovableTroops(zoneName);
	}

	// TODO remove as soon as player initialization implemented
	public Player[] getPlayers() {
		return game.getPlayers();
	}

	public PlayerColor[] getPlayerColors() {
		PlayerColor[] playerColors = new PlayerColor[game.getPlayers().length];
		for (int i = 0; i < game.getPlayers().length; i++) {
			playerColors[i] = game.getPlayers()[i].getColor();
		}
		return playerColors;
	}

	public void setZoneOwner(Player owner, String zoneName) {
		game.setZoneOwner(owner, zoneName);
	}

	public void updateZoneTroops(String zone, int troops) {
		game.updateZoneTroops(zone, troops);
	}

	public void nextAction() {
		game.nextAction();
	}

	public Action getAction() {
		return game.getCurrentAction();
	}

	public List<String> getValidTargetZoneNames(String sourceZoneName) {
		return getAction() == Action.ATTACK ? getAttackableZones(sourceZoneName) : getPossibleMovementNeighbours(sourceZoneName, getCurrentPlayer());
	}

	public List<String> getValidSourceZoneNames() {
		switch(getAction()) {
			case ATTACK: return getPossibleAttackerZones(getCurrentPlayer());
			case MOVE: return getZonesWithMovableTroops(getCurrentPlayer());
			default: return getZonesOwnedbyPlayer(getCurrentPlayer());
		}
	}

	public SimpleObjectProperty<PlayerColor> getFightWinner() {
		return game.getFightWinner();
	}

	public SimpleBooleanProperty getZoneOvertaken() {
		return game.getZoneOvertaken();
	}

	public RegionName getRegionByOwner(String zoneName) {
		return game.getRegionOfZone(game.getZone(zoneName));
	}

	// ---------------------------------------------------------------------------------------------------------
    
    private ArrayList<String> translateZoneListToNameList(ArrayList<Zone> zoneList){
    	ArrayList<String> zoneNameList = new ArrayList<String>();
    	for(Zone zone: zoneList) {
    		zoneNameList.add(zone.getName());
    	}
    	return zoneNameList;
    }

	/**
	 * Gets the amount of Reinforcements the current player receives
	 * @return amount of reinforcements
	 */
	public int getAmountOfReinforcements(){
		return game.getAmountOfReinforcements();
	}
}
