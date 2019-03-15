package kidridicarus.game.info;

public class PowerupInfo {
	// "powerup character" - shortened for brevity
	public enum PowChar { NONE, MARIO, SAMUS }

	// "powerup type"
	public enum PowType {
		// each powerup type has an associated character
		NONE(PowChar.NONE), MUSHROOM(PowChar.MARIO), FIREFLOWER(PowChar.MARIO), POWERSTAR(PowChar.MARIO),
		MUSH1UP(PowChar.MARIO), MARUMARI(PowChar.SAMUS), COIN(PowChar.MARIO);

		private PowChar pc;
		PowType(PowChar pc) { this.pc = pc; }
		// return true if the power character of this powerup type matches the other power character
		public boolean isPowChar(PowChar other) { return pc.equals(other); }
	}
}
