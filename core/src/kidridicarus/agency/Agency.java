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
import kidridicarus.agency.helper.AgentChangeQueue;
import kidridicarus.agency.helper.AgentIndex;
import kidridicarus.agency.helper.AgentPlaceholder;
import kidridicarus.agency.helper.SolidTileChangeQueue;
import kidridicarus.agency.helper.AgentChangeQueue.AgentChange;
import kidridicarus.agency.helper.AgentChangeQueue.AgentChangeCallback;
import kidridicarus.agency.helper.AgentIndex.AgentIter;
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

/*
 * Desc:
 *   Agency contains Agents and organizes interactions between Agents (Agent-to-Agent), and between Agents and
 *   Agency (Agent-to-Agency, and Agency-to-Agent).
 * Wikipedia definitions for some context:
 *   "Agency - Psychology"
 *     https://en.wikipedia.org/wiki/Agency_(psychology)
 *     "In psychology, agents are goal-directed entities that are able to monitor their environment to select and
 *     perform efficient means-ends actions that are available in a given situation to achieve an intended goal."
 *   "Structure and agency"
 *     https://en.wikipedia.org/wiki/Structure_and_agency
 *     "Agency is the capacity of individuals to act independently and to make their own free choices."
 *   "Government Agency"
 *     https://en.wikipedia.org/wiki/Government_agency
 *     "A government or state agency, sometimes an appointed commission, is a permanent or semi-permanent organization
 *     in the machinery of government that is responsible for the oversight and administration of specific functions,
 *     such as an intelligence agency."
 * "Agency" Google dictionary definition to complete the picture:
 *   http://www.google.com/search?q=agency+definition
 *   "a business or organization established to provide a particular service, typically one that involves organizing
 *   transactions between two other parties."
 */
public class Agency implements Disposable {
	private World world;
	private TileCollisionMap collisionMap;
	private SolidTileChangeQueue tileChangeQ;
	private AgentChangeQueue agentChangeQ;
	private AgentIndex agentIndex;
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

		agentChangeQ = new AgentChangeQueue();
		agentIndex = new AgentIndex();
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

	public void disposeAgent(Agent agent) {
		agentChangeQ.removeAgent(new AgentPlaceholder(agent));
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
		// when the agent object is constructed, it may invoke calls to enable agent udpates or set agent draw order,
		// so a placeholder must be inserted before creating the object - then the object's reference is put into the
		// placeholder after the object is created.  
		AgentPlaceholder agentPlaceholder = new AgentPlaceholder(null);
		agentChangeQ.addAgent(agentPlaceholder);
		try {
			constructor = agentClass.getConstructor(new Class[] { Agency.class, AgentDef.class });
			newlyCreatedAgent = (Agent) constructor.newInstance(new Object[] { this, adef });
		}
		catch (Exception e) {
			throw new IllegalStateException("Unable to create Agent.");
		}

		agentPlaceholder.agent = newlyCreatedAgent;
		return newlyCreatedAgent;
	}

	private void updateAgents(float delta) {
		// loop through list of agents receiving updates, calling each agent's update method
		for(Agent a : agentIndex.getUpdateList())
			a.update(delta);
		// During the update, agents may have been added/removed from queues:
		//   -agents to add/remove from queues
		//   -update agents
		//   -draw order agents
		// Process these queues.
		processAgentChangeQ();
	}

	/*
	 * Process the queue of changes to perform the following:
	 *   -add an agent to list of all agents
	 *   -remove an agent from list of all agents
	 *   -enable updates of an agent
	 *   -disable updates of an agent
	 *   -set the draw order of an agent
	 */
	public void processAgentChangeQ() {
		agentChangeQ.process(new AgentChangeCallback() {
				@Override
				public void change(AgentChange ac) {
					if(ac.addAgent != null) {
						if(ac.addAgent == true)
							agentIndex.addAgent(ac.ap.agent);
						else
							agentIndex.removeAgent(ac.ap.agent);
					}
					if(ac.enableUpdate != null) {
						if(ac.enableUpdate == true)
							agentIndex.enableAgentUpdate(ac.ap.agent);
						else
							agentIndex.disableAgentUpdate(ac.ap.agent);
					}
					if(ac.drawOrder != null)
						agentIndex.setAgentDrawOrder(ac.ap.agent, ac.drawOrder);
				}
			});
	}

	/*
	 * Add a collision map tile change to the tile change queue.
	 */
	public void setPhysicTile(Vector2 t, boolean solid) {
		tileChangeQ.add((int) t.x, (int) t.y, solid);
	}

	/*
	 * Returns solid status of a tile in collision map (solid = true).
	 */
	public boolean isMapTileSolid(Vector2 tilePos) {
		return collisionMap.isTileExist((int) tilePos.x, (int) tilePos.y);
	}

	public void enableAgentUpdate(Agent agent) {
		agentChangeQ.enableAgentUpdate(new AgentPlaceholder(agent));
	}

	public void disableAgentUpdate(Agent agent) {
		agentChangeQ.disableAgentUpdate(new AgentPlaceholder(agent));
	}

	public void setAgentDrawOrder(Agent agent, SpriteDrawOrder order) {
		agentChangeQ.setAgentDrawOrder(new AgentPlaceholder(agent), order);
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
		return agentIndex.getAgentsToDraw();
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
	private Collection<Agent> getAgentsByPropertiesInt(final String[] keys, final String[] vals,
			final boolean firstOnly) {
		final LinkedList<Agent> ret = new LinkedList<Agent>();
		if(keys.length != vals.length)
			throw new IllegalArgumentException("keys[] and vals[] arrays are not of equal length.");

		// loop through list of all agents, ignoring agents that have any wrong key/value pairs 
		agentIndex.iterateThroughAllAgents(new AgentIter() {
				@Override
				public boolean iterate(Agent agent) {
					boolean ignore = false;
					for(int i=0; i<keys.length; i++) {
						// If the key is not found, or the value doesn't match then ignore this agent (if the value 
						// to match is null then don't check value).
						if(!agent.getProperties().containsKey(keys[i]) ||
								(vals[i] != null && !agent.getProperties().get(keys[i], String.class).equals(vals[i]))) {
							ignore = true;
							break;
						}
					}
					// continue iterating if the right agent was *almost* found
					if(ignore)
						return false;
					// this agent had all the right keys and values, so return it
					ret.add(agent);
					// return only first agent found?
					return firstOnly;
				}
			});
		return ret;
	}

	/*
	 * Returns null if guide spawner is not found.
	 */
	public GuideSpawner getGuideSpawnerByName(String name) {
		Agent agent = getFirstAgentByProperties(new String[] { KVInfo.KEY_AGENTCLASS, KVInfo.KEY_NAME },
				new String[] { KVInfo.VAL_SPAWNGUIDE, name });
		if(agent instanceof GuideSpawner)
			return (GuideSpawner) agent;
		return null;
	}

	/*
	 * Returns null if guide spawner is not found
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
		disposeAllAgents();
		collisionMap.dispose();
		world.dispose();
	}

	private void disposeAllAgents() {
		agentIndex.dispose();
	}
}
