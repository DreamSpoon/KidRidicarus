package kidridicarus.common.agentspine;

import java.util.List;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agent.despawnbox.DespawnBox;
import kidridicarus.common.agent.keepalivebox.KeepAliveBox;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;

/*
 * Complement of the AgentBody, the Agent spine allows for better organization/coordination of movement than
 * simply using AgentBody.
 */
public class BasicAgentSpine {
	protected AgentBody body;
	protected AgentContactHoldSensor agentSensor;

	public BasicAgentSpine(AgentBody body) {
		this.body = body;
		agentSensor = null;
	}

	public AgentContactHoldSensor createAgentSensor() {
		agentSensor = new AgentContactHoldSensor(body);
		return agentSensor;
	}

	public PowerupTakeAgent getTouchingPowerupTaker() {
		return agentSensor.getFirstContactByClass(PowerupTakeAgent.class);
	}

	public boolean isContactDespawn() {
		return agentSensor.getFirstContactByClass(DespawnBox.class) != null;
	}

	public boolean isTouchingKeepAlive() {
		return agentSensor.getFirstContactByClass(KeepAliveBox.class) != null;
	}

	public List<ContactDmgTakeAgent> getContactDmgTakeAgents() {
		return agentSensor.getContactsByClass(ContactDmgTakeAgent.class);
	}
}
