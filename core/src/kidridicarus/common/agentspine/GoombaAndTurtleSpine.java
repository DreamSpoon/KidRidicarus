package kidridicarus.common.agentspine;

import java.util.List;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.common.agent.despawnbox.DespawnBox;
import kidridicarus.common.agentsensor.AgentContactBeginSensor;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.SolidBoundSensor;

public class GoombaAndTurtleSpine extends OnGroundSpine {
	protected AgentBody body;
	private AgentContactHoldSensor acSensor;
	private AgentContactBeginSensor headBounceAndContactDmgSensor; 
	// horizontal move sensor
	private SolidBoundSensor hmSensor;

	public GoombaAndTurtleSpine(AgentBody body) {
		this.body = body;
		acSensor = null;
		headBounceAndContactDmgSensor = null;
		hmSensor = null;
	}

	public AgentContactHoldSensor createAgentContactSensor() {
		acSensor = new AgentContactHoldSensor(body);
		return acSensor;
	}

	public AgentContactBeginSensor createHeadBounceAndContactDamageSensor() {
		headBounceAndContactDmgSensor = new AgentContactBeginSensor(body);
		return headBounceAndContactDmgSensor;
	}

	public SolidBoundSensor createHorizontalMoveSensor() {
		hmSensor = new SolidBoundSensor(body);
		return hmSensor;
	}

	public boolean checkReverseVelocity(boolean isFacingRight) {
		// if regular move is blocked...
		if(isMoveBlocked(isFacingRight) ||
				isMoveBlockedByAgent(isFacingRight)) {
			// ... and reverse move is not also blocked then reverse 
			if(!isMoveBlocked(!isFacingRight) &&
					!isMoveBlockedByAgent(!isFacingRight)) {
				return true;
			}
		}
		return false;
	}

	private boolean isMoveBlocked(boolean moveRight) {
		return hmSensor.isHMoveBlocked(body.getBounds(), moveRight);
	}

	private boolean isMoveBlockedByAgent(boolean moveRight) {
		return AgentContactHoldSensor.isMoveBlockedByAgent(acSensor, body.getPosition(), moveRight);
	}

	public List<Agent> getHeadBounceAndContactDamageAgents() {
		return headBounceAndContactDmgSensor.getAndResetContacts();
	}

	public boolean isContactDespawn() {
		return acSensor.getFirstContactByClass(DespawnBox.class) != null;
	}
}
