package kidridicarus.common.agentspine;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.agentspawntrigger.AgentSpawnTrigger;
import kidridicarus.common.agent.keepalivebox.KeepAliveBox;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentbody.MobileAgentBody;
import kidridicarus.common.agentsensor.SolidContactSensor;

public class NPC_Spine extends MobileAgentSpine {
	private SolidContactSensor horizontalMoveSensor;

	public NPC_Spine(MobileAgentBody body) {
		super(body);
		horizontalMoveSensor = null;
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
}
