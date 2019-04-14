package kidridicarus.game.powerup;

import kidridicarus.common.powerup.PowChar;
import kidridicarus.common.powerup.Powerup;

public class KidIcarusPow extends Powerup {
	private static final int CHALICE_HEAL_AMT = 6;

	public static class AngelHeartPow extends KidIcarusPow {
		private int numHearts;
		public AngelHeartPow(int numHearts) { this.numHearts = numHearts; }
		public int getNumHearts() { return numHearts; }
	}

	public static class ChaliceHealthPow extends KidIcarusPow {
		public int getHealAmount() { return CHALICE_HEAL_AMT; }
	}

	public PowChar getPowerupCharacter() {
		return PowChar.PIT;
	}
}
