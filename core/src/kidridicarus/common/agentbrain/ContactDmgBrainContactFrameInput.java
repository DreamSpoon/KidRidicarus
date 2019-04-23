package kidridicarus.common.agentbrain;

import java.util.List;

import kidridicarus.common.agent.optional.ContactDmgTakeAgent;

public class ContactDmgBrainContactFrameInput extends BrainContactFrameInput {
	public List<ContactDmgTakeAgent> contactDmgTakeAgents;
	public ContactDmgBrainContactFrameInput(List<ContactDmgTakeAgent> contactDmgTakeAgents) {
		this.contactDmgTakeAgents = contactDmgTakeAgents;
	}
}
