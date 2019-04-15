package kidridicarus.common.agent.agentspawner;

import kidridicarus.agency.tool.ObjectProperties;

public class SingleSpawnController extends SpawnController {
	private boolean isSpawned;

	public SingleSpawnController(AgentSpawner spawner, ObjectProperties properties) {
		super(spawner, properties);
		isSpawned = false;
	}

	@Override
	public void update(float delta, boolean isEnabled) {
		if(isEnabled && !isSpawned) {
			isSpawned = true;
			doSpawn();
		}
	}
}
