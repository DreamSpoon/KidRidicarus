package kidridicarus.common.agent.agentspawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.agentspawntrigger.AgentSpawnTrigger;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.metaagent.tiledmap.solidlayer.SolidTiledMapAgent;
import kidridicarus.common.tool.Direction4;

public class MultiSpawnController extends SpawnController {
	private AgentSpawnerBody body;
	private int multiCount;
	private int multiGrpCount;
	private float spawnRate;
	private Direction4 scrollDir;

	private int numSpawns;
	private int numSpawnsDisposed;
	private float spawnTimer;

	public MultiSpawnController(AgentSpawner spawner, AgentSpawnerBody body, ObjectProperties properties) {
		super(spawner, properties);
		this.body = body;
		this.multiCount = properties.get(CommonKV.Spawn.KEY_SPAWN_MULTI_COUNT, 1, Integer.class);
		this.multiGrpCount = properties.get(CommonKV.Spawn.KEY_SPAWN_MULTI_GRP_COUNT, 1, Integer.class);
		this.spawnRate = properties.get(CommonKV.Spawn.KEY_SPAWN_MULTI_RATE, 0f, Float.class);
		this.scrollDir = properties.get(CommonKV.Spawn.KEY_SPAWN_SCROLL_DIR, Direction4.NONE, Direction4.class);
		numSpawns = 0;
		numSpawnsDisposed = 0;
		spawnTimer = 0f;
	}

	@Override
	public void update(float delta, boolean isEnabled) {
		// DEBUG: error state check
		if(numSpawnsDisposed > numSpawns)
			throw new IllegalStateException("numSpawnsDisposed ("+numSpawnsDisposed+" > numSpawns ("+numSpawns+")");
		// if not enabled or if all groups have been spawned then exit
		if(!isEnabled || numSpawns == multiCount * multiGrpCount)
			return;

		// if a spawn position exists and spawn is allowed then do spawn
		if(isSpawnAllowed()) {
			// if this spawner has a scroll direction property then get scroll spawn position
			Vector2 scrollSpawnPos = null;
			if(scrollDir != Direction4.NONE)
				scrollSpawnPos = getScrollSpawnPos();
			// if not scrolling, or if scrolling and spawn position is available, then do spawn
			if(scrollDir == Direction4.NONE || scrollSpawnPos != null) {
				numSpawns++;
				spawnTimer = 0f;
				Agent spawnedAgent;
				if(scrollDir == Direction4.NONE)
					spawnedAgent = doSpawn();
				else
					spawnedAgent = doSpawn(scrollSpawnPos);
				// track agent removal for spawn of next group
				spawner.getAgency().addAgentRemoveListener(new AgentRemoveListener(spawner, spawnedAgent) {
						@Override
						public void removedAgent() { numSpawnsDisposed++; }
					});
			}
		}

		spawnTimer += delta;
	}

	private Vector2 getScrollSpawnPos() {
		// get spawn trigger for scroll spawn position calculation, and exit if spawn trigger not found
		AgentSpawnTrigger spawnTrigger = body.getFirstContactByClass(AgentSpawnTrigger.class);
		if(spawnTrigger == null)
			return null;

		// that's all folks!
		if(scrollDir != Direction4.UP)
			throw new IllegalStateException("do more code");

		// AgentSpawnTrigger is contacting, so X value is okay...
		// Check only Y bounds for overlap and where empty tiles available...
		Rectangle spawnerTiles = UInfo.RectangleM2T(body.getBounds());
		Rectangle triggerTiles = UInfo.RectangleM2T(spawnTrigger.getBounds());
		// if top of AgentSpawnTrigger is at least as high as top of AgentSpawner then disallow spawn
		if(triggerTiles.y+triggerTiles.height >= spawnerTiles.y+spawnerTiles.height)
			return null;

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

		// if a "top" tile to use for spawning was not available, then return null to indicate no spawn pos found
		if(topNonSolidY == null)
			return null;
		// otherwise return spawn position
		return UInfo.VectorT2M(tileX, topNonSolidY);
	}

	private boolean isSpawnAllowed() {
		// if doing first spawn then don't wait
		if(numSpawns == 0)
			return true;
		// If doing second, third, fourth, etc. spawns then wait between spawns - 
		// If wait time has elapsed...
		else if(spawnTimer > spawnRate) {
			// if doing multiple spawn groups...
			if(multiGrpCount > 0) {
				// how many individuals have been spawned within current group?
				int numSpawnsCurrentGrp = Math.floorMod(numSpawns, multiCount);
				// If full group has been spawned then wait for last member of group to be disposed before
				// spawning next Agent.
				if(numSpawnsCurrentGrp == 0) {
					if(numSpawns == numSpawnsDisposed)
						return true;
				}
				// if less than the full group has been spawned, then spawn another individual Agent
				else if(numSpawnsCurrentGrp < multiCount)
					return true;
			}
			// Not doing multiple spawn groups; if less than the full group has been spawned, then spawn
			// another individual Agent.
			else if(numSpawns < multiCount)
				return true;
		}
		return false;
	}

	private boolean isMapTileSolid(Vector2 tileCoords) {
		SolidTiledMapAgent ctMap = body.getFirstContactByClass(SolidTiledMapAgent.class);
		return ctMap == null ? false : ctMap.isMapTileSolid(tileCoords); 
	}
}
