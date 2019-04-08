package kidridicarus.common.agentspine;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.agency.agentcontact.AgentContactSensor;
import kidridicarus.common.agent.agentspawntrigger.AgentSpawnTrigger;
import kidridicarus.common.agent.keepalivebox.KeepAliveBox;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;

public class WallContactSpine extends BasicAgentSpine {
	private HorizontalMoveNerve hmNerve;

	public WallContactSpine(AgentBody body) {
		super(body);
		hmNerve = new HorizontalMoveNerve();
	}

	public AgentContactSensor createHorizontalMoveSensor() {
		return hmNerve.createHorizontalMoveSensor(body);
	}

	public boolean isHorizontalMoveBlocked(boolean isFacingRight, boolean useAgents) {
		// If regular move is blocked...
		// ... and reverse move is not also blocked then reverse.
		if( (hmNerve.isSolidOnThisSide(body.getBounds(), isFacingRight) ||
				(useAgents && isMoveBlockedByAgent(isFacingRight))) &&
			(!hmNerve.isSolidOnThisSide(body.getBounds(), !isFacingRight) &&
				(!useAgents || !isMoveBlockedByAgent(!isFacingRight))) ) {
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

			// If wants to move right and other agent is on the right side then move is blocked, or
			// if wants to move left and other agent is on the left side then move is blocked.
			if((moveRight && body.getPosition().x < agent.getPosition().x) ||
					(!moveRight && body.getPosition().x > agent.getPosition().x))
				return true;
		}
		return false;
	}

	public boolean isSolidOnThisSide(boolean isRight) {
		return hmNerve.isSolidOnThisSide(body.getBounds(), isRight);
	}
}
