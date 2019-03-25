package kidridicarus.common.agentspine;

import java.util.List;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agent.despawnbox.DespawnBox;
import kidridicarus.common.agent.keepalivebox.KeepAliveBox;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;

public abstract class NPC_Spine extends OnGroundSpine {
	protected AgentBody body;
	protected AgentContactHoldSensor agentSensor;

	public NPC_Spine(AgentBody body) {
		this.body = body;
		agentSensor = null;
	}

	public AgentContactHoldSensor createAgentSensor() {
		agentSensor = new AgentContactHoldSensor(body);
		return agentSensor;
	}

	public boolean isTouchingKeepAlive() {
		return agentSensor.getFirstContactByClass(KeepAliveBox.class) != null;
	}

	public boolean isContactDespawn() {
		return agentSensor.getFirstContactByClass(DespawnBox.class) != null;
	}

	public List<ContactDmgTakeAgent> getContactDmgTakeAgents() {
		return agentSensor.getContactsByClass(ContactDmgTakeAgent.class);
	}
}
