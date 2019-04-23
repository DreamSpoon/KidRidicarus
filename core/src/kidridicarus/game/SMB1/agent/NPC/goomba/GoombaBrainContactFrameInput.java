package kidridicarus.game.SMB1.agent.NPC.goomba;

import java.util.List;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agentbrain.BrainContactFrameInput;

public class GoombaBrainContactFrameInput extends BrainContactFrameInput {
	public List<Agent> headBounceBeginContacts;
	public GoombaBrainContactFrameInput(List<Agent> headBounceBeginContacts) {
		this.headBounceBeginContacts = headBounceBeginContacts;
	}
}
