package ch.zhaw.ovtycoon.model;

import ch.zhaw.ovtycoon.Config;
import org.junit.Before;
import org.junit.Test;

import ch.zhaw.ovtycoon.Config.PlayerColor;
import ch.zhaw.ovtycoon.Config.RegionName;

import java.util.ArrayList;

import static org.junit.Assert.*;


public class GameTest {
	private Game testee;
	private Player a;
	private Player b;
	private Player c;
	private Player[] players;
	
	@Before
	public void init() {
		testee = new Game();
		testee.initGame(3);
		initPlayer();
	}
	
	private void initPlayer() {
		a = new Player("a");
		b = new Player("b");
		c = new Player("c");
		a.setColor(PlayerColor.BLACK);
		b.setColor(PlayerColor.BLUE);
		c.setColor(PlayerColor.RED);
		players = new Player[3];
		players[0] = a;
		players[1] = b;
		players[2] = c;
		testee.setPlayerList(players);
	}

	@Test
	public void getNeighboursList_multipleNeighbours(){
		//arrange
		testee.setZoneOwner(a, testee.getZone("Zone121"));
		testee.setZoneOwner(a, testee.getZone("Zone140"));
		testee.setZoneOwner(a, testee.getZone("Zone141"));
		testee.setZoneOwner(a, testee.getZone("Zone142"));
		testee.setZoneOwner(a, testee.getZone("Zone143"));
		testee.setZoneOwner(a, testee.getZone("Zone131"));
		testee.setZoneOwner(a, testee.getZone("Zone133"));
		testee.setZoneOwner(a, testee.getZone("Zone172"));

		testee.setZoneOwner(b, testee.getZone("Zone130"));
		testee.setZoneOwner(b, testee.getZone("Zone132"));

		//act
		ArrayList<Zone> testZone = testee.getPossibleMovementNeighbours(testee.getZone("Zone121"),a);
		boolean checkNeighboursSetRight = allNeighboursCorrect(createNeighboursManually(),testZone);

		//assert
		assertTrue(testZone.size()==7);
		assertTrue(checkNeighboursSetRight);
	}

	@Test
	public void getNeighboursList_noNeighbours(){
		//arrange
		testee.setZoneOwner(a, testee.getZone("Zone111"));

		testee.setZoneOwner(b, testee.getZone("Zone117"));
		testee.setZoneOwner(b, testee.getZone("Zone112"));
		testee.setZoneOwner(b, testee.getZone("Zone121"));
		testee.setZoneOwner(b, testee.getZone("Zone110"));
		testee.setZoneOwner(b, testee.getZone("Zone154"));

		//act
		ArrayList<Zone> testZone = testee.getPossibleMovementNeighbours(testee.getZone("Zone111"),a);

		//assert
		assertTrue(testZone.size()==0);
	}


	@Test
	public void getRegionOwner_noOwner() {
		//Arrange
		//name=MeilenZurich,zones=Zone143,Zone141,Zone130,Zone140,Zone180,Zone110,Zone142
		testee.setZoneOwner(a, testee.getZone("Zone143"));
		testee.setZoneOwner(a, testee.getZone("Zone141"));
		testee.setZoneOwner(a, testee.getZone("Zone130"));
		testee.setZoneOwner(a, testee.getZone("Zone142"));
		testee.setZoneOwner(a, testee.getZone("Zone140"));
		testee.setZoneOwner(a, testee.getZone("Zone180"));
		testee.setZoneOwner(b, testee.getZone("Zone110"));
		
		//Act
		Player owner = testee.getRegionOwner(RegionName.MeilenZurich);
		//Assert
		assertEquals(owner, null);
		
	}
	
	@Test
	public void getRegionOwner_aOwner() {
		//Arrange
		//name=MeilenZurich,zones=Zone143,Zone141,Zone130,Zone140,Zone180,Zone110,Zone142
		testee.setZoneOwner(a, testee.getZone("Zone143"));
		testee.setZoneOwner(a, testee.getZone("Zone141"));
		testee.setZoneOwner(a, testee.getZone("Zone130"));
		testee.setZoneOwner(a, testee.getZone("Zone140"));
		testee.setZoneOwner(a, testee.getZone("Zone180"));
		testee.setZoneOwner(a, testee.getZone("Zone142"));
		testee.setZoneOwner(a, testee.getZone("Zone110"));
		
		//Act
		Player owner = testee.getRegionOwner(RegionName.MeilenZurich);
		//Assert
		assertEquals(a, owner);
		
	}
	
