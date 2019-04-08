package kidridicarus.common.agentspine;

import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;

public class PlayerContactNerve {
	private AgentContactHoldSensor playerSensor = null;

	public AgentContactHoldSensor createPlayerSensor() {
		playerSensor = new AgentContactHoldSensor(null);
		return playerSensor;
	}

	public PlayerAgent getFirstPlayerContact() {
		if(playerSensor == null)
			return null;
		return playerSensor.getFirstContactByClass(PlayerAgent.class);
	}
}
