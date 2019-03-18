package kidridicarus.common.agent.optional;

import kidridicarus.game.info.PowerupInfo.PowType;

public interface PowerupTakeAgent {
	public abstract boolean onTakePowerup(PowType powType);
}