	@Test
	public void getWinner_noWinner() {
		//Arrange
		//name=MeilenZurich,zones=Zone143,Zone141,Zone130,Zone140,Zone180,Zone110,Zone142
		testee.setZoneOwner(a, testee.getZone("Zone143"));
		testee.setZoneOwner(a, testee.getZone("Zone141"));
		testee.setZoneOwner(a, testee.getZone("Zone130"));
		testee.setZoneOwner(a, testee.getZone("Zone140"));
		testee.setZoneOwner(a, testee.getZone("Zone180"));
		testee.setZoneOwner(a, testee.getZone("Zone142"));
		testee.setZoneOwner(a, testee.getZone("Zone110"));
		//name=HorgenAlbis,zones=Zone155,Zone156,Zone151,Zone152,Zone153,Zone150,Zone181
		testee.setZoneOwner(a, testee.getZone("Zone155"));
		testee.setZoneOwner(a, testee.getZone("Zone156"));
		testee.setZoneOwner(a, testee.getZone("Zone151"));
		testee.setZoneOwner(a, testee.getZone("Zone152"));
		testee.setZoneOwner(a, testee.getZone("Zone153"));
		testee.setZoneOwner(a, testee.getZone("Zone150"));
		testee.setZoneOwner(a, testee.getZone("Zone181"));
		//name=Oberland,zones=Zone122,Zone135,Zone172,Zone173,Zone134,Zone133,Zone132,Zone131,Zone130
		testee.setZoneOwner(a, testee.getZone("Zone122"));
		testee.setZoneOwner(a, testee.getZone("Zone135"));
		testee.setZoneOwner(a, testee.getZone("Zone172"));
		testee.setZoneOwner(a, testee.getZone("Zone173"));
		testee.setZoneOwner(a, testee.getZone("Zone134"));
		testee.setZoneOwner(a, testee.getZone("Zone133"));
		testee.setZoneOwner(a, testee.getZone("Zone132"));
		testee.setZoneOwner(a, testee.getZone("Zone131"));
		testee.setZoneOwner(a, testee.getZone("Zone130"));
		//name=Weinland,zones=Zone115,Zone124,Zone123,Zone120,Zone170,Zone171,Zone164,Zone163,Zone160,Zone161,Zone162,Zone116
		testee.setZoneOwner(a, testee.getZone("Zone115"));
		testee.setZoneOwner(a, testee.getZone("Zone116"));
		testee.setZoneOwner(a, testee.getZone("Zone124"));
		testee.setZoneOwner(a, testee.getZone("Zone123"));
		testee.setZoneOwner(a, testee.getZone("Zone120"));
		testee.setZoneOwner(a, testee.getZone("Zone170"));
		testee.setZoneOwner(a, testee.getZone("Zone171"));
		testee.setZoneOwner(a, testee.getZone("Zone164"));
		testee.setZoneOwner(a, testee.getZone("Zone163"));
		testee.setZoneOwner(a, testee.getZone("Zone160"));
		testee.setZoneOwner(a, testee.getZone("Zone161"));
		testee.setZoneOwner(a, testee.getZone("Zone162"));
		//name=Unterland,zones=Zone114,Zone113,Zone112,Zone121,Zone111,Zone117,Zone154,Zone184,Zone118
		testee.setZoneOwner(a, testee.getZone("Zone114"));
		testee.setZoneOwner(a, testee.getZone("Zone113"));
		testee.setZoneOwner(a, testee.getZone("Zone112"));
		testee.setZoneOwner(a, testee.getZone("Zone121"));
		testee.setZoneOwner(a, testee.getZone("Zone111"));
		testee.setZoneOwner(a, testee.getZone("Zone117"));
		testee.setZoneOwner(a, testee.getZone("Zone154"));
		testee.setZoneOwner(a, testee.getZone("Zone118"));
		
		testee.setZoneOwner(b, testee.getZone("Zone184"));

		//Act
		Player winner = testee.getWinner();
		
		//Assert
		assertEquals(winner, null);
	}
	
