package kidridicarus.common.agent.agentspawner;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;

class SingleSpawnController extends SpawnController {
	private boolean isSpawned;

	SingleSpawnController(AgentSpawner parent, AgentHooks parentHooks, ObjectProperties properties) {
		super(parent, parentHooks, properties);
		isSpawned = false;
	}

	@Override
	void update(FrameTime frameTime, boolean isEnabled) {
		if(isEnabled && !isSpawned) {
			isSpawned = true;
			doSpawn();
		}
	}
}
