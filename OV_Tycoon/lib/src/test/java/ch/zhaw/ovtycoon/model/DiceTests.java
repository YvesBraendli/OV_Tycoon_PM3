package ch.zhaw.ovtycoon.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class DiceTests {
	private Dice testee = new Dice();
	
	@Test
	public void rollDice_returnNull_negativAmount() {
		// Arrange
		Object expectedResult = null;
		int negativeArgument = -1;
		
		// Act
		Object actualResult = testee.rollDice(negativeArgument);
		
		// Assert
		assertEquals(expectedResult, actualResult);
	}
	
	@Test
	public void rollDice_returnNull_amountToBig() {
		// Arrange
		Object expectedResult = null;
		int bigArgument = 201;
		
		// Act
		Object actualResult = testee.rollDice(bigArgument);
		
		// Assert
		assertEquals(expectedResult, actualResult);
	}
	
	@Test
	public void rollDice_returnAmountOfDiceIsEqualToArgumentAmount_validArgument() {
		// Arrange
		int argument = 10;
		int expectedResultLength = argument;
		
		// Act
		int actualResult = testee.rollDice(argument).length;
		
		// Assert
		assertTrue(expectedResultLength == actualResult);
	}

	@Test
	public void rollDice_sortedValues_validArgument() {
		// Arrange
		int argument = 10;
		
		// Act
		int[] actualResult = testee.rollDice(argument);
		
		// Assert
		for (int i = 0; i < (actualResult.length - 1); i++) {
			if(actualResult[i] > actualResult[i+1]) {
				fail("not Sorted: Elements with index " + i + " and " + (i+1));
			}
		}
		
	}
}
