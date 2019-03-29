package kidridicarus.game.powerup;

import kidridicarus.common.powerup.PowChar;
import kidridicarus.common.powerup.Powerup;

public class KidIcarusPow extends Powerup {
	public static class Heart1Pow extends KidIcarusPow {}

	public PowChar getPowerupCharacter() {
		return PowChar.PIT;
	}
}
