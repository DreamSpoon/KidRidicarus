package kidridicarus.common.agent.agentspawner;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.tool.ObjectProperties;

public class DeadRespawnController extends SpawnController {
	private boolean isSpawnReset;
	private int numSpawns;
	private int numSpawnsDisposed;

	public DeadRespawnController(AgentSpawner spawner, ObjectProperties properties) {
		super(spawner, properties);
		isSpawnReset = true;
		numSpawns = 0;
		numSpawnsDisposed = 0;
	}

	@Override
	public void update(float delta, boolean isEnabled) {
		if(!isEnabled) {
			// if all spawns have died, and the spawner is not enabled, then reset so another spawn can occur 
			if(numSpawns == numSpawnsDisposed)
				isSpawnReset = true;
		}
		else if(isSpawnAllowed()) {
			isSpawnReset = false;
			numSpawns++;
			Agent spawnedAgent = doSpawn();
			spawner.getAgency().addAgentRemoveListener(new AgentRemoveListener(spawner, spawnedAgent) {
				@Override
				public void removedAgent() { numSpawnsDisposed++; }
			});
		}
	}

	private boolean isSpawnAllowed() {
		// if the spawner has been reset and all spawns have been disposed then do respawn
		return isSpawnReset && numSpawns == numSpawnsDisposed; 
	}
}
