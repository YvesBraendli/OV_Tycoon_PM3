package ch.zhaw.ovtycoon.model;

import ch.zhaw.ovtycoon.Config.ZoneName;

public class Fight {
	private Zone attackingZone;
	private Zone defendingZone;

	public Fight(Zone attackingZone, Zone defendingZone) {
		this.attackingZone = attackingZone;
		this.defendingZone = defendingZone;
	}

	public static void main(String[] args) {
		Zone attacking = new Zone(ZoneName.Zone110);
		attacking.setTroops(12);
		Zone deffending = new Zone(ZoneName.Zone111);
		deffending.setTroops(10);

		Fight f = new Fight(attacking, deffending);
		f.fight(3, 2);
	}

	/**
	 * This fight methods executes the fight between the attacking zone and the
	 * defending zone. The smaller amount of troops (arguments) is the number of
	 * thrown dice per player/zone. The highest thrown number from the attacker is
	 * compared to the highest thrown number of the defender. The attacker wins, if
	 * his number is bigger than the defender. Then the defender loses one troop.
	 * If the number from the attacker isn't higher, the attacker loses one troop.
	 * This goes one as many times as the dice has been thrown.
	 * 
	 * @param attackingTroops  Number of troops that the attacker sends to attack. Must be positive and 
	 * @param defendingTroops Number of troops that the defender sends to defend.
	 */
	public void fight(int attackingTroops, int defendingTroops) {
		System.out.println("*******************************************");

		System.out.println("start configuration: ");
		System.out.println("attackingTroops : " + attackingTroops);
		System.out.println("defendingTroops : " + defendingTroops);
		System.out.println("troops in attacking zone: " + attackingZone.getTroops());
		System.out.println("troops in defending zone: " + defendingZone.getTroops());
		System.out.println("#########################################");

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
			System.out.println("attacking dice : " + attackingDice[lastAttackIndex - i]);
			System.out.println("defendig dice : " + defendingDice[lastDeffendingIndex - i]);
			System.out.println("attaciing zone :" + attackingZone.getTroops());
			System.out.println("defending zone :" + defendingZone.getTroops());
			System.out.println("*******************************************");
		}

	}

}
