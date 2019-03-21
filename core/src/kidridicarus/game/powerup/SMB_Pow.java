package kidridicarus.game.powerup;

import kidridicarus.common.powerup.PowChar;
import kidridicarus.common.powerup.Powerup;

public class SMB_Pow extends Powerup{
	@Override
	public PowChar getPowerupCharacter() {
		return PowChar.MARIO;
	}

	public static class MushroomPow extends SMB_Pow {}
	public static class FireFlowerPow extends SMB_Pow {}
	public static class Mush1UpPow extends SMB_Pow {}
	public static class PowerStarPow extends SMB_Pow {}
	public static class CoinPow extends SMB_Pow {}
	public static class PointsPow extends SMB_Pow {
			private int amount;
			public PointsPow(int amount) { this.amount = amount; }
			public int getAmount() { return amount; }
		}
}
