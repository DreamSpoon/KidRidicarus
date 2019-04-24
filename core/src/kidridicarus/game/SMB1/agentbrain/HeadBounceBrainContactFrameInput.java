package kidridicarus.game.SMB1.agentbrain;

import java.util.List;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agentbrain.BrainContactFrameInput;

public class HeadBounceBrainContactFrameInput extends BrainContactFrameInput {
	public List<Agent> headBounceBeginContacts;
	public HeadBounceBrainContactFrameInput(List<Agent> headBounceBeginContacts) {
		this.headBounceBeginContacts = headBounceBeginContacts;
	}
}
