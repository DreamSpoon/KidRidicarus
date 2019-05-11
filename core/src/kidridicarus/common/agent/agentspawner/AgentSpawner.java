package kidridicarus.common.agent.agentspawner;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.agent.optional.EnableTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;

public class AgentSpawner extends CorpusAgent implements EnableTakeAgent {
	private SpawnController spawnController;
	private boolean isEnabled;

	public AgentSpawner(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		// verify that the class of the Agent to be spawned is a valid Agent class
		String spawnAgentClassAlias = properties.getString(CommonKV.Spawn.KEY_SPAWN_AGENTCLASS, "");
		if(!agentHooks.isValidAgentClassAlias(spawnAgentClassAlias)) {
			throw new IllegalStateException(
					"Cannot create AgentSpawner with non-valid agent class alias =" + spawnAgentClassAlias);
		}
		isEnabled = false;
		body = new AgentSpawnerBody(this, agentHooks.getWorld(), AP_Tool.getBounds(properties));
		// create controller for the desired type of spawning
		String spawnerType = properties.getString(CommonKV.Spawn.KEY_SPAWNER_TYPE, "");
		if(spawnerType.equals(CommonKV.Spawn.VAL_SPAWNER_TYPE_MULTI))
			spawnController = new MultiSpawnController(this, agentHooks, (AgentSpawnerBody) body, properties);
		else if(spawnerType.equals(CommonKV.Spawn.VAL_SPAWNER_TYPE_RESPAWN))
			spawnController = new DeadRespawnController(this, agentHooks, properties);
		else
			spawnController = new SingleSpawnController(this, agentHooks, properties);

		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(FrameTime frameTime) {
				if(spawnController != null)
					spawnController.update(frameTime, isEnabled);
			}
		});
		// need to dispose body on Agent removal
		agentHooks.createAgentRemoveListener(this, new AgentRemoveCallback() {
			@Override
			public void preRemoveAgent() { dispose(); }
		});
	}

	@Override
	public void onTakeEnable(boolean enabled) {
		this.isEnabled = enabled;
	}
}
