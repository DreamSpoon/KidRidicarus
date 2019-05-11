package kidridicarus.common.agent.agentspawntrigger;

import java.util.List;

import kidridicarus.common.agent.optional.EnableTakeAgent;

class SpawnTriggerFrameInput {
	List<EnableTakeAgent> beginContacts;
	List<EnableTakeAgent> endContacts;

	SpawnTriggerFrameInput(List<EnableTakeAgent> beginContacts, List<EnableTakeAgent> endContacts) {
		this.beginContacts = beginContacts;
		this.endContacts = endContacts;
	}
}
