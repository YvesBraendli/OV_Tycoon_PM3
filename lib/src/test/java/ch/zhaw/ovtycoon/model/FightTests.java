package ch.zhaw.ovtycoon.model;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ch.zhaw.ovtycoon.Config.ZoneName;

public class FightTests {
	private Fight testee;
	private Zone attackingZone;
	private Zone defendingZone;
	
	@Before
	public void init() {
		attackingZone = new Zone(ZoneName.Zone110);
		attackingZone.setTroops(2);
		defendingZone = new Zone(ZoneName.Zone110);
		defendingZone.setTroops(1);
		testee = new Fight(attackingZone, defendingZone);
	}
	
	@Test
	public void constructor_attackingZoneArgumentNull_throwException() {
		assertThrows(IllegalArgumentException.class, () -> new Fight(null, defendingZone));	
	}
	
	@Test
	public void constructor_defendingZoneArgumentNull_throwException() {	
		assertThrows(IllegalArgumentException.class, () -> new Fight(attackingZone, null));	
	}
	
	@Test
	public void fight_NegativeAttackerTroop_throwException() {
		int invalidArgument = -1;
		assertThrows(IllegalArgumentException.class, () -> testee.fight(invalidArgument, 1));
	}
	
	@Test
	public void fight_ArgumentBigerThanTroopsInAttackingZone_throwException() {
		int invalidArgument = attackingZone.getTroops() + 1;
		assertThrows(IllegalArgumentException.class, () -> testee.fight(invalidArgument, 1));
	}
	
	@Test
	public void fight_invalidDefenderTroop_throwException() {
		int invalidArgument = -1;
		assertThrows(IllegalArgumentException.class, () -> testee.fight(1, invalidArgument));
	}
	
	@Test
	public void fight_ArgumentBigerThanTroopsInDefendingZone_throwException() {
		int invalidArgument = defendingZone.getTroops() + 1;
		assertThrows(IllegalArgumentException.class, () -> testee.fight(1, invalidArgument));
	}
	
	@Test
	public void fight_executeFight_correctTroopsLost() {
		// Arrange
		int attackingTroops = 2;
		int defendingTroops = 1;
		int expectedLostTroops = defendingTroops;
		
		int totalTroopsAtStart = attackingZone.getTroops() + defendingZone.getTroops();
		
		// Act
		testee.fight(attackingTroops, defendingTroops);
		int totalTroopsAtEnding = attackingZone.getTroops() + defendingZone.getTroops();
		int actualLostTroops = totalTroopsAtStart - totalTroopsAtEnding;
		
		// Arrange
		assertTrue(expectedLostTroops == actualLostTroops);
	}
}
