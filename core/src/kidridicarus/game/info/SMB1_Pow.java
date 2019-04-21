package kidridicarus.game.info;

import kidridicarus.common.powerup.PowChar;
import kidridicarus.common.powerup.Powerup;

public class SMB1_Pow extends Powerup{
	@Override
	public PowChar getPowerupCharacter() {
		return PowChar.MARIO;
	}

	public static class MushroomPow extends SMB1_Pow {}
	public static class FireFlowerPow extends SMB1_Pow {}
	public static class Mush1UpPow extends SMB1_Pow {}
	public static class PowerStarPow extends SMB1_Pow {}
	public static class CoinPow extends SMB1_Pow {}
	public static class PointsPow extends SMB1_Pow {
			private int amount;
			public PointsPow(int amount) { this.amount = amount; }
			public int getAmount() { return amount; }
		}
}
