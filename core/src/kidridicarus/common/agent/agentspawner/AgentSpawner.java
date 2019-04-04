package kidridicarus.common.agent.agentspawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.agentspawntrigger.AgentSpawnTrigger;
import kidridicarus.common.agent.optional.EnableTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.metaagent.tiledmap.solidlayer.SolidTiledMapAgent;
import kidridicarus.common.tool.Direction4;

public class AgentSpawner extends Agent implements EnableTakeAgent, DisposableAgent {
	private AgentSpawnerBody body;
	private String spawnAgentClassAlias;

	private boolean isEnabled;
	private boolean isRespawnDead;
	private int spawnMultiCount;
	private int spawnMultiGrpCount;
	private float spawnMultiRate;
	private Direction4 spawnScrollDir;

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
		spawnScrollDir = properties.get(CommonKV.Spawn.KEY_SPAWN_SCROLL_DIR, Direction4.NONE, Direction4.class);

		numSpawns = 0;
		numSpawnsDisposed = 0;
		spawnTimer = 0f;

		body = new AgentSpawnerBody(this, agency.getWorld(), Agent.getStartBounds(properties));
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
	}

	private void doUpdate(float delta) {
		// DEBUG: error state check
		if(numSpawnsDisposed > numSpawns)
			throw new IllegalStateException("numSpawnsDisposed ("+numSpawnsDisposed+" > numSpawns ("+numSpawns+")");
		// If not enabled then exit. If spawned at least one thing and not a multi-spawner then exit.
		// If doing multi-group spawns, and if all groups have been spawned, then exit.
		if(!isEnabled || (numSpawns > 0 && spawnMultiCount == 0) ||
				(spawnMultiCount > 0 && spawnMultiGrpCount > 0 && numSpawns == spawnMultiCount * spawnMultiGrpCount))
			return;

		// if this spawner has a scroll direction property then get scroll spawn position, or exit if unavailable
		Vector2 spawnPos = body.getPosition();
		if(spawnScrollDir != Direction4.NONE) {
			AgentSpawnTrigger spawnTrigger = body.getFirstContactByClass(AgentSpawnTrigger.class);
			if(spawnTrigger == null)
				return;

			if(spawnScrollDir != Direction4.UP)
				throw new IllegalStateException("do more code");

			// AgentSpawnTrigger is contacting, so X value is okay...
			// Check only Y bounds for overlap and where empty tiles available...
			Rectangle spawnerTiles = UInfo.RectangleM2T(body.getBounds());
			Rectangle triggerTiles = UInfo.RectangleM2T(spawnTrigger.getBounds());
			// if top of AgentSpawnTrigger is at least as high as top of AgentSpawner then disallow spawn
			if(triggerTiles.y+triggerTiles.height >= spawnerTiles.y+spawnerTiles.height)
				return;

			int topY = (int) (triggerTiles.y + triggerTiles.height-1);
			int bottomY = (int) spawnerTiles.y;
			Integer topNonSolidY = null;
			// tileX = tile X coordinate of middle of AgentSpawner
			int tileX = (int) (spawnerTiles.x + (spawnerTiles.width-1)/2);
			for(int tileY=topY; tileY >= bottomY;tileY--) {
				if(!isMapTileSolid(new Vector2(tileX, tileY))) {
					topNonSolidY = tileY;
					break;
				}
			}
			if(topNonSolidY != null)
				spawnPos = UInfo.VectorT2M(tileX, topNonSolidY);
		}

		if(isSpawnAllowed())
			doSpawn(spawnPos);

		spawnTimer += delta;
	}

	private boolean isSpawnAllowed() {
		if(isRespawnDead) {
			// if all spawns have been disposed then do respawn
			if(numSpawns == numSpawnsDisposed)
				return true;
		}
		// if doing multi-spawn, or multi-spawn groups...
		else if(spawnMultiCount > 0) {
			if(isMultiSpawnAllowed())
				return true;
		}
		return false;
	}

	private boolean isMultiSpawnAllowed() {
		// if doing first spawn then don't wait
		if(numSpawns == 0)
			return true;
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
						return true;
				}
				// if less than the full group has been spawned, then spawn another individual Agent
				else if(numSpawnsCurrentGrp < spawnMultiCount)
					return true;
			}
			// Not doing multiple spawn groups; if less than the full group has been spawned, then spawn
			// another individual Agent.
			else if(numSpawns < spawnMultiCount)
				return true;
		}
		return false;
	}

	private void doSpawn(Vector2 spawnPos) {
		numSpawns++;
		spawnTimer = 0f;
		Agent spawnedAgent = agency.createAgent(Agent.createPointAP(spawnAgentClassAlias, spawnPos));
		agency.addAgentRemoveListener(new AgentRemoveListener(this, spawnedAgent) {
			@Override
			public void removedAgent() { numSpawnsDisposed++; }
		});
	}

	public boolean isMapTileSolid(Vector2 tileCoords) {
		SolidTiledMapAgent ctMap = body.getFirstContactByClass(SolidTiledMapAgent.class);
		return ctMap == null ? false : ctMap.isMapTileSolid(tileCoords); 
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
