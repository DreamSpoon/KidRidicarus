package kidridicarus.game.agent.SMB1.other.bumptile;

import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.metaagent.tiledmap.solidlayer.SolidTiledMapAgent;

public class BumpTileSpine {
	private BumpTileBody body;
	private AgentContactHoldSensor mainSensor;

	public BumpTileSpine(BumpTileBody body) {
		this.body = body;
		mainSensor = null;
	}

	public AgentContactHoldSensor createMainSensor() {
		mainSensor = new AgentContactHoldSensor(body);
		return mainSensor;
	}

	public SolidTiledMapAgent getSolidTileMap() {
		if(mainSensor == null)
			return null;
		return mainSensor.getFirstContactByClass(SolidTiledMapAgent.class);
	}
}