	@Test
	public void getWinner_aWinner() {
		//Arrange
		//name=MeilenZurich,zones=Zone143,Zone141,Zone130,Zone140,Zone180,Zone110,Zone142
		testee.setZoneOwner(a, testee.getZone("Zone143"));
		testee.setZoneOwner(a, testee.getZone("Zone141"));
		testee.setZoneOwner(a, testee.getZone("Zone130"));
		testee.setZoneOwner(a, testee.getZone("Zone140"));
		testee.setZoneOwner(a, testee.getZone("Zone180"));
		testee.setZoneOwner(a, testee.getZone("Zone110"));
		testee.setZoneOwner(a, testee.getZone("Zone142"));
		//name=HorgenAlbis,zones=Zone155,Zone156,Zone151,Zone152,Zone153,Zone150,Zone181
		testee.setZoneOwner(a, testee.getZone("Zone155"));
		testee.setZoneOwner(a, testee.getZone("Zone156"));
		testee.setZoneOwner(a, testee.getZone("Zone151"));
		testee.setZoneOwner(a, testee.getZone("Zone152"));
		testee.setZoneOwner(a, testee.getZone("Zone153"));
		testee.setZoneOwner(a, testee.getZone("Zone150"));
		testee.setZoneOwner(a, testee.getZone("Zone181"));
		//name=Oberland,zones=Zone122,Zone135,Zone172,Zone173,Zone134,Zone133,Zone132,Zone131,Zone130
		testee.setZoneOwner(a, testee.getZone("Zone122"));
		testee.setZoneOwner(a, testee.getZone("Zone135"));
		testee.setZoneOwner(a, testee.getZone("Zone172"));
		testee.setZoneOwner(a, testee.getZone("Zone173"));
		testee.setZoneOwner(a, testee.getZone("Zone134"));
		testee.setZoneOwner(a, testee.getZone("Zone133"));
		testee.setZoneOwner(a, testee.getZone("Zone132"));
		testee.setZoneOwner(a, testee.getZone("Zone131"));
		testee.setZoneOwner(a, testee.getZone("Zone130"));
		//name=Weinland,zones=Zone115,Zone124,Zone123,Zone120,Zone170,Zone171,Zone164,Zone163,Zone160,Zone161,Zone162,Zone116
		testee.setZoneOwner(a, testee.getZone("Zone115"));
		testee.setZoneOwner(a, testee.getZone("Zone116"));
		testee.setZoneOwner(a, testee.getZone("Zone124"));
		testee.setZoneOwner(a, testee.getZone("Zone123"));
		testee.setZoneOwner(a, testee.getZone("Zone120"));
		testee.setZoneOwner(a, testee.getZone("Zone170"));
		testee.setZoneOwner(a, testee.getZone("Zone171"));
		testee.setZoneOwner(a, testee.getZone("Zone164"));
		testee.setZoneOwner(a, testee.getZone("Zone163"));
		testee.setZoneOwner(a, testee.getZone("Zone160"));
		testee.setZoneOwner(a, testee.getZone("Zone161"));
		testee.setZoneOwner(a, testee.getZone("Zone162"));
		//name=Unterland,zones=Zone114,Zone113,Zone112,Zone121,Zone111,Zone117,Zone154,Zone184,Zone118
		testee.setZoneOwner(a, testee.getZone("Zone114"));
		testee.setZoneOwner(a, testee.getZone("Zone113"));
		testee.setZoneOwner(a, testee.getZone("Zone112"));
		testee.setZoneOwner(a, testee.getZone("Zone121"));
		testee.setZoneOwner(a, testee.getZone("Zone111"));
		testee.setZoneOwner(a, testee.getZone("Zone117"));
		testee.setZoneOwner(a, testee.getZone("Zone154"));
		testee.setZoneOwner(a, testee.getZone("Zone184"));
		testee.setZoneOwner(a, testee.getZone("Zone118"));

		//Act
		Player winner = testee.getWinner();
		
		//Assert
		assertEquals(winner, a);
	}

	@Test
	public void switchToNextPlayer_nextPlayerInQueueActive() {	
		//Act
		boolean retVal = testee.switchToNextPlayer();
		
		//Assert
		assertEquals(testee.getCurrentPlayer(), b);
		assertTrue(retVal);
		
	}
	
	@Test
	public void switchToNextPlayer_nextPlayerInQueueEliminated() {
		//Arrange
		testee.tryEliminatePlayer(b);
		
		//Act
		boolean retVal = testee.switchToNextPlayer();
		
		//Assert
		assertEquals(testee.getCurrentPlayer(), c);
		assertTrue(retVal);
	}
	
