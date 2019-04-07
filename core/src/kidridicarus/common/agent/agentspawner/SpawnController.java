package kidridicarus.common.agent.agentspawner;

import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonKV;

public abstract class SpawnController {
	protected AgentSpawner spawner;
	protected String spawnAgentClassAlias;

	public abstract void update(float delta, boolean isEnabled);

	public SpawnController(AgentSpawner spawner, ObjectProperties properties) {
		this.spawner = spawner;
		this.spawnAgentClassAlias = properties.get(CommonKV.Spawn.KEY_SPAWN_AGENTCLASS, "", String.class);
	}
}
