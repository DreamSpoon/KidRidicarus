package kidridicarus.common.tool;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.game.info.PowerupInfo.PowType;

public class Powerups {
	public static boolean tryPushPowerup(Agent agent, PowType powType) {
		if(agent instanceof PowerupTakeAgent)
			return ((PowerupTakeAgent)agent).onTakePowerup(powType);
		return false;
	}
}
