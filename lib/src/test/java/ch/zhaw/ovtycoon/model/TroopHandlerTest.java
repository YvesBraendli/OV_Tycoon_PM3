package ch.zhaw.ovtycoon.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import ch.zhaw.ovtycoon.Config;
import ch.zhaw.ovtycoon.Config.PlayerColor;

import org.junit.Before;
import org.junit.Test;

public class TroopHandlerTest {

    private TroopHandler troopHandlerTestOnePlayer;
    private TroopHandler troopHandlerTestMultiplePlayers;
    private Game gameInstance;
    private int numberOfTroopsInZone;
    private int troopUnitsToMove;
    private int oddBigPlayerNumber;
    private int evenBigPlayerNumber;

    @Before
    public void init() {
        troopUnitsToMove = 5;
        oddBigPlayerNumber = 15;
        evenBigPlayerNumber = 16;
        troopHandlerTestOnePlayer = new TroopHandler(1);
        gameInstance = new Game();
        ArrayList<PlayerColor> colors = new ArrayList<>();
		colors.add(PlayerColor.BLACK);
        gameInstance.initGame(colors);
        clearBoard(colors);
        numberOfTroopsInZone = 20;
        fillTroopUnitsInZone(gameInstance.getZone("Zone140"));
        fillTroopUnitsInZone(gameInstance.getZone("Zone141"));

    }
    
	private void clearBoard(ArrayList<PlayerColor> colors) {
		for(PlayerColor c : colors)
		for(Zone zone: gameInstance.getZonesOwnedbyPlayer(gameInstance.getPlayer(c))) {
			zone.setTroops(0);
			gameInstance.setZoneOwner(null, zone);
		}
	}

    @Test
    public void movedRigthAmountsOfUnits() {
        //Arrange
        int troopsUnitsAfterMovementInOriginZone = gameInstance.getZone("Zone140").getTroops() - troopUnitsToMove;
        int troopsUnitsAfterMovementInTargetZone = gameInstance.getZone("Zone141").getTroops() + troopUnitsToMove;
        //Act
        troopHandlerTestOnePlayer.moveUnits(gameInstance.getZone(("Zone140")),
                gameInstance.getZone(("Zone141")), troopUnitsToMove);

        //Test
        assertTrue(troopsUnitsAfterMovementInOriginZone == gameInstance.getZone("Zone140").getTroops());
        assertTrue(troopsUnitsAfterMovementInTargetZone == gameInstance.getZone("Zone141").getTroops());
    }

    @Test
    public void movedUnitsToTheRightZone() {
        //Arrange
        int troopsUnitsBeforeMovementInTargetZone = gameInstance.getZone("Zone141").getTroops();
        //Act
        troopHandlerTestOnePlayer.moveUnits(gameInstance.getZone(("Zone140")),
                gameInstance.getZone(("Zone141")), troopUnitsToMove);

        //Test
        assertTrue(troopsUnitsBeforeMovementInTargetZone < gameInstance.getZone("Zone141").getTroops());
    }

    @Test
    public void removedUnitsFromTheRightZone() {
        //Arrange
        int troopsUnitsBeforeMovementInOriginZone = gameInstance.getZone("Zone140").getTroops();
        //Act
        troopHandlerTestOnePlayer.moveUnits(gameInstance.getZone(("Zone140")),
        		gameInstance.getZone(("Zone141")),troopUnitsToMove);
       //Test
        assertTrue(troopsUnitsBeforeMovementInOriginZone > gameInstance.getZone("Zone140").getTroops());
    }

    @Test


    public void unitDistributionWithOddBigPlayerNumberRight() {
        //Arrange
        int numberOfTroopsPerPlayer = Config.NUMBER_OF_TROOPS_TOTAL_IN_GAME / oddBigPlayerNumber;
        troopHandlerTestMultiplePlayers = new TroopHandler(oddBigPlayerNumber);
        //Test
        assertTrue(numberOfTroopsPerPlayer == troopHandlerTestMultiplePlayers.getNumberOfTroopsPerPlayer());
    }

    @Test
    public void unitDistributionWithEvenBigPlayerNumberRight() {
        //Arrange
        int numberOfTroopsPerPlayer = Config.NUMBER_OF_TROOPS_TOTAL_IN_GAME / evenBigPlayerNumber;
        troopHandlerTestMultiplePlayers = new TroopHandler(evenBigPlayerNumber);
        //Test
        assertTrue(numberOfTroopsPerPlayer == troopHandlerTestMultiplePlayers.getNumberOfTroopsPerPlayer());
    }

    private void fillTroopUnitsInZone(Zone zone) {
        zone.setTroops(numberOfTroopsInZone);
    }
}
