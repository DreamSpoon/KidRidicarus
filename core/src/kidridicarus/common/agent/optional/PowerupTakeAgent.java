package kidridicarus.common.agent.optional;

import kidridicarus.game.info.PowerupInfo.PowType;

public interface PowerupTakeAgent {
	public boolean onTakePowerup(PowType powType);
}
