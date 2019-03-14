package kidridicarus.game.agent.Metroid.NPC.skree;

import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.OnGroundSensor;

public class SkreeSpine {
	private SkreeBody body;
	private OnGroundSensor ogSensor;
	private AgentContactHoldSensor acSensor;
	private AgentContactHoldSensor playerSensor;

	public SkreeSpine(SkreeBody body) {
		this.body = body;
		ogSensor = null;
		acSensor = null;
		playerSensor = null;
	}

	public AgentContactHoldSensor createAgentSensor() {
		acSensor = new AgentContactHoldSensor(body);
		return acSensor;
	}

	public OnGroundSensor createOnGroundSensor() {
		ogSensor = new OnGroundSensor(null);
		return ogSensor;
	}

	public AgentContactHoldSensor createPlayerSensor() {
		playerSensor = new AgentContactHoldSensor(null);
		return playerSensor;
	}

	public boolean isOnGround() {
		return ogSensor.isOnGround();
	}

	public PlayerAgent getPlayerContact() {
		return playerSensor.getFirstContactByClass(PlayerAgent.class);
	}
}
