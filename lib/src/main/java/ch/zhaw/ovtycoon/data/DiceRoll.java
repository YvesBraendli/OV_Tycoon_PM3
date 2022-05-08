package ch.zhaw.ovtycoon.data;

/**
 * DTO Class to transfer the information of the last executed dice roll of a fight
 * from the ÖVTycoon Backend to a corresponding Fronend
 * 
 *
 */
public class DiceRoll {
	private int[] attackerRoll;
	private int[] defenderRoll;
	private int numberOfAttackDie;
	private int numberOfDefenceDie;
	
	/**
	 * Constructor of the dice roll
	 * 
	 * @param attackerRoll
	 * @param defenderRoll
	 * @param numberOfAttackDie
	 * @param numberOfDefenceDie
	 */
	public DiceRoll(int[] attackerRoll, int[] defenderRoll, int numberOfAttackDie, int numberOfDefenceDie) {
		this.attackerRoll = attackerRoll;
		this.defenderRoll = defenderRoll;
		this.numberOfAttackDie = numberOfAttackDie;
		this.numberOfDefenceDie = numberOfDefenceDie;
	}
	
	public int[] getAttackerRoll() {
		return attackerRoll;
	}
	public int[] getDefenderRoll() {
		return defenderRoll;
	}
	public int getNumberOfAttackDie() {
		return numberOfAttackDie;
	}
	public int getNumberOfDefenceDie() {
		return numberOfDefenceDie;
	}
}
