package kidridicarus.game.agent.SMB.NPC.goomba;

import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.OnGroundSensor;
import kidridicarus.common.agentsensor.SolidBoundSensor;

public class GoombaSpine {
	private GoombaBody body;
	private OnGroundSensor ogSensor;
	// horizontal move sensor
	private SolidBoundSensor hmSensor;
	private AgentContactHoldSensor acSensor;

	public GoombaSpine(GoombaBody body) {
		this.body = body;
		ogSensor = null;
		hmSensor = null;
		acSensor = null;
	}

	public SolidBoundSensor createHorizontalMoveSensor() {
		hmSensor = new SolidBoundSensor(body);
		return hmSensor;
	}

	public AgentContactHoldSensor createAgentSensor() {
		acSensor = new AgentContactHoldSensor(this);
		return acSensor;
	}

	public OnGroundSensor createOnGroundSensor() {
		ogSensor = new OnGroundSensor(null);
		return ogSensor;
	}

	public boolean isMoveBlocked(boolean moveRight) {
		return hmSensor.isHMoveBlocked(body.getBounds(), moveRight);
	}

	public boolean isOnGround() {
		// return true if the on ground contacts list contains at least 1 floor
		return ogSensor.isOnGround();
	}

	public boolean isMoveBlockedByAgent(boolean moveRight) {
		return AgentContactHoldSensor.isMoveBlockedByAgent(acSensor, body.getPosition(), moveRight);
	}
}
