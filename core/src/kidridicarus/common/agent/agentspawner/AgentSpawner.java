package kidridicarus.common.agent.agentspawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.DeadReturnTakeAgent;
import kidridicarus.common.agent.optional.EnableTakeAgent;
import kidridicarus.common.info.CommonKV;

public class AgentSpawner extends Agent implements EnableTakeAgent, DeadReturnTakeAgent, DisposableAgent {
	private AgentSpawnerBody sbody;
	private boolean isUsed;
	private String spawnAgentClassAlias;
	private boolean isRespawnDead;

	public AgentSpawner(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		spawnAgentClassAlias = properties.get(CommonKV.Spawn.KEY_SPAWNAGENTCLASS, "", String.class);
		isUsed = false;
		isRespawnDead = properties.get(CommonKV.Spawn.KEY_RESPAWN_DEAD, false, Boolean.class);;
		sbody = new AgentSpawnerBody(this, agency.getWorld(), Agent.getStartBounds(properties));
	}

	@Override
	public void onTakeEnable(boolean enabled) {
		if(enabled == false || isUsed)
			return;

		isUsed = true;
		ObjectProperties props = Agent.createPointAP(spawnAgentClassAlias, sbody.getPosition());
		// if spawned NPCs need to respawn then give them a ref to this spawner to allow callback when dead
		if(isRespawnDead)
			props.put(CommonKV.Spawn.KEY_SPAWNER_AGENT, this);
		agency.createAgent(props);
	}

	@Override
	public void onTakeDeadReturn(Agent deadAgent) {
		isUsed = false;
	}

	@Override
	public Vector2 getPosition() {
		return sbody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return sbody.getBounds();
	}

	@Override
	public void disposeAgent() {
		sbody.dispose();
	}
}
