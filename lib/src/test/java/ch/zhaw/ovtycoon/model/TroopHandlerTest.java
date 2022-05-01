package ch.zhaw.ovtycoon.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ch.zhaw.ovtycoon.Config;
import org.junit.Before;
import org.junit.Test;

public class TroopHandlerTest {

    private TroopHandler troopHandlerTestOnePlayer;
    private TroopHandler troopHandlerTestMultiplePlayers;
    private Game gameInstance;
    private Player playerTest;
    private Player playerTestTwo;
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
        playerTest = new Player("Versuchskaninchen");
        playerTestTwo = new Player("Fake");
        gameInstance.initGame(1);
        numberOfTroopsInZone = 20;
        gameInstance.setZoneOwner(playerTest, gameInstance.getZone(Config.ZoneName.Zone140));
        gameInstance.setZoneOwner(playerTest, gameInstance.getZone(Config.ZoneName.Zone141));
        gameInstance.setZoneOwner(playerTest, gameInstance.getZone(Config.ZoneName.Zone143));
        gameInstance.setZoneOwner(playerTest, gameInstance.getZone(Config.ZoneName.Zone132));
        gameInstance.setZoneOwner(playerTestTwo, gameInstance.getZone(Config.ZoneName.Zone142));
        fillTroopUnitsInZone(gameInstance.getZone(Config.ZoneName.Zone142));
        fillTroopUnitsInZone(gameInstance.getZone(Config.ZoneName.Zone140));
        fillTroopUnitsInZone(gameInstance.getZone(Config.ZoneName.Zone141));
        fillTroopUnitsInZone(gameInstance.getZone(Config.ZoneName.Zone143));
        fillTroopUnitsInZone(gameInstance.getZone(Config.ZoneName.Zone132));
    }

    @Test
    public void moveUnitsNoDirectConnection() {
        //Test
        assertFalse(troopHandlerTestOnePlayer.moveUnits(gameInstance.getZone((Config.ZoneName.Zone140)),
                gameInstance.getZone((Config.ZoneName.Zone132)), playerTest, troopUnitsToMove, gameInstance));

    }

    @Test
    public void movedRigthAmountsOfUnits() {
        //Arrange
        int troopsUnitsAfterMovementInOriginZone = gameInstance.getZone(Config.ZoneName.Zone140).getTroops() - troopUnitsToMove;
        int troopsUnitsAfterMovementInTargetZone = gameInstance.getZone(Config.ZoneName.Zone141).getTroops() + troopUnitsToMove;
        //Act
        troopHandlerTestOnePlayer.moveUnits(gameInstance.getZone((Config.ZoneName.Zone140)),
                gameInstance.getZone((Config.ZoneName.Zone141)), playerTest, troopUnitsToMove, gameInstance);
        //Test
        assertTrue(troopsUnitsAfterMovementInOriginZone == gameInstance.getZone(Config.ZoneName.Zone140).getTroops());
        assertTrue(troopsUnitsAfterMovementInTargetZone == gameInstance.getZone(Config.ZoneName.Zone141).getTroops());
    }

    @Test
    public void movedUnitsToTheRightZone() {
        //Arrange
        int troopsUnitsBeforeMovementInTargetZone = gameInstance.getZone(Config.ZoneName.Zone141).getTroops();
        //Act
        troopHandlerTestOnePlayer.moveUnits(gameInstance.getZone((Config.ZoneName.Zone140)),
                gameInstance.getZone((Config.ZoneName.Zone141)), playerTest, troopUnitsToMove, gameInstance);
        //Test
        assertTrue(troopsUnitsBeforeMovementInTargetZone < gameInstance.getZone(Config.ZoneName.Zone141).getTroops());
    }

    @Test
    public void removedUnitsFromTheRightZone() {
        //Arrange
        int troopsUnitsBeforeMovementInOriginZone = gameInstance.getZone(Config.ZoneName.Zone140).getTroops();
        //Act
        troopHandlerTestOnePlayer.moveUnits(gameInstance.getZone((Config.ZoneName.Zone140)),
                gameInstance.getZone((Config.ZoneName.Zone141)), playerTest, troopUnitsToMove, gameInstance);
        //Test
        assertTrue(troopsUnitsBeforeMovementInOriginZone > gameInstance.getZone(Config.ZoneName.Zone140).getTroops());
    }

    @Test
    public void moveUnitsWithUnitNumberLowerThanZeroAfterMovementInOrigin() {
        //Arrange
        int toManyTroopsToMove = gameInstance.getZone(Config.ZoneName.Zone140).getTroops() + 1;
        //Test
        assertFalse(troopHandlerTestOnePlayer.moveUnits(gameInstance.getZone((Config.ZoneName.Zone140)),
                gameInstance.getZone((Config.ZoneName.Zone141)), playerTest, toManyTroopsToMove, gameInstance));
    }

    @Test
    public void moveUnitsSuccessfully() {
        assertTrue(troopHandlerTestOnePlayer.moveUnits(gameInstance.getZone((Config.ZoneName.Zone140)),
                gameInstance.getZone((Config.ZoneName.Zone141)), playerTest, troopUnitsToMove, gameInstance));
    }

    @Test
    public void invalidTroopMovementPlayerIsNotZoneOwner() {
        assertFalse(troopHandlerTestOnePlayer.moveUnits(gameInstance.getZone((Config.ZoneName.Zone140)),
                gameInstance.getZone((Config.ZoneName.Zone142)), playerTest, troopUnitsToMove, gameInstance));
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
