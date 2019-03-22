package kidridicarus.game.powerup;

import kidridicarus.common.powerup.PowChar;
import kidridicarus.common.powerup.Powerup;

public class MetroidPow extends Powerup {
	public static class MaruMariPow extends MetroidPow {}

	public PowChar getPowerupCharacter() {
		return PowChar.SAMUS;
	}
}
