package kidridicarus.common.agent.agentspawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.EnableTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;

public class AgentSpawner extends Agent implements EnableTakeAgent, DisposableAgent {
	private AgentSpawnerBody sbody;
	private String spawnAgentClassAlias;

	private boolean isEnabled;
	private boolean isRespawnDead;
	private int spawnMultiCount;
	private int spawnMultiGrpCount;
	private float spawnMultiRate;

	private int numSpawns;
	private int numSpawnsDisposed;
	private float spawnTimer;

	public AgentSpawner(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		spawnAgentClassAlias = properties.get(CommonKV.Spawn.KEY_SPAWN_AGENTCLASS, "", String.class);
		isRespawnDead = properties.get(CommonKV.Spawn.KEY_RESPAWN_DEAD, false, Boolean.class);
		spawnMultiCount = properties.get(CommonKV.Spawn.KEY_SPAWN_MULTI_COUNT, 0, Integer.class);
		spawnMultiGrpCount = properties.get(CommonKV.Spawn.KEY_SPAWN_MULTI_GRP_COUNT, 0, Integer.class);
		spawnMultiRate = properties.get(CommonKV.Spawn.KEY_SPAWN_MULTI_RATE, 0f, Float.class);

		numSpawns = 0;
		numSpawnsDisposed = 0;
		spawnTimer = 0f;

		sbody = new AgentSpawnerBody(this, agency.getWorld(), Agent.getStartBounds(properties));
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
	}

	private void doUpdate(float delta) {
		// DEBUG: error state check
		if(numSpawnsDisposed > numSpawns)
			throw new IllegalStateException("numSpawnsDisposed ("+numSpawnsDisposed+" > numSpawns ("+numSpawns+")");
		// if not enabled then exit
		if(!isEnabled)
			return;
		// if spawned at least one thing and not a multi-spawner then exit
		if(numSpawns > 0 && spawnMultiCount == 0)
			return;
		// if doing multi-group spawns, and if all groups have been spawned, then exit
		if(spawnMultiCount > 0 && spawnMultiGrpCount > 0 && numSpawns == spawnMultiCount * spawnMultiGrpCount)
			return;

		boolean doSpawn = false;
		if(isRespawnDead) {
			// if all spawns have been disposed then do respawn
			if(numSpawns == numSpawnsDisposed)
				doSpawn = true;
		}
		// if doing multi-spawn, or multi-spawn groups...
		else if(spawnMultiCount > 0) {
			// if doing first spawn then don't wait
			if(numSpawns == 0)
				doSpawn = true;
			// If doing second, third, fourth, etc. spawns then wait between spawns - 
			// If wait time has elapsed...
			else if(spawnTimer > spawnMultiRate) {
				// if doing multiple spawn groups...
				if(spawnMultiGrpCount > 0) {
					// how many individuals have been spawned within current group?
					int numSpawnsCurrentGrp = Math.floorMod(numSpawns, spawnMultiCount);
					// If full group has been spawned then wait for last member of group to be disposed before
					// spawning next Agent.
					if(numSpawnsCurrentGrp == 0) {
						if(numSpawns == numSpawnsDisposed)
							doSpawn = true;
					}
					// if less than the full group has been spawned, then spawn another individual Agent
					else if(numSpawnsCurrentGrp < spawnMultiCount)
						doSpawn = true;
				}
				// Not doing multiple spawn groups; if less than the full group has been spawned, then spawn
				// another individual Agent.
				else if(numSpawns < spawnMultiCount)
					doSpawn = true;
			}
		}

		if(doSpawn) {
			numSpawns++;
			spawnTimer = 0f;
			Agent spawnedAgent = agency.createAgent(Agent.createPointAP(spawnAgentClassAlias, sbody.getPosition()));
			agency.addAgentRemoveListener(new AgentRemoveListener(this, spawnedAgent) {
				@Override
				public void removedAgent() { numSpawnsDisposed++; }
			});
		}

		spawnTimer += delta;
	}

	@Override
	public void onTakeEnable(boolean enabled) {
		this.isEnabled = enabled;
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
