package kidridicarus.game.agentspine.SMB;

import java.util.List;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agentbody.MobileAgentBody;
import kidridicarus.common.agentsensor.OneWayContactSensor;
import kidridicarus.common.agentspine.NPC_Spine;

public class HeadBounceSpine extends NPC_Spine {
	private OneWayContactSensor headBounceSensor;

	public HeadBounceSpine(MobileAgentBody body) {
		super(body);
		headBounceSensor = null;
	}

	public OneWayContactSensor createHeadBounceAndContactDamageSensor() {
		headBounceSensor = new OneWayContactSensor(body, true);
		return headBounceSensor;
	}

	public List<Agent> getHeadBounceBeginContacts() {
		return headBounceSensor.getAndResetContacts();
	}
}
