package kidridicarus.game.agentspine.SMB;

import java.util.List;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agent.despawnbox.DespawnBox;
import kidridicarus.common.agent.keepalivebox.KeepAliveBox;
import kidridicarus.common.agentsensor.AgentContactBeginSensor;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.SolidContactSensor;
import kidridicarus.common.agentspine.OnGroundSpine;

public class SMB_NPC_Spine extends OnGroundSpine {
	protected AgentBody body;
	private AgentContactHoldSensor agentHoldContactSensor;
	private AgentContactBeginSensor agentBeginContactSensor; 
	// horizontal move sensor
	private SolidContactSensor hmSensor;

	public SMB_NPC_Spine(AgentBody body) {
		this.body = body;
		agentHoldContactSensor = null;
		agentBeginContactSensor = null;
		hmSensor = null;
	}

	public AgentContactHoldSensor createAgentContactSensor() {
		agentHoldContactSensor = new AgentContactHoldSensor(body);
		return agentHoldContactSensor;
	}

	public AgentContactBeginSensor createHeadBounceAndContactDamageSensor() {
		agentBeginContactSensor = new AgentContactBeginSensor(body);
		return agentBeginContactSensor;
	}

	public SolidContactSensor createHorizontalMoveSensor() {
		hmSensor = new SolidContactSensor(body);
		return hmSensor;
	}

	public boolean checkReverseVelocity(boolean isFacingRight, boolean useAgents) {
		// if regular move is blocked...
		if(isMoveBlocked(isFacingRight) ||
				(isMoveBlockedByAgent(isFacingRight)) && useAgents) {
			// ... and reverse move is not also blocked then reverse 
			if(!isMoveBlocked(!isFacingRight) && (!useAgents ||
					!isMoveBlockedByAgent(!isFacingRight))) {
				return true;
			}
		}
		return false;
	}

	private boolean isMoveBlocked(boolean moveRight) {
		return hmSensor.isSolidOnThisSide(body.getBounds(), moveRight);
	}

	private boolean isMoveBlockedByAgent(boolean moveRight) {
		return AgentContactHoldSensor.isMoveBlockedByAgent(agentHoldContactSensor, body.getPosition(), moveRight);
	}

	public List<Agent> getAgentBeginContacts() {
		return agentBeginContactSensor.getAndResetContacts();
	}

	public boolean isTouchingKeepAlive() {
		return agentHoldContactSensor.getFirstContactByClass(KeepAliveBox.class) != null;
	}

	public boolean isContactDespawn() {
		return agentHoldContactSensor.getFirstContactByClass(DespawnBox.class) != null;
	}
}