	@Test
	public void switchToNextPlayer_currentPlayerLastInArray() {
		//Arrange
		testee.switchToNextPlayer();
		testee.switchToNextPlayer();
		Player startPlayer = testee.getCurrentPlayer();

		
		//Act
		boolean retVal = testee.switchToNextPlayer();
		
		//Assert
		assertEquals(startPlayer, c);
		assertEquals(testee.getCurrentPlayer(), a);	
		assertTrue(retVal);
	}
	
	@Test
	public void switchToNextPlayer_currentPlayerLastInArray_FirstPlayerEliminated() {
		//Arrange
		testee.switchToNextPlayer();
		testee.switchToNextPlayer();
		testee.tryEliminatePlayer(a);
		Player startPlayer = testee.getCurrentPlayer();
		
		//Act
		boolean retVal = testee.switchToNextPlayer();
		
		//Assert
		assertEquals(startPlayer, c);
		assertEquals(testee.getCurrentPlayer(), b);	
		assertTrue(retVal);
	}
	
	@Test
	public void switchToNextPlayer_allPlayerEliminated() {
		//Arrange
		testee.tryEliminatePlayer(a);
		testee.tryEliminatePlayer(b);
		testee.tryEliminatePlayer(c);
		
		//Act
		boolean retVal = testee.switchToNextPlayer();
		
		//Assert
		assertEquals(testee.getCurrentPlayer(), a);	
		assertFalse(retVal);
		
	}
	
	@Test
	public void isZoneOwner() {
		//Arrange
		testee.setZoneOwner(a, testee.getZone("Zone110"));
		
		//Act
		Player retVal = testee.getZoneOwner(testee.getZone("Zone110"));
				
		//Assert
		assertEquals(a, retVal);
	}
	
	@Test
	public void getZonesOwnedByPlayer() {
		//Arrange
		testee.setZoneOwner(a, testee.getZone("Zone110"));
		testee.setZoneOwner(a, testee.getZone("Zone117"));
		testee.setZoneOwner(a, testee.getZone("Zone180"));
		testee.setZoneOwner(a, testee.getZone("Zone123"));
		testee.setZoneOwner(a, testee.getZone("Zone121"));
		
		ArrayList<Zone> expected = new ArrayList<Zone>();
		expected.add(testee.getZone("Zone110"));
		expected.add(testee.getZone("Zone117"));
		expected.add(testee.getZone("Zone180"));
		expected.add(testee.getZone("Zone123"));
		expected.add(testee.getZone("Zone121"));
		
		//Act
		ArrayList<Zone> actual = testee.getZonesOwnedbyPlayer(a);
		
		//Assert
		assertEquals(expected.size(), actual.size());
		assertTrue(actual.containsAll(expected));
	}
	
	@Test
	public void getZonesOwnedByPlayer_noZonesOwned() {
		//Act
		ArrayList<Zone> actual = testee.getZonesOwnedbyPlayer(a);
		
		//Assert
		assertEquals(0, actual.size());
	}
	
	@Test
	public void getAttackableZones() {
		//Arrange
		testee.setZoneOwner(a, testee.getZone("Zone114"));
		testee.setZoneOwner(a, testee.getZone("Zone113"));
		testee.setZoneOwner(a, testee.getZone("Zone121"));
		testee.setZoneOwner(a, testee.getZone("Zone111"));
		testee.setZoneOwner(a, testee.getZone("Zone117"));
		testee.setZoneOwner(a, testee.getZone("Zone154"));
		testee.setZoneOwner(a, testee.getZone("Zone118"));
		testee.setZoneOwner(a, testee.getZone("Zone155"));
		testee.setZoneOwner(a, testee.getZone("Zone156"));
		testee.setZoneOwner(a, testee.getZone("Zone151"));
		testee.setZoneOwner(a, testee.getZone("Zone152"));
		testee.setZoneOwner(a, testee.getZone("Zone153"));
		testee.setZoneOwner(a, testee.getZone("Zone150"));
		testee.setZoneOwner(a, testee.getZone("Zone181"));
		
		testee.setZoneOwner(b, testee.getZone("Zone112"));
		testee.getZone("Zone112").setTroops(10);
		
		ArrayList<Zone> expected = new ArrayList<Zone>();
		expected.add(testee.getZone("Zone118"));
		expected.add(testee.getZone("Zone117"));
		expected.add(testee.getZone("Zone111"));
		expected.add(testee.getZone("Zone121"));
		expected.add(testee.getZone("Zone123"));
		expected.add(testee.getZone("Zone113"));
		
		//Act
		ArrayList<Zone> actual = testee.getAttackableZones(testee.getZone("Zone112"));
				
		//Assert
		assertEquals(expected.size(), actual.size());
		assertTrue(actual.containsAll(expected));

	}
	
