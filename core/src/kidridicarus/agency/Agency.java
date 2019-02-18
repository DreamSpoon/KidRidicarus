package kidridicarus.agency;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.contact.WorldContactFilter;
import kidridicarus.agency.contact.WorldContactListener;
import kidridicarus.agency.helper.AgentDrawOrderLists;
import kidridicarus.agency.helper.SolidTileChangeQueue;
import kidridicarus.agent.Agent;
import kidridicarus.agent.Metroid.NPC.Skree;
import kidridicarus.agent.Metroid.NPC.SkreeExp;
import kidridicarus.agent.Metroid.NPC.Zoomer;
import kidridicarus.agent.Metroid.item.MaruMari;
import kidridicarus.agent.Metroid.player.Samus;
import kidridicarus.agent.Metroid.player.SamusShot;
import kidridicarus.agent.SMB.BrickPiece;
import kidridicarus.agent.SMB.BumpTile;
import kidridicarus.agent.SMB.CastleFlag;
import kidridicarus.agent.SMB.Flagpole;
import kidridicarus.agent.SMB.FloatingPoints;
import kidridicarus.agent.SMB.LevelEndTrigger;
import kidridicarus.agent.SMB.WarpPipe;
import kidridicarus.agent.SMB.NPC.Goomba;
import kidridicarus.agent.SMB.NPC.Turtle;
import kidridicarus.agent.SMB.SpinCoin;
import kidridicarus.agent.SMB.item.FireFlower;
import kidridicarus.agent.SMB.item.Mush1UP;
import kidridicarus.agent.SMB.item.PowerMushroom;
import kidridicarus.agent.SMB.item.PowerStar;
import kidridicarus.agent.SMB.item.StaticCoin;
import kidridicarus.agent.SMB.player.Mario;
import kidridicarus.agent.SMB.player.MarioFireball;
import kidridicarus.agent.general.DespawnBox;
import kidridicarus.agent.general.GuideSpawner;
import kidridicarus.agent.general.AgentSpawnTrigger;
import kidridicarus.agent.general.AgentSpawner;
import kidridicarus.agent.general.Room;
import kidridicarus.collisionmap.TileCollisionMap;
import kidridicarus.info.KVInfo;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.tool.BlockingQueueList;
import kidridicarus.tool.BlockingQueueList.AddRemCallback;

public class Agency implements Disposable {
	private World world;
	private TileCollisionMap collisionMap;
	private SolidTileChangeQueue tileChangeQ;
	private BlockingQueueList<Agent> agents;
	private BlockingQueueList<Agent> updateAgents;
	private AgentDrawOrderLists agentDrawOrderLists;
	private TextureAtlas atlas;
	private float globalTimer;
	private AgencyEventListener agencyEventListener;

	private Object[] agentClassList = new Object[] {
			KVInfo.VAL_BUMPTILE, BumpTile.class,
			KVInfo.VAL_GOOMBA, Goomba.class,
			KVInfo.VAL_TURTLE, Turtle.class,
			KVInfo.VAL_COIN, StaticCoin.class,
			KVInfo.VAL_AGENTSPAWNER, AgentSpawner.class,
			KVInfo.VAL_SPAWNGUIDE, GuideSpawner.class,
			KVInfo.VAL_ROOM, Room.class,
			KVInfo.VAL_FLAGPOLE, Flagpole.class,
			KVInfo.VAL_LEVELEND_TRIGGER, LevelEndTrigger.class,
			KVInfo.VAL_PIPEWARP, WarpPipe.class,
			KVInfo.VAL_DESPAWN, DespawnBox.class,
			KVInfo.VAL_AGENTSPAWN_TRIGGER, AgentSpawnTrigger.class,
			KVInfo.VAL_CASTLEFLAG, CastleFlag.class,
			KVInfo.VAL_MUSHROOM, PowerMushroom.class,
			KVInfo.VAL_FIREFLOWER, FireFlower.class,
			KVInfo.VAL_MUSH1UP, Mush1UP.class,
			KVInfo.VAL_POWERSTAR, PowerStar.class,
			KVInfo.VAL_BRICKPIECE, BrickPiece.class,
			KVInfo.VAL_SPINCOIN, SpinCoin.class,
			KVInfo.VAL_MARIOFIREBALL, MarioFireball.class,
			KVInfo.VAL_FLOATINGPOINTS, FloatingPoints.class,
			KVInfo.VAL_MARIO, Mario.class,
			KVInfo.VAL_ZOOMER, Zoomer.class,
			KVInfo.VAL_SKREE, Skree.class,
			KVInfo.VAL_SKREE_EXP, SkreeExp.class,
			KVInfo.VAL_MARUMARI, MaruMari.class,
			KVInfo.VAL_SAMUS, Samus.class,
			KVInfo.VAL_SAMUS_SHOT, SamusShot.class
		};

