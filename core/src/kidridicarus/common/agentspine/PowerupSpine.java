package kidridicarus.common.agentspine;

import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;

public class PowerupSpine {
	private AgentBody body;
	private AgentContactHoldSensor acSensor;

	public PowerupSpine(AgentBody body) {
		this.body = body;
		acSensor = null;
	}

	public AgentContactHoldSensor createAgentSensor() {
		acSensor = new AgentContactHoldSensor(body);
		return acSensor;
	}

	public PowerupTakeAgent getTouchingPowerupTaker() {
		return acSensor.getFirstContactByClass(PowerupTakeAgent.class);
	}
}