	@Test
	public void getAttackableZones_attackerHasNotEnoughTroups() {
		//Arrange
		testee.setZoneOwner(a, testee.getZone("Zone114"));
		testee.setZoneOwner(a, testee.getZone("Zone113"));
		testee.setZoneOwner(a, testee.getZone("Zone121"));
		testee.setZoneOwner(a, testee.getZone("Zone111"));
		testee.setZoneOwner(c, testee.getZone("Zone117"));
		testee.setZoneOwner(c, testee.getZone("Zone154"));
		testee.setZoneOwner(c, testee.getZone("Zone118"));
		testee.setZoneOwner(c, testee.getZone("Zone123"));
		
		testee.setZoneOwner(b, testee.getZone("Zone112"));
		testee.getZone("Zone112").setTroops(1);

		
		//Act
		ArrayList<Zone> actual = testee.getAttackableZones(testee.getZone("Zone112"));
				
		//Assert
		assertEquals(0, actual.size());
	}
	
	@Test
	public void getAttackableZones_noAttackableZonesAround() {
		//Arrange
		testee.setZoneOwner(a, testee.getZone("Zone114"));
		testee.setZoneOwner(a, testee.getZone("Zone113"));
		testee.setZoneOwner(a, testee.getZone("Zone121"));
		testee.setZoneOwner(a, testee.getZone("Zone111"));
		testee.setZoneOwner(a, testee.getZone("Zone117"));
		testee.setZoneOwner(a, testee.getZone("Zone154"));
		testee.setZoneOwner(a, testee.getZone("Zone118"));
		testee.setZoneOwner(a, testee.getZone("Zone123"));
		testee.setZoneOwner(a, testee.getZone("Zone112"));
		testee.getZone("Zone112").setTroops(10);

		//Act
		ArrayList<Zone> actual = testee.getAttackableZones(testee.getZone("Zone112"));
				
		//Assert
		assertEquals(0, actual.size());
	}
	
	@Test
	public void getPossibleAttackerZones_Cluster() {
		//Arrange
		testee.setZoneOwner(a, testee.getZone("Zone113"));
		testee.setZoneOwner(a, testee.getZone("Zone121"));
		testee.setZoneOwner(a, testee.getZone("Zone111"));
		testee.setZoneOwner(a, testee.getZone("Zone117"));
		testee.setZoneOwner(a, testee.getZone("Zone154"));
		testee.setZoneOwner(a, testee.getZone("Zone118"));
		testee.setZoneOwner(a, testee.getZone("Zone123"));
		testee.setZoneOwner(a, testee.getZone("Zone112"));
		
		testee.getZone("Zone114").setTroops(10);
		testee.getZone("Zone113").setTroops(10);
		testee.getZone("Zone121").setTroops(10);
		testee.getZone("Zone111").setTroops(10);
		testee.getZone("Zone117").setTroops(10);
		testee.getZone("Zone154").setTroops(10);
		testee.getZone("Zone118").setTroops(10);
		testee.getZone("Zone123").setTroops(10);
		testee.getZone("Zone112").setTroops(10);
		
		ArrayList<Zone> expected = new ArrayList<Zone>();
		expected.add(testee.getZone("Zone123"));
		expected.add(testee.getZone("Zone121"));
		expected.add(testee.getZone("Zone111"));
		expected.add(testee.getZone("Zone154"));
		expected.add(testee.getZone("Zone113"));
		
		//Act
		ArrayList<Zone> actual = testee.getPossibleAttackerZones(a);
		
		//Assert
		assertEquals(expected.size(), actual.size());
		assertTrue(actual.containsAll(expected));
		
	}
	
