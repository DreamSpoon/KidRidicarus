package kidridicarus.game.SMB1.agentspine;

import java.util.List;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.common.agentsensor.OneWayContactSensor;

public class HeadBounceNerve {
	private OneWayContactSensor headBounceSensor = null;

	public OneWayContactSensor createHeadBounceSensor(AgentBody body) {
		headBounceSensor = new OneWayContactSensor(body, true);
		return headBounceSensor;
	}

	public List<Agent> getHeadBounceBeginContacts() {
		if(headBounceSensor == null)
			return null;
		return headBounceSensor.getAndResetContacts();
	}
}
