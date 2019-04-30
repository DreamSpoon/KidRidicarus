package kidridicarus.common.agent.agentspawntrigger;

import java.util.List;

import kidridicarus.common.agent.optional.EnableTakeAgent;

public class SpawnTriggerFrameInput {
	public List<EnableTakeAgent> beginContacts;
	public List<EnableTakeAgent> endContacts;

	public SpawnTriggerFrameInput(List<EnableTakeAgent> beginContacts, List<EnableTakeAgent> endContacts) {
		this.beginContacts = beginContacts;
		this.endContacts = endContacts;
	}
}
