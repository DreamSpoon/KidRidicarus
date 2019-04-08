package kidridicarus.common.agentspine;

import java.util.List;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.agency.agentcontact.AgentContactSensor;
import kidridicarus.common.agent.agentspawntrigger.AgentSpawnTrigger;
import kidridicarus.common.agent.keepalivebox.KeepAliveBox;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentsensor.OneWayContactSensor;
import kidridicarus.common.agentsensor.SolidContactSensor;
import kidridicarus.game.agentspine.SMB1.HeadBounceNerve;

public class SMB_NPC_Spine extends BasicAgentSpine {
	private OnGroundNerve ogNerve;
	private HorizontalMoveNerve hmNerve;
	private HeadBounceNerve hbNerve;

	public SMB_NPC_Spine(AgentBody body) {
		super(body);
		ogNerve = new OnGroundNerve();
		hmNerve = new HorizontalMoveNerve();
		hbNerve = new HeadBounceNerve();
	}

	public SolidContactSensor createOnGroundSensor() {
		return ogNerve.createOnGroundSensor();
	}

	public AgentContactSensor createHorizontalMoveSensor() {
		return hmNerve.createHorizontalMoveSensor(body);
	}

	public OneWayContactSensor createHeadBounceSensor() {
		return hbNerve.createHeadBounceSensor(body);
	}

	public boolean isOnGround() {
		return ogNerve.isOnGround();
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

			// If wants to move right and other agent is on the right side then move is blocked, or
			// if wants to move left and other agent is on the left side then move is blocked.
			if((moveRight && body.getPosition().x < agent.getPosition().x) ||
					(!moveRight && body.getPosition().x > agent.getPosition().x))
				return true;
		}
		return false;
	}

	private boolean isMoveBlockedBySolid(boolean moveRight) {
		return hmNerve.isSolidOnThisSide(body.getBounds(), moveRight);
	}

	public List<Agent> getHeadBounceBeginContacts() {
		return hbNerve.getHeadBounceBeginContacts();
	}
}
