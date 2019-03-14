package kidridicarus.game.agent.SMB.other.bumptile;

import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.metaagent.tiledmap.collision.CollisionTiledMapAgent;
import kidridicarus.game.agent.SMB.TileBumpGiveAgent;

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

	public CollisionTiledMapAgent getCollisionMap() {
		if(mainSensor == null)
			return null;
		return mainSensor.getFirstContactByClass(CollisionTiledMapAgent.class);
	}

	public TileBumpGiveAgent getTileBumpGiveAgent() {
		if(mainSensor == null)
			return null;
		return mainSensor.getFirstContactByClass(TileBumpGiveAgent.class);
	}
}
