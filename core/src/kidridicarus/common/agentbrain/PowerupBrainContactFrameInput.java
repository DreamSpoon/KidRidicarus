package kidridicarus.common.agentbrain;

import kidridicarus.common.agent.optional.PowerupTakeAgent;

public class PowerupBrainContactFrameInput extends BrainContactFrameInput {
	public PowerupTakeAgent powerupTaker;

	public PowerupBrainContactFrameInput(PowerupTakeAgent powerupTaker) {
		this.powerupTaker = powerupTaker;
	}
}
