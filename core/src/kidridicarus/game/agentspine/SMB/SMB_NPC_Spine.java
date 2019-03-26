package kidridicarus.game.agentspine.SMB;

import java.util.List;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agent.PlayerAgent;
import kidridicarus.common.agent.agentspawntrigger.AgentSpawnTrigger;
import kidridicarus.common.agent.keepalivebox.KeepAliveBox;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentsensor.AgentContactBeginSensor;
import kidridicarus.common.agentsensor.SolidContactSensor;
import kidridicarus.common.agentspine.NPC_Spine;

public class SMB_NPC_Spine extends NPC_Spine {
	private AgentContactBeginSensor headBounceSensor; 
	// horizontal move sensor
	private SolidContactSensor hmSensor;

	public SMB_NPC_Spine(AgentBody body) {
		super(body);
		headBounceSensor = null;
		hmSensor = null;
	}

	public AgentContactBeginSensor createHeadBounceAndContactDamageSensor() {
		headBounceSensor = new AgentContactBeginSensor(body);
		return headBounceSensor;
	}

	public SolidContactSensor createHorizontalMoveSensor() {
		hmSensor = new SolidContactSensor(body);
		return hmSensor;
	}

	public boolean checkReverseVelocity(boolean isFacingRight, boolean useAgents) {
		// If regular move is blocked...
		// ... and reverse move is not also blocked then reverse.
		if( (isMoveBlockedBySolid(isFacingRight) || (useAgents && isMoveBlockedByAgent(isFacingRight))) &&
				(!isMoveBlockedBySolid(!isFacingRight) && (!useAgents || !isMoveBlockedByAgent(!isFacingRight))) ) {
			return true;
		}
		return false;
	}

	private boolean isMoveBlockedByAgent(boolean moveRight) {
		for(Agent agent : agentSensor.getContacts()) {
			// Do not check against players - just damage them or whatever, also don't check against
			// some special things like RoomBox.
			if(agent instanceof PlayerAgent || agent instanceof RoomBox || agent instanceof AgentSpawnTrigger ||
					agent instanceof KeepAliveBox)
				continue;

			// If wants to move right and other agent is on the right side then move is blocked
			if(moveRight && body.getPosition().x < agent.getPosition().x)
				return true;
			// If wants to move left and other agent is on the left side then move is blocked
			else if(!moveRight && body.getPosition().x > agent.getPosition().x)
				return true;
		}
		return false;
	}

	private boolean isMoveBlockedBySolid(boolean moveRight) {
		return hmSensor.isSolidOnThisSide(body.getBounds(), moveRight);
	}

	public List<Agent> getHeadBounceBeginContacts() {
		return headBounceSensor.getAndResetContacts();
	}
}
