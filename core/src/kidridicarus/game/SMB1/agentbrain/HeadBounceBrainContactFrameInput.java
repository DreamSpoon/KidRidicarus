package kidridicarus.game.SMB1.agentbrain;

import java.util.List;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentbrain.BrainContactFrameInput;

public class HeadBounceBrainContactFrameInput extends BrainContactFrameInput {
	public List<Agent> headBounceBeginContacts;

	public HeadBounceBrainContactFrameInput(RoomBox room, boolean isKeepAlive, boolean isDespawn,
			List<Agent> headBounceBeginContacts) {
		super(room, isKeepAlive, isDespawn);
		this.headBounceBeginContacts = headBounceBeginContacts;
	}
}
