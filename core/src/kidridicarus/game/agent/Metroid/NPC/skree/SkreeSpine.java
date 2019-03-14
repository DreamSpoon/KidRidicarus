package kidridicarus.game.agent.Metroid.NPC.skree;

import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentspine.OnGroundSpine;

public class SkreeSpine extends OnGroundSpine {
	private SkreeBody body;
	private AgentContactHoldSensor acSensor;
	private AgentContactHoldSensor playerSensor;

	public SkreeSpine(SkreeBody body) {
		this.body = body;
		acSensor = null;
		playerSensor = null;
	}

	public AgentContactHoldSensor createAgentSensor() {
		acSensor = new AgentContactHoldSensor(body);
		return acSensor;
	}

	public AgentContactHoldSensor createPlayerSensor() {
		playerSensor = new AgentContactHoldSensor(null);
		return playerSensor;
	}

	public PlayerAgent getPlayerContact() {
		return playerSensor.getFirstContactByClass(PlayerAgent.class);
	}
}
