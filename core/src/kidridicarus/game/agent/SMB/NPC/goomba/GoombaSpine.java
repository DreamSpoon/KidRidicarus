package kidridicarus.game.agent.SMB.NPC.goomba;

import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.SolidBoundSensor;
import kidridicarus.common.agentspine.OnGroundSpine;

public class GoombaSpine extends OnGroundSpine {
	private GoombaBody body;
	// horizontal move sensor
	private SolidBoundSensor hmSensor;
	private AgentContactHoldSensor acSensor;

	public GoombaSpine(GoombaBody body) {
		this.body = body;
		hmSensor = null;
		acSensor = null;
	}

	public SolidBoundSensor createHorizontalMoveSensor() {
		hmSensor = new SolidBoundSensor(body);
		return hmSensor;
	}

	public AgentContactHoldSensor createAgentSensor() {
		acSensor = new AgentContactHoldSensor(body);
		return acSensor;
	}

	public boolean isMoveBlocked(boolean moveRight) {
		return hmSensor.isHMoveBlocked(body.getBounds(), moveRight);
	}

	public boolean isMoveBlockedByAgent(boolean moveRight) {
		return AgentContactHoldSensor.isMoveBlockedByAgent(acSensor, body.getPosition(), moveRight);
	}
}
