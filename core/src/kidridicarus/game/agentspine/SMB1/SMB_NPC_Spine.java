package kidridicarus.game.agentspine.SMB1;

import java.util.List;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentsensor.OneWayContactSensor;
import kidridicarus.common.agentspine.FloorWallContactSpine;

public class SMB_NPC_Spine extends FloorWallContactSpine {
	private HeadBounceNerve hbNerve;

	public SMB_NPC_Spine(AgentBody body) {
		super(body);
		hbNerve = new HeadBounceNerve();
	}

	public OneWayContactSensor createHeadBounceSensor() {
		return hbNerve.createHeadBounceSensor(body);
	}

	public List<Agent> getHeadBounceBeginContacts() {
		return hbNerve.getHeadBounceBeginContacts();
	}
}
