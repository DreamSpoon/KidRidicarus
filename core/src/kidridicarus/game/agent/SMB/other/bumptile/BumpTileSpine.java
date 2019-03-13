package kidridicarus.game.agent.SMB.other.bumptile;

import kidridicarus.common.agent.collisionmap.OrthoCollisionTiledMapAgent;
import kidridicarus.common.agentbody.sensor.AgentContactHoldSensor;
import kidridicarus.game.SMB.agent.TileBumpGiveAgent;

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

	public OrthoCollisionTiledMapAgent getCollisionMap() {
		if(mainSensor == null)
			return null;
		return mainSensor.getFirstContactByClass(OrthoCollisionTiledMapAgent.class);
	}

	public TileBumpGiveAgent getTileBumpGiveAgent() {
		if(mainSensor == null)
			return null;
		return mainSensor.getFirstContactByClass(TileBumpGiveAgent.class);
	}
}
