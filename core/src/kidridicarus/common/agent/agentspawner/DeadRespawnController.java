package kidridicarus.common.agent.agentspawner;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.Agent;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;

class DeadRespawnController extends SpawnController {
	private boolean isSpawnReset;
	private int numSpawns;
	private int numSpawnsDisposed;

	DeadRespawnController(AgentSpawner spawner, AgentHooks parentHooks, ObjectProperties properties) {
		super(spawner, parentHooks, properties);
		isSpawnReset = true;
		numSpawns = 0;
		numSpawnsDisposed = 0;
	}

	@Override
	void update(FrameTime frameTime, boolean isEnabled) {
		if(!isEnabled) {
			// if all spawns have died, and the spawner is not enabled, then reset so another spawn can occur 
			if(numSpawns == numSpawnsDisposed)
				isSpawnReset = true;
		}
		else if(isSpawnAllowed()) {
			isSpawnReset = false;
			numSpawns++;
			Agent spawnedAgent = doSpawn();
			parentHooks.createAgentRemoveListener(spawnedAgent, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { numSpawnsDisposed++; }
			});
		}
	}

	private boolean isSpawnAllowed() {
		// if the spawner has been reset and all spawns have been disposed then do respawn
		return isSpawnReset && numSpawns == numSpawnsDisposed; 
	}
}
