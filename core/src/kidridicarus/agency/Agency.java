package kidridicarus.agency;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.AgencyIndex.AgentIter;
import kidridicarus.agency.AgencyIndex.DrawObjectIter;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDef;
import kidridicarus.agency.agent.general.AgentSpawnTrigger;
import kidridicarus.agency.agent.general.AgentSpawner;
import kidridicarus.agency.agent.general.DespawnBox;
import kidridicarus.agency.agent.general.GuideSpawner;
import kidridicarus.agency.agent.general.Room;
import kidridicarus.agency.agent.general.WarpPipe;
import kidridicarus.agency.change.AgencyChangeQueue;
import kidridicarus.agency.change.AgentPlaceholder;
import kidridicarus.agency.change.DrawOrderChange;
import kidridicarus.agency.change.TileChange;
import kidridicarus.agency.change.UpdateChange;
import kidridicarus.agency.change.AgencyChangeQueue.AgencyChangeCallback;
import kidridicarus.agency.collisionmap.TileCollisionMap;
import kidridicarus.agency.change.AgentListChange;
import kidridicarus.agency.contact.AgencyContactFilter;
import kidridicarus.agency.contact.AgencyContactListener;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.DrawOrder;

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
	private static final AgentClassList CORE_AGENT_CLASS_LIST = new AgentClassList( 
			AgencyKV.Spawn.VAL_AGENTSPAWNER, AgentSpawner.class,
			AgencyKV.Spawn.VAL_AGENTSPAWN_TRIGGER, AgentSpawnTrigger.class,
			AgencyKV.Spawn.VAL_DESPAWN, DespawnBox.class,
			AgencyKV.Spawn.VAL_PIPEWARP, WarpPipe.class,
			AgencyKV.Spawn.VAL_SPAWNGUIDE, GuideSpawner.class,
			AgencyKV.Room.VAL_ROOM, Room.class);

	private World world;
	private TileCollisionMap collisionMap;
	private AgencyChangeQueue agencyChangeQ;
	private AgencyIndex agencyIndex;
	private TextureAtlas atlas;
	private float globalTimer;
	private AgencyEventListener agencyEventListener;
	private AgentClassList allAgentsClassList;

	public Agency(AgentClassList additionalAgents) {
		atlas = null;
		globalTimer = 0f;

		world = new World(new Vector2(0, -10f), true);
		world.setContactListener(new AgencyContactListener());
		world.setContactFilter(new AgencyContactFilter());

		agencyChangeQ = new AgencyChangeQueue();
		agencyIndex = new AgencyIndex();

		allAgentsClassList = new AgentClassList(CORE_AGENT_CLASS_LIST, additionalAgents);
	}

	public void update(float delta) {
		world.step(delta, 6, 2);
		updateAgents(delta);
		globalTimer += delta;
	}

	private void updateAgents(float delta) {
		// loop through list of agents receiving updates, calling each agent's update method
		for(Agent a : agencyIndex.getAgentsToUpdate())
			a.update(delta);
		processChangeQ();
	}

	/*
	 * During the update, agents may have been added/removed from lists:
	 * -list of all agents
	 * -list of agents receiving updates
	 * -agent draw order lists
	 * And tiles may have changed solid status.
	 * Process these queues.
	 */
	public void processChangeQ() {
		agencyChangeQ.process(new AgencyChangeCallback() {
				@Override
				public void change(Object change) {
					if(change instanceof AgentListChange)
						doAgentListChange((AgentListChange) change);
					else if(change instanceof UpdateChange)
						doUpdateChange((UpdateChange) change);
					else if(change instanceof DrawOrderChange)
						doDrawOrderChange((DrawOrderChange) change);
					else if(change instanceof TileChange)
						doTileChange((TileChange) change);
					else {
						throw new IllegalArgumentException(
								"Cannot process agency change; unknown agent change class: " + change);
					}
				}
			});
	}

	private void doAgentListChange(AgentListChange alc) {
		if(alc.add)
			agencyIndex.addAgent(alc.ap.agent);
		else
			agencyIndex.removeAgent(alc.ap.agent);
	}

	private void doUpdateChange(UpdateChange uc) {
		if(uc.enableUpdate)
			agencyIndex.enableAgentUpdate(uc.ap.agent);
		else
			agencyIndex.disableAgentUpdate(uc.ap.agent);
	}

	private void doDrawOrderChange(DrawOrderChange doc) {
		agencyIndex.setAgentDrawOrder(doc.ap.agent, doc.drawOrder);
	}

	private void doTileChange(TileChange change) {
		if(change.solid) {
			if(collisionMap.isTileExist(change.x, change.y)) {
				throw new IllegalStateException(
						"Cannot add solid tile where solid tile already exists in collision map.");
			}
			collisionMap.addTile(change.x, change.y);
		}
		// change from to solid non-solid
		else {
			if(!collisionMap.isTileExist(change.x, change.y)) {
				throw new IllegalStateException(
						"Cannot remove solid tile where solid tile does not already exist in collision map.");
			}
			collisionMap.removeTile(change.x, change.y);
		}
	}

	/*
	 * Use the input layers to find solid (i.e. non-null) tiles and create the collision map.
	 */
	public void createCollisionMap(Collection<TiledMapTileLayer> solidLayers) {
		collisionMap = new TileCollisionMap(world, solidLayers);
	}

	public void setDrawLayers(TreeMap<DrawOrder, LinkedList<TiledMapTileLayer>> drawLayers) {
		agencyIndex.addMapDrawLayers(drawLayers);
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
		String desiredAgentClass = adef.properties.get(AgencyKV.Spawn.KEY_AGENTCLASS, String.class);

		Class<?> agentClass = allAgentsClassList.get(desiredAgentClass);
		if(agentClass == null)
			return null;

		Constructor<?> constructor;
		Agent newlyCreatedAgent = null;
		// when the agent object is constructed, it may invoke calls to enable agent udpates or set agent draw order,
		// so a placeholder must be inserted before creating the object - then the object's reference is put into the
		// placeholder after the object is created.  
		AgentPlaceholder agentPlaceholder = new AgentPlaceholder(null);
		agencyChangeQ.addAgent(agentPlaceholder);
		try {
			constructor = agentClass.getConstructor(new Class[] { Agency.class, AgentDef.class });
			newlyCreatedAgent = (Agent) constructor.newInstance(new Object[] { this, adef });
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Unable to create Agent.");
		}

		agentPlaceholder.agent = newlyCreatedAgent;
		return newlyCreatedAgent;
	}

	public void disposeAgent(Agent agent) {
		agencyChangeQ.removeAgent(new AgentPlaceholder(agent));
	}

	public void enableAgentUpdate(Agent agent) {
		agencyChangeQ.enableAgentUpdate(new AgentPlaceholder(agent));
	}

	public void disableAgentUpdate(Agent agent) {
		agencyChangeQ.disableAgentUpdate(new AgentPlaceholder(agent));
	}

	public void setAgentDrawOrder(Agent agent, DrawOrder order) {
		agencyChangeQ.setAgentDrawOrder(new AgentPlaceholder(agent), order);
	}

	/*
	 * Add item to change queue for change in a tile's "solid" state.
	 */
	public void setPhysicTile(Vector2 t, boolean solid) {
		agencyChangeQ.setPhysicTile((int) t.x, (int) t.y, solid);
	}

	/*
	 * Returns solid status of a tile in collision map (solid = true).
	 * Note: Does not take into account any changes that may be scheduled in the agency change queue.
	 */
	public boolean isMapTileSolid(Vector2 tilePos) {
		return collisionMap.isTileExist((int) tilePos.x, (int) tilePos.y);
	}

	/*
	 * Returns solid status of a point in collision map (solid = true).
	 * Note: Does not take into account any changes that may be scheduled in the agency change queue.
	 */
	public boolean isMapPointSolid(Vector2 pointPos) {
		// convert the point position to a tile position
		Vector2 tilePos = UInfo.getM2PTileForPos(pointPos);
		return collisionMap.isTileExist((int) tilePos.x, (int) tilePos.y);
	}

	public void setAtlas(TextureAtlas atlas) {
		this.atlas = atlas;
	}

	public TextureAtlas getAtlas() {
		return atlas;
	}

	public World getWorld() {
		return world;
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
		agencyIndex.iterateThroughAllAgents(new AgentIter() {
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
		Agent agent = getFirstAgentByProperties(new String[] { AgencyKV.Spawn.KEY_AGENTCLASS,
				AgencyKV.Spawn.KEY_NAME }, new String[] { AgencyKV.Spawn.VAL_SPAWNGUIDE, name });
		if(agent instanceof GuideSpawner)
			return (GuideSpawner) agent;
		return null;
	}

	public void iterateThroughDrawObjects(DrawObjectIter objIter) {
		agencyIndex.iterateThroughDrawObjects(objIter);
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

	/*
	 * Call dispose method of each agent in the all agents list.
	 */
	private void disposeAllAgents() {
		agencyIndex.iterateThroughAllAgents(new AgentIter() {
			@Override
			public boolean iterate(Agent agent) {
				agent.dispose();
				return false;
			}
		});
	}
}
