package kidridicarus.game.agent.SMB.NPC.turtle;

import java.util.List;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agentsensor.AgentContactBeginSensor;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.OnGroundSensor;
import kidridicarus.common.agentsensor.SolidBoundSensor;

public class TurtleSpine {
	private OnGroundSensor ogSensor;
	// horizontal move sensor
	private SolidBoundSensor hmSensor;
	private AgentContactHoldSensor acSensor;
	private AgentContactBeginSensor kickSensor;
	private TurtleBody body;

	public TurtleSpine(TurtleBody body) {
		this.body = body;
		ogSensor = null;
		hmSensor = null;
		acSensor = null;
		kickSensor = null;
	}

	public AgentContactBeginSensor createAgentContactSensor() {
		acSensor = new AgentContactHoldSensor(body);
		kickSensor = new AgentContactBeginSensor(body);
		kickSensor.chainTo(acSensor);
		return kickSensor;
	}

	public SolidBoundSensor createHorizontalMoveSensor() {
		hmSensor = new SolidBoundSensor(body);
		return hmSensor;
	}

	public OnGroundSensor createOnGroundSensor() {
		ogSensor = new OnGroundSensor(null);
		return ogSensor;
	}

	public boolean isMoveBlocked(boolean moveRight) {
		return hmSensor.isHMoveBlocked(body.getBounds(), moveRight);
	}

	public boolean isMoveBlockedByAgent(boolean moveRight) {
		return AgentContactHoldSensor.isMoveBlockedByAgent(acSensor, body.getPosition(), moveRight);
	}

	public boolean isOnGround() {
		// return true if the on ground contacts list contains at least 1 floor
		return ogSensor.isOnGround();
	}

	public List<Agent> getAndResetContactBeginAgents() {
		return kickSensor.getAndResetContacts();
	}
}