	@Test
	public void getPossibleAttackerZones_SingleSpread() {
		//Arrange
		testee.setZoneOwner(a, testee.getZone("Zone113"));
		testee.setZoneOwner(a, testee.getZone("Zone135"));
		testee.setZoneOwner(a, testee.getZone("Zone170"));
		
		testee.getZone("Zone113").setTroops(10);
		testee.getZone("Zone135").setTroops(10);
		testee.getZone("Zone170").setTroops(1);

		ArrayList<Zone> expected = new ArrayList<Zone>();
		expected.add(testee.getZone("Zone113"));
		expected.add(testee.getZone("Zone135"));

		//Act
		ArrayList<Zone> actual = testee.getPossibleAttackerZones(a);
		
		//Assert
		assertEquals(expected.size(), actual.size());
		assertTrue(actual.containsAll(expected));
		
	}
	
	@Test
	public void getZonesWithMovableTroops_singleZones() {
		//Arrange
		testee.setZoneOwner(a, testee.getZone("Zone113"));
		testee.setZoneOwner(a, testee.getZone("Zone135"));
		testee.setZoneOwner(a, testee.getZone("Zone173"));
		
		testee.getZone("Zone113").setTroops(10);
		testee.getZone("Zone135").setTroops(10);
		testee.getZone("Zone170").setTroops(10);
		
		//Act
		ArrayList<Zone> actual = testee.getZonesWithMovableTroops(a);
		
		//Assert
		assertEquals(0, actual.size());

	}
	
	@Test
	public void getZonesWithMovableTroops() {
		//Arrange
		testee.setZoneOwner(a, testee.getZone("Zone113"));
		testee.setZoneOwner(a, testee.getZone("Zone135"));
		testee.setZoneOwner(a, testee.getZone("Zone170"));
		testee.setZoneOwner(a, testee.getZone("Zone171"));
		
		testee.getZone("Zone113").setTroops(10);
		testee.getZone("Zone135").setTroops(10);
		testee.getZone("Zone170").setTroops(10);
		testee.getZone("Zone171").setTroops(10);
		
		ArrayList<Zone> expected = new ArrayList<Zone>();
		expected.add(testee.getZone("Zone171"));
		expected.add(testee.getZone("Zone170"));
		expected.add(testee.getZone("Zone135"));
		
		//Act
		ArrayList<Zone> actual = testee.getZonesWithMovableTroops(a);

		
		//Assert
		assertEquals(expected.size(), actual.size());
		assertTrue(actual.containsAll(expected));

	}
	
	@Test
	public void getZonesWithMovableTroops_notEnoughTroopsToMove() {
		//Arrange
		testee.setZoneOwner(a, testee.getZone("Zone113"));
		testee.setZoneOwner(a, testee.getZone("Zone135"));
		testee.setZoneOwner(a, testee.getZone("Zone170"));
		testee.setZoneOwner(a, testee.getZone("Zone171"));
		
		testee.getZone("Zone113").setTroops(10);
		testee.getZone("Zone135").setTroops(10);
		testee.getZone("Zone170").setTroops(10);
		testee.getZone("Zone171").setTroops(1);
		
		ArrayList<Zone> expected = new ArrayList<Zone>();
		expected.add(testee.getZone("Zone170"));
		expected.add(testee.getZone("Zone135"));
		
		//Act
		ArrayList<Zone> actual = testee.getZonesWithMovableTroops(a);

		
		//Assert
		assertEquals(expected.size(), actual.size());
		assertTrue(actual.containsAll(expected));

	}
	
	@Test
	public void tryEliminatePlayer_ownsNoZones() {
		//Act
		testee.tryEliminatePlayer(a);
		
		//Assert
		assertTrue(a.isEliminated());
		
	}
	
	@Test
	public void tryEliminatePlayer_stillOwnsZones() {
		//Arrange
		testee.setZoneOwner(a, testee.getZone("Zone113"));
		
		//Act
		testee.tryEliminatePlayer(a);
		
		//Assert
		assertFalse(a.isEliminated());
		
	}
	
	@Test
	public void getZone_ExistingZone() {
		//Arrange
		String searchZoneName = "Zone110";
		
		//Act
		Zone returnVal = testee.getZone(searchZoneName);
		
		//Assert
		assertTrue(returnVal != null && returnVal.getName().equals(searchZoneName));	
	}
	
	@Test
	public void getZone_NoneExistingZone() {
		//Arrange
		String searchZoneName = "Zone310";
		
		//Act
		Zone returnVal = testee.getZone(searchZoneName);
		
		//Assert
		assertNull(returnVal);
	}		
	
	
	@Test
	public void getPlayer_existingPlayer() {
		//Act
		Player returnVal = testee.getPlayer(PlayerColor.BLACK);
		
		//Assert
		assertEquals(returnVal, a);
	}
	
