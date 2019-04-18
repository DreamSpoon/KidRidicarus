package kidridicarus.common.agent.agentspawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;

public abstract class SpawnController {
	protected AgentSpawner spawner;
	private String spawnAgentClassAlias;
	private Boolean isRandomPos;

	public abstract void update(float delta, boolean isEnabled);

	public SpawnController(AgentSpawner spawner, ObjectProperties properties) {
		this.spawner = spawner;
		this.spawnAgentClassAlias = properties.get(CommonKV.Spawn.KEY_SPAWN_AGENTCLASS, "", String.class);
		// spawn in random position within spawn body boundaries?
		isRandomPos = properties.get(CommonKV.Spawn.KEY_SPAWN_RAND_POS, false, Boolean.class);
	}

	protected Agent doSpawn(Vector2 position) {
		return spawner.getAgency().createAgent(AP_Tool.createPointAP(spawnAgentClassAlias, position));
	}

	protected Agent doSpawn() {
		Vector2 spawnPos = spawner.getPosition().cpy();
		// apply random positioning if needed
		if(isRandomPos) {
			Rectangle bounds = spawner.getBounds();
			spawnPos.set((float) (bounds.x + bounds.width * Math.random()),
					(float) (bounds.y + bounds.height * Math.random()));
		}
		return doSpawn(spawnPos);
	}
}
