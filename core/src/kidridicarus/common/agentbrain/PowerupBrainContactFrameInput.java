package kidridicarus.common.agentbrain;

import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agent.roombox.RoomBox;

public class PowerupBrainContactFrameInput extends BrainContactFrameInput {
	public PowerupTakeAgent powerupTaker;

	public PowerupBrainContactFrameInput(RoomBox room, boolean isKeepAlive, boolean isDespawn,
			PowerupTakeAgent powerupTaker) {
		super(room, isKeepAlive, isDespawn);
		this.powerupTaker = powerupTaker;
	}
}