	@Test
	public void getPlayer_NoneExistingPlayer() {
		//Act
		Player returnVal = testee.getPlayer(PlayerColor.GREEN);
		
		//Assert
		assertNull(returnVal);
	}
	
	@Test
	public void getRegionOfZone() {
		//Arrange
		RegionName expected = RegionName.Unterland;
		Zone testZone = testee.getZone("Zone117");
		
		//Act
		RegionName actual = testee.getRegionOfZone(testZone);
		
		//Assert
		assertEquals(expected, actual);	
	}

	private ArrayList<Zone> createNeighboursManually(){
		ArrayList<Zone> neighbours = new ArrayList<>();
		neighbours.add(testee.getZone("Zone140"));
		neighbours.add(testee.getZone("Zone141"));
		neighbours.add(testee.getZone("Zone142"));
		neighbours.add(testee.getZone("Zone143"));
		neighbours.add(testee.getZone("Zone131"));
		neighbours.add(testee.getZone("Zone133"));
		neighbours.add(testee.getZone("Zone172"));
		return neighbours;
	}

	private boolean allNeighboursCorrect(ArrayList<Zone> neighboursManually,ArrayList<Zone> neighboursAutomatic){
		boolean hasNoMatch = false;
		for (int i=0; i<neighboursManually.size();i++){
			boolean flag = false;
			for (int j=0; j<neighboursAutomatic.size();j++){
				if(neighboursManually.get(i)==neighboursAutomatic.get(j)){
					flag = true;
				}
			}
			if(flag){
				hasNoMatch=true;
			}
		}
		return hasNoMatch;
	}

	@Test
	public void calcReinforcementsZonesMultipleOf3NoRegion(){
		testee.setZoneOwner(a, testee.getZone("Zone114"));
		testee.setZoneOwner(a, testee.getZone("Zone115"));
		testee.setZoneOwner(a, testee.getZone("Zone122"));
		testee.setZoneOwner(a, testee.getZone("Zone142"));
		testee.setZoneOwner(a, testee.getZone("Zone155"));
		testee.setZoneOwner(a, testee.getZone("Zone113"));
		testee.setZoneOwner(a, testee.getZone("Zone112"));
		testee.setZoneOwner(a, testee.getZone("Zone121"));
		testee.setZoneOwner(a, testee.getZone("Zone111"));
		testee.setZoneOwner(a, testee.getZone("Zone117"));
		testee.setZoneOwner(a, testee.getZone("Zone154"));
		testee.setZoneOwner(a, testee.getZone("Zone124"));
		testee.setZoneOwner(a, testee.getZone("Zone123"));
		testee.setZoneOwner(a, testee.getZone("Zone120"));
		testee.setZoneOwner(a, testee.getZone("Zone170"));

		int expected = 5; // Magic number because formula already used in tested method
		int actual = testee.getAmountOfReinforcements();

		assertEquals(expected, actual);
	}

	@Test
	public void calcReinforcementsZonesNotMultipleOf3NoRegion(){
		testee.setZoneOwner(a, testee.getZone("Zone114"));
		testee.setZoneOwner(a, testee.getZone("Zone115"));
		testee.setZoneOwner(a, testee.getZone("Zone122"));
		testee.setZoneOwner(a, testee.getZone("Zone142"));
		testee.setZoneOwner(a, testee.getZone("Zone155"));
		testee.setZoneOwner(a, testee.getZone("Zone113"));
		testee.setZoneOwner(a, testee.getZone("Zone112"));
		testee.setZoneOwner(a, testee.getZone("Zone121"));
		testee.setZoneOwner(a, testee.getZone("Zone111"));
		testee.setZoneOwner(a, testee.getZone("Zone117"));
		testee.setZoneOwner(a, testee.getZone("Zone154"));
		testee.setZoneOwner(a, testee.getZone("Zone124"));
		testee.setZoneOwner(a, testee.getZone("Zone123"));
		testee.setZoneOwner(a, testee.getZone("Zone120"));

		int expected = 4; // Magic number because formula already used in tested method
		int actual = testee.getAmountOfReinforcements();

		assertEquals(expected, actual);
	}

