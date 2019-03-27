package kidridicarus.common.agentspine;

import java.util.List;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agent.despawnbox.DespawnBox;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.SolidContactSensor;

public class GameAgentSpine {
	protected AgentBody body;
	protected AgentContactHoldSensor agentSensor;
	private SolidContactSensor ogSensor;

	public GameAgentSpine(AgentBody body) {
		this.body = body;
		agentSensor = null;
		ogSensor = null;
	}

	public AgentContactHoldSensor createAgentSensor() {
		agentSensor = new AgentContactHoldSensor(body);
		return agentSensor;
	}

	public SolidContactSensor createOnGroundSensor() {
		ogSensor = new SolidContactSensor(null);
		return ogSensor;
	}

	public boolean isContactDespawn() {
		return agentSensor.getFirstContactByClass(DespawnBox.class) != null;
	}

	public List<ContactDmgTakeAgent> getContactDmgTakeAgents() {
		return agentSensor.getContactsByClass(ContactDmgTakeAgent.class);
	}

	public boolean isOnGround() {
		return ogSensor.isContactFloor();
	}
}
