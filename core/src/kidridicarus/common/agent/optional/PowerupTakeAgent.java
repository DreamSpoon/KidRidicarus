package kidridicarus.common.agent.optional;

import kidridicarus.common.powerup.Powerup;

public interface PowerupTakeAgent {
	public abstract boolean onTakePowerup(Powerup pu);
}
