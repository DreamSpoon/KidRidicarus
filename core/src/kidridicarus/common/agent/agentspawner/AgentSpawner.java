package kidridicarus.common.agent.agentspawner;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.corpusagent.CorpusAgent;
import kidridicarus.common.agent.optional.EnableTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;

public class AgentSpawner extends CorpusAgent implements EnableTakeAgent, DisposableAgent {
	private SpawnController spawnController;
	private boolean isEnabled;

	public AgentSpawner(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		// verify that the class of the Agent to be spawned is a valid Agent class
		String spawnAgentClassAlias = properties.get(CommonKV.Spawn.KEY_SPAWN_AGENTCLASS, "", String.class);
		if(!agency.isValidAgentClassAlias(spawnAgentClassAlias)) {
			throw new IllegalStateException(
					"Cannot create AgentSpawner with non-valid agent class alias =" + spawnAgentClassAlias);
		}

		isEnabled = false;

		body = new AgentSpawnerBody(this, agency.getWorld(), AP_Tool.getBounds(properties));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) {
					if(spawnController != null)
						spawnController.update(delta, isEnabled);
				}
			});

		String spawnerType = properties.get(CommonKV.Spawn.KEY_SPAWNER_TYPE, "", String.class);
		if(spawnerType.equals(CommonKV.Spawn.VAL_SPAWNER_TYPE_MULTI))
			spawnController = new MultiSpawnController(this, (AgentSpawnerBody) body, properties);
		else if(spawnerType.equals(CommonKV.Spawn.VAL_SPAWNER_TYPE_RESPAWN))
			spawnController = new DeadRespawnController(this, properties);
		else
			spawnController = new SingleSpawnController(this, properties);
	}

	@Override
	public void onTakeEnable(boolean enabled) {
		this.isEnabled = enabled;
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
