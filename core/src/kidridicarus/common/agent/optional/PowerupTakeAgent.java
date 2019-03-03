package kidridicarus.common.agent.optional;

import kidridicarus.game.info.PowerupInfo.PowType;

// this agent can take powerups
public interface PowerupTakeAgent {
	public void applyPowerup(PowType pt);
}
