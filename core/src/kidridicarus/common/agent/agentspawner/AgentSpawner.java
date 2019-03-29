package kidridicarus.common.agent.agentspawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.EnableTakeAgent;
import kidridicarus.common.info.CommonKV;

public class AgentSpawner extends Agent implements EnableTakeAgent, DisposableAgent {
	private AgentSpawnerBody sbody;
	private boolean isUsed;
	private String spawnAgentClassAlias;
	private boolean isRespawnDead;

	public AgentSpawner(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		spawnAgentClassAlias = properties.get(CommonKV.Spawn.KEY_SPAWNAGENTCLASS, "", String.class);
		isUsed = false;
		isRespawnDead = properties.get(CommonKV.Spawn.KEY_RESPAWN_DEAD, false, Boolean.class);
		sbody = new AgentSpawnerBody(this, agency.getWorld(), Agent.getStartBounds(properties));
	}

	@Override
	public void onTakeEnable(boolean enabled) {
		if(enabled == false || isUsed)
			return;

		isUsed = true;
		Agent spawnedAgent = agency.createAgent(Agent.createPointAP(spawnAgentClassAlias, sbody.getPosition()));
		if(isRespawnDead) {
			agency.addAgentRemoveListener(new AgentRemoveListener(this, spawnedAgent) {
					@Override
					public void removedAgent() { isUsed = false; }
				});
		}
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