	public Agency() {
		atlas = null;
		globalTimer = 0f;
		tileChangeQ = new SolidTileChangeQueue();

		updateAgents = new BlockingQueueList<Agent>();
		agentDrawOrderLists = new AgentDrawOrderLists();
		agents = new BlockingQueueList<Agent>(new AddRemCallback<Agent>() {
				@Override
				public void add(Agent agent) {}
				@Override
				public void remove(Agent agent) {
					updateAgents.remove(agent);
					agentDrawOrderLists.remove(agent);
					agent.dispose();
				}
			});

		world = new World(new Vector2(0, -10f), true);
		world.setContactListener(new WorldContactListener());
		world.setContactFilter(new WorldContactFilter());
	}

	public void setAtlas(TextureAtlas atlas) {
		this.atlas = atlas;
	}

	/*
	 * Use the input layers to find solid (i.e. non-null) tiles and create the collision map.
	 */
	public void createCollisionMap(Collection<TiledMapTileLayer> solidLayers) {
		collisionMap = new TileCollisionMap(world, solidLayers);
	}

	public void update(float delta) {
		world.step(delta, 6, 2);
		updateAgents(delta);
		// agent updates may have modified collision map, so process the changes
		tileChangeQ.processUpdates(collisionMap);
		globalTimer += delta;
	}

	/*
	 * TODO: If the World.step method is NOT running at present then dispose the agent immediately, otherwise
	 * add it to the queue to be disposed later - apply this same concept to other queues if possible.
	 */
	public void disposeAgent(Agent agent) {
		agents.remove(agent);
	}

	public void createAgents(Collection<AgentDef> agentDefs) {
		Iterator<AgentDef> adIter = agentDefs.iterator();
		while(adIter.hasNext()) {
			AgentDef aDef = adIter.next();
			createAgent(aDef);
		}
	}

