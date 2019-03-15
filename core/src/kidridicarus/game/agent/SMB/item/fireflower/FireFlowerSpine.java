package kidridicarus.game.agent.SMB.item.fireflower;

import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;

public class FireFlowerSpine {
	private FireFlowerBody body;
	private AgentContactHoldSensor acSensor;

	public FireFlowerSpine(FireFlowerBody body) {
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
