package ch.zhaw.ovtycoon.model;

import ch.zhaw.ovtycoon.Config;

public class Fight {
	private Zone attackingZone;
	private Zone defendingZone;

	/**
	 * Instantiates a new Fight object.
	 * @param attackingZone		attacking Zone, can't be null
	 * @param defendingZone		defending Zone, can't be null
	 * 
	 * @throws IllegalArgumentException	if one of the zones is null
	 */
	public Fight(Zone attackingZone, Zone defendingZone) {
		if(attackingZone == null || defendingZone == null) {
			throw new IllegalArgumentException();
		}
		this.attackingZone = attackingZone;
		this.defendingZone = defendingZone;
	}
	
	/**
	 * This fight methods executes the fight between the attacking zone and the
	 * defending zone. The smaller amount of troops (arguments) is the number of
	 * thrown dice per player/zone. The highest thrown number from the attacker is
	 * compared to the highest thrown number of the defender. The attacker wins, if
	 * his number is bigger than the defender. Then the defender loses one troop. If
	 * the number from the attacker isn't higher, the attacker loses one troop. This
	 * goes one as many times as the dice has been thrown.
	 * 
	 * @param attackingTroops Number of troops that the attacker sends to attack.
	 *                        Must be positive and max amount is equals to
	 *                        Config.MAX_THROWABLE_DICE and not bigger than troops
	 *                        in attacking zone.
	 * @param defendingTroops Number of troops that the defender sends to defend.
	 *                        Must be positive and max amount is equals to
	 *                        Config.MAX_THROWABLE_DICE and not bigger than troops
	 *                        in defending zone.
	 * @throws IllegalArgumentException if arguments are invalid.
	 */
	public void fight(int attackingTroops, int defendingTroops) {
		if (!isValidArgument(attackingTroops, true) || !isValidArgument(defendingTroops, false)) {
			throw new IllegalArgumentException();
		}

		Dice dice = new Dice();
		int[] attackingDice = dice.rollDice(attackingTroops);
		int[] defendingDice = dice.rollDice(defendingTroops);

		int lastAttackIndex = attackingTroops - 1;
		int lastDeffendingIndex = defendingTroops - 1;

		int comparingDice = defendingTroops < attackingTroops ? defendingTroops : attackingTroops;

		for (int i = 0; i < comparingDice; i++) {
			if (attackingDice[lastAttackIndex - i] > defendingDice[lastDeffendingIndex - i]) {
				defendingZone.decreaseZone(1);
			} else {
				attackingZone.decreaseZone(1);
			}
		}

	}

	private boolean isValidArgument(int attackingTroops, boolean isAttacker) {
		if (attackingTroops < 0 || attackingTroops > Config.MAX_THROWABLE_DICE) {
			return false;
		}
		int maxPossibleTroops = isAttacker ? attackingZone.getTroops() : defendingZone.getTroops();
		if (maxPossibleTroops < attackingTroops) {
			return false;
		}
		return true;
	}

}
