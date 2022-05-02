package ch.zhaw.ovtycoon.model;

import java.util.Arrays;
import java.util.Random;

import ch.zhaw.ovtycoon.Config;

public class Dice {
	private final Random random = new Random();   
	private int[] rolledDice;
	/**
	 * This method rolls Dice from one to maxDiceValue.
	 * 
	 * @param amount	how many times the dices needed to be rolled
	 * 					needs to be between 0 and maxThrowableDice Value
	 * @return	the rolled Dice Array sorted from min to max values
	 * 			if argument was an invalid the return value is null.	
	 * 	
	 */
	public int[] rollDice(int amount) {
		if(amount < 0 || amount > Config.MAX_THROWABLE_DICE) {
			return null;
		}
		
		rolledDice = new int[amount];	
		for (int i = 0; i < amount; i++) {
			// +1 because bound is not included in nextInt.
			rolledDice[i] = (random.nextInt(Config.MAX_DICE_VALUE) + 1);
		}
		Arrays.sort(rolledDice);
		return rolledDice;			
	}
	
	public int[] getRolledDice() {
		return rolledDice;
	}
	
	

}
