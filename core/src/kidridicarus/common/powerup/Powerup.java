package kidridicarus.common.powerup;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.optional.PowerupTakeAgent;

public abstract class Powerup {
	public abstract PowChar getPowerupCharacter();

	public static boolean tryPushPowerup(Agent agent, Powerup pu) {
		if(agent instanceof PowerupTakeAgent)
			return ((PowerupTakeAgent)agent).onTakePowerup(pu);
		return false;
	}
}