	/*
	 * Create an agent based on the adef.
	 * See website:
	 * http://www.avajava.com/tutorials/lessons/how-do-i-create-an-object-via-its-multiparameter-constructor-using-reflection.html
	 */
	public Agent createAgent(AgentDef adef) {
		String desiredAgentClass = adef.properties.get(KVInfo.KEY_AGENTCLASS, String.class);

		Class<?> agentClass = null;
		for(int i=0; i<agentClassList.length; i+=2) {
			String agentClassName = (String) agentClassList[i+0];
			if(agentClassName.equals(desiredAgentClass)) {
				agentClass = (Class<?>) agentClassList[i+1];
				break;
			}
		}
		if(agentClass == null) {
			return null;
		}

		Constructor<?> constructor;
		Agent newlyCreatedAgent = null;
		try {
			constructor = agentClass.getConstructor(new Class[] { Agency.class, AgentDef.class });
			newlyCreatedAgent = (Agent) constructor.newInstance(new Object[] { this, adef });
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		agents.add(newlyCreatedAgent);
		return newlyCreatedAgent;
	}

	public void updateAgents(float delta) {
		// loop through list of agents receiving updates, calling each agent's update method
		for(Agent a : updateAgents.getList())
			a.update(delta);
		// During the update, agents may have been added/removed from queues:
		//   -agents to add/remove from queues
		//   -update agents
		//   -draw order agents
		// The queues must be processed in the correct order, to prevent errors/unknown behavior.
		// Here is my attempt to get the order of operations right:
		//   After all agents have had their update method called,
		//     1) Process the "all agents list" add/removes. Since any agents that were destroyed will not be
		//        affected by update enable/disable or draw order change.
		//     2) Process the update enable/disable queue and draw order change queue, in any order.
		agents.processQ();
		updateAgents.processQ();
		agentDrawOrderLists.process();
	}

	public void setPhysicTile(Vector2 t, boolean solid) {
		tileChangeQ.add((int) t.x, (int) t.y, solid);
	}

	public boolean isMapTileSolid(Vector2 tilePos) {
		return collisionMap.isTileExist((int) tilePos.x, (int) tilePos.y);
	}

	public void enableAgentUpdate(Agent agent) {
		updateAgents.add(agent);
	}

	public void disableAgentUpdate(Agent agent) {
		updateAgents.remove(agent);
	}

	public void setAgentDrawLayer(Agent agent, SpriteDrawOrder layer) {
		agentDrawOrderLists.setAgentDrawOrder(agent, layer);
	}

	public World getWorld() {
		return world;
	}

	public TileCollisionMap getCollisionMap() {
		return collisionMap;
	}

	/*
	 * WorldRenderer will call this method to get and render the drawable agents.
	 */
	public Collection<Agent>[] getAgentsToDraw() {
		return agentDrawOrderLists.getAgentsToDraw();
	}

	public TextureAtlas getAtlas() {
		return atlas;
	}

	public void setEventListener(AgencyEventListener listener) {
		agencyEventListener = listener;
	}

	public void playSound(String soundName) {
		if(agencyEventListener != null)
			agencyEventListener.onPlaySound(soundName);
	}

	public void changeAndStartMusic(String musicName) {
		if(agencyEventListener != null)
			agencyEventListener.onChangeAndStartMusic(musicName);
	}

	public void startMusic() {
		if(agencyEventListener != null)
			agencyEventListener.onStartMusic();
	}

	public void stopMusic() {
		if(agencyEventListener != null)
			agencyEventListener.onStopMusic();
	}

	public void startSinglePlayMusic(String musicName) {
		if(agencyEventListener != null)
			agencyEventListener.onStartSinglePlayMusic(musicName);
	}

	/*
	 * Find agents in list which contain map properties equal to the given keys/vals data.
	 * Note: A value in the vals[] array can be set to null if the value of it's key is to be ignored (in this case,
	 * only the key need exist in the agent's properties - the value for the key is irrelevant).
	 */
	public Collection<Agent> getAgentsByProperties(String[] keys, String[] vals) {
		return getAgentsByPropertiesInt(keys, vals, false);
	}

	/*
	 * Same as gettAgentsByProperties, but returns only the first agent (if any) found.
	 */
	public Agent getFirstAgentByProperties(String[] keys, String[] vals) {
		Collection<Agent> r = getAgentsByPropertiesInt(keys, vals, true);
		if(r.iterator().hasNext())
			return r.iterator().next(); 
		return null;
	}

	/*
	 * Never returns null. If no agent(s) are found, returns an empty collection.
	 */
	private Collection<Agent> getAgentsByPropertiesInt(String[] keys, String[] vals, boolean firstOnly) {
		LinkedList<Agent> ret = new LinkedList<Agent>();

		if(keys.length != vals.length)
			throw new IllegalArgumentException("keys[] and vals[] arrays are not of equal length.");

		// loop through list of all agents, ignoring agents that have any wrong key/value pairs 
		for(Agent a : agents.getList()) {
			boolean ignore = false;
			for(int i=0; i<keys.length; i++) {
				// If the key is not found, or the value doesn't match then ignore this agent (if the value 
				// to match is null then don't check value).
				if(!a.getProperties().containsKey(keys[i]) ||
						(vals[i] != null && !a.getProperties().get(keys[i], String.class).equals(vals[i]))) {
					ignore = true;
					break;
				}
			}
			if(ignore)
				continue;

			// this agent had all the right keys and values, so return it
			ret.add(a);
			// return only first agent found?
			if(firstOnly)
				return ret;
		}
		return ret;
	}

	/*
	 * Returns null if guide spawner not found
	 */
	public GuideSpawner getGuideSpawnerByName(String name) {
		Agent agent = getFirstAgentByProperties(new String[] { KVInfo.KEY_AGENTCLASS, KVInfo.KEY_NAME },
				new String[] { KVInfo.VAL_SPAWNGUIDE, name });
		if(agent instanceof GuideSpawner)
			return (GuideSpawner) agent;
		return null;
	}

	/*
	 * Returns null if guide spawner not found
	 */
	public GuideSpawner getGuideMainSpawner() {
		Agent agent = getFirstAgentByProperties(
				new String[] { KVInfo.KEY_AGENTCLASS, KVInfo.KEY_SPAWNMAIN },
				new String[] { KVInfo.VAL_SPAWNGUIDE, null });
		if(agent instanceof GuideSpawner)
			return (GuideSpawner) agent;
		return null;
	}

	/*
	 * How much time has passed since this agency was constructed?
	 */
	public float getGlobalTimer() {
		return globalTimer;
	}

	@Override
	public void dispose() {
		collisionMap.dispose();
		world.dispose();
	}
}
