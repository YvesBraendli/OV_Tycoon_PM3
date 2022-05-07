package ch.zhaw.ovtycoon.model;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ch.zhaw.ovtycoon.Config.RegionName;
import ch.zhaw.ovtycoon.Config.ZoneName;

import java.util.ArrayList;


public class GameTest {
	private Game testee;
	private Player a;
	private Player b;
	
	@Before
	public void init() {
		testee = new Game();
		testee.initGame(2);
		a = new Player("a");
		b = new Player("b");
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
		assertTrue(owner == null);
		
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
		assertTrue(a == owner);
		
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
		assertTrue(winner == null);
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
		assertTrue(winner == a);
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
	
}
