package kidridicarus.common.agent.agentspawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.Agent;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;

abstract class SpawnController {
	AgentSpawner parent;
	AgentHooks parentHooks;
	private String spawnAgentClassAlias;
	private Boolean isRandomPos;

	abstract void update(FrameTime frameTime, boolean isEnabled);

	SpawnController(AgentSpawner parent, AgentHooks parentHooks, ObjectProperties properties) {
		this.parent = parent;
		this.parentHooks = parentHooks;
		this.spawnAgentClassAlias = properties.getString(CommonKV.Spawn.KEY_SPAWN_AGENTCLASS, "");
		// spawn in random position within spawn body boundaries?
		isRandomPos = properties.getBoolean(CommonKV.Spawn.KEY_SPAWN_RAND_POS, false);
	}

	Agent doSpawn(Vector2 position) {
		return parentHooks.createAgent(AP_Tool.createPointAP(spawnAgentClassAlias, position));
	}

	Agent doSpawn() {
		// get spawn position and exit if unavailable
		Vector2 spawnPos = AP_Tool.getCenter(parent);
		if(spawnPos == null)
			return null;
		// apply random positioning if needed and available
		if(isRandomPos) {
			Rectangle spawnBounds = AP_Tool.getBounds(parent);
			if(spawnBounds != null) {
				spawnPos = new Vector2((float) (spawnBounds.x + spawnBounds.width * Math.random()),
						(float) (spawnBounds.y + spawnBounds.height * Math.random()));
			}
		}
		return doSpawn(spawnPos);
	}
}
