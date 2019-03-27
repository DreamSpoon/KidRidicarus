package kidridicarus.common.agentspine;

import java.util.List;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agent.agentspawntrigger.AgentSpawnTrigger;
import kidridicarus.common.agent.despawnbox.DespawnBox;
import kidridicarus.common.agent.keepalivebox.KeepAliveBox;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.SolidContactSensor;

public class NPC_Spine extends OnGroundSpine {
	protected AgentBody body;
	protected AgentContactHoldSensor agentSensor;
	private SolidContactSensor horizontalMoveSensor;

	public NPC_Spine(AgentBody body) {
		this.body = body;
		agentSensor = null;
		horizontalMoveSensor = null;
	}

	public AgentContactHoldSensor createAgentSensor() {
		agentSensor = new AgentContactHoldSensor(body);
		return agentSensor;
	}

	public SolidContactSensor createHorizontalMoveSensor() {
		horizontalMoveSensor = new SolidContactSensor(body);
		return horizontalMoveSensor;
	}

	public boolean isHorizontalMoveBlocked(boolean isFacingRight, boolean useAgents) {
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
		return horizontalMoveSensor.isSolidOnThisSide(body.getBounds(), moveRight);
	}

	public boolean isTouchingKeepAlive() {
		return agentSensor.getFirstContactByClass(KeepAliveBox.class) != null;
	}

	public boolean isContactDespawn() {
		return agentSensor.getFirstContactByClass(DespawnBox.class) != null;
	}

	public List<ContactDmgTakeAgent> getContactDmgTakeAgents() {
		return agentSensor.getContactsByClass(ContactDmgTakeAgent.class);
	}

	public PowerupTakeAgent getTouchingPowerupTaker() {
		return agentSensor.getFirstContactByClass(PowerupTakeAgent.class);
	}
}
