package kidridicarus.game.agentspine.SMB1;

import java.util.List;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentsensor.OneWayContactSensor;

public class HeadBounceNerve {
	private OneWayContactSensor headBounceSensor = null;

	public OneWayContactSensor createHeadBounceSensor(AgentBody body) {
		headBounceSensor = new OneWayContactSensor(body, true);
		return headBounceSensor;
	}

	public List<Agent> getHeadBounceBeginContacts() {
		return headBounceSensor.getAndResetContacts();
	}
}
