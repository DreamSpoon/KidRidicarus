package kidridicarus.game.powerup;

import kidridicarus.common.powerup.PowChar;
import kidridicarus.common.powerup.Powerup;

public class KidIcarusPow extends Powerup {
	public static class HeartsPow extends KidIcarusPow {
		private int numHearts;
		public HeartsPow(int numHearts) { this.numHearts = numHearts; }
		public int getNumHearts() { return numHearts; }
	}

	public PowChar getPowerupCharacter() {
		return PowChar.PIT;
	}
}