	@Test
	public void calcReinforcementsZonesLessThan9(){
		testee.setZoneOwner(a, testee.getZone("Zone114"));
		testee.setZoneOwner(a, testee.getZone("Zone115"));

		int expected = Config.MIN_NUMBER_OF_REINFORCEMENTS;
		int actual = testee.getAmountOfReinforcements();

		assertEquals(expected, actual);
	}

	@Test
	public void calcReinforcements1Region(){
		testee.setZoneOwner(a, testee.getZone("Zone114"));
		testee.setZoneOwner(a, testee.getZone("Zone113"));
		testee.setZoneOwner(a, testee.getZone("Zone112"));
		testee.setZoneOwner(a, testee.getZone("Zone121"));
		testee.setZoneOwner(a, testee.getZone("Zone111"));
		testee.setZoneOwner(a, testee.getZone("Zone117"));
		testee.setZoneOwner(a, testee.getZone("Zone154"));
		testee.setZoneOwner(a, testee.getZone("Zone184"));
		testee.setZoneOwner(a, testee.getZone("Zone118"));

		int expected = 5; // Magic number because formula already used in tested method
		int actual = testee.getAmountOfReinforcements();

		assertEquals(expected, actual);
	}

	@Test
	public void calcReinforcements1RegionAndRandomZones(){
		testee.setZoneOwner(a, testee.getZone("Zone114"));
		testee.setZoneOwner(a, testee.getZone("Zone113"));
		testee.setZoneOwner(a, testee.getZone("Zone112"));
		testee.setZoneOwner(a, testee.getZone("Zone121"));
		testee.setZoneOwner(a, testee.getZone("Zone111"));
		testee.setZoneOwner(a, testee.getZone("Zone117"));
		testee.setZoneOwner(a, testee.getZone("Zone154"));
		testee.setZoneOwner(a, testee.getZone("Zone184"));
		testee.setZoneOwner(a, testee.getZone("Zone118"));
		testee.setZoneOwner(a, testee.getZone("Zone115"));
		testee.setZoneOwner(a, testee.getZone("Zone124"));
		testee.setZoneOwner(a, testee.getZone("Zone123"));
		testee.setZoneOwner(a, testee.getZone("Zone120"));
		testee.setZoneOwner(a, testee.getZone("Zone170"));

		int expected = 6; // Magic number because formula already used in tested method
		int actual = testee.getAmountOfReinforcements();

		assertEquals(expected, actual);
	}

	@Test
	public void calcReinforcementsMultipleRegionsAndRandomZones(){
		testee.setZoneOwner(a, testee.getZone("Zone114"));
		testee.setZoneOwner(a, testee.getZone("Zone113"));
		testee.setZoneOwner(a, testee.getZone("Zone112"));
		testee.setZoneOwner(a, testee.getZone("Zone121"));
		testee.setZoneOwner(a, testee.getZone("Zone111"));
		testee.setZoneOwner(a, testee.getZone("Zone117"));
		testee.setZoneOwner(a, testee.getZone("Zone154"));
		testee.setZoneOwner(a, testee.getZone("Zone184"));
		testee.setZoneOwner(a, testee.getZone("Zone118"));
		testee.setZoneOwner(a, testee.getZone("Zone115"));
		testee.setZoneOwner(a, testee.getZone("Zone124"));
		testee.setZoneOwner(a, testee.getZone("Zone123"));
		testee.setZoneOwner(a, testee.getZone("Zone120"));
		testee.setZoneOwner(a, testee.getZone("Zone170"));
		testee.setZoneOwner(a, testee.getZone("Zone171"));
		testee.setZoneOwner(a, testee.getZone("Zone164"));
		testee.setZoneOwner(a, testee.getZone("Zone163"));
		testee.setZoneOwner(a, testee.getZone("Zone160"));
		testee.setZoneOwner(a, testee.getZone("Zone161"));
		testee.setZoneOwner(a, testee.getZone("Zone162"));
		testee.setZoneOwner(a, testee.getZone("Zone116"));
		testee.setZoneOwner(a, testee.getZone("Zone170"));
		testee.setZoneOwner(a, testee.getZone("Zone132"));
		testee.setZoneOwner(a, testee.getZone("Zone131"));
		testee.setZoneOwner(a, testee.getZone("Zone130"));

		int expected = 12; // Magic number because formula already used in tested method
		int actual = testee.getAmountOfReinforcements();

		assertEquals(expected, actual);
	}
}
