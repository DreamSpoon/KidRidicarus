package kidridicarus.common.agent.agentspawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.EnableTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;

public class AgentSpawner extends Agent implements EnableTakeAgent, DisposableAgent {
	private SpawnController spawnController;
	private AgentSpawnerBody body;
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

		body = new AgentSpawnerBody(this, agency.getWorld(), Agent.getStartBounds(properties));
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) {
					if(spawnController != null)
						spawnController.update(delta, isEnabled);
				}
			});

		String spawnerType = properties.get(CommonKV.Spawn.KEY_SPAWNER_TYPE, "", String.class);
		if(spawnerType.equals(CommonKV.Spawn.VAL_SPAWNER_TYPE_MULTI))
			spawnController = new MultiSpawnController(this, body, properties);
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
	public Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
