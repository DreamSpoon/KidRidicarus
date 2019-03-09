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
import kidridicarus.agency.agencychange.AgencyChangeQueue;
import kidridicarus.agency.agencychange.AgentPlaceholder;
import kidridicarus.agency.agencychange.AllAgentListChange;
import kidridicarus.agency.agencychange.DrawOrderChange;
import kidridicarus.agency.agencychange.UpdateOrderChange;
import kidridicarus.agency.agencychange.AgencyChangeQueue.AgencyChangeCallback;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agent.UpdatableAgent;
import kidridicarus.agency.agentcontact.AgentContactFilter;
import kidridicarus.agency.agentcontact.AgentContactListener;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.AllowOrderList.AllowOrderListIter;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.tool.AllowOrder;

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
 * And a Google dictionary definition of "Agency" to complete the picture:
 *   http://www.google.com/search?q=agency+definition
 *   "a business or organization established to provide a particular service, typically one that involves organizing
 *   transactions between two other parties."
 */
public class Agency implements Disposable {
	private AgentClassList allAgentsClassList;
	private AgencyEventListener agencyEventListener;
	private AgencyChangeQueue agencyChangeQ;
	private AgencyIndex agencyIndex;
	private World world;
	private TextureAtlas atlas;
	private float globalTimer;

	public Agency(AgentClassList allAgentsClassList) {
		atlas = null;
		globalTimer = 0f;

		world = new World(new Vector2(0, -10f), true);
		world.setContactListener(new AgentContactListener());
		world.setContactFilter(new AgentContactFilter());

		agencyChangeQ = new AgencyChangeQueue();
		agencyIndex = new AgencyIndex();

		this.allAgentsClassList = allAgentsClassList;
	}

	public void update(float delta) {
		world.step(delta, 6, 2);
		updateAgents(delta);
		globalTimer += delta;
	}

	private void updateAgents(final float delta) {
		// loop through list of agents receiving updates, calling each agent's update method
		agencyIndex.iterateThroughUpdateAgents(new AllowOrderListIter() {
				@Override
				public boolean iterate(Object obj) {
					((UpdatableAgent) obj).update(delta);
					// continue iterating
					return false;
				}
			});
		processChangeQ();
	}

	/*
	 * During the update, agents may have been added/removed from lists:
	 * -list of all agents
	 * -list of agents receiving updates
	 * -agent draw order lists
	 * Process these queues.
	 */
	public void processChangeQ() {
		agencyChangeQ.process(new AgencyChangeCallback() {
				@Override
				public void change(Object change) {
					if(change instanceof AllAgentListChange)
						doAgentListChange((AllAgentListChange) change);
					else if(change instanceof UpdateOrderChange)
						doUpdateChange((UpdateOrderChange) change);
					else if(change instanceof DrawOrderChange)
						doDrawOrderChange((DrawOrderChange) change);
					else {
						throw new IllegalArgumentException(
								"Cannot process agency change; unknown agent change class: " + change);
					}
				}
			});
	}

	private void doAgentListChange(AllAgentListChange alc) {
		if(alc.add)
			agencyIndex.addAgent(alc.ap.agent);
		else
			agencyIndex.removeAgent(alc.ap.agent);
	}

	private void doUpdateChange(UpdateOrderChange uoc) {
		agencyIndex.setAgentUpdateOrder(uoc.ap.agent, uoc.updateOrder);
	}

	private void doDrawOrderChange(DrawOrderChange doc) {
		agencyIndex.setAgentDrawOrder(doc.ap.agent, doc.drawOrder);
	}

	public void setDrawLayers(TreeMap<AllowOrder, LinkedList<TiledMapTileLayer>> drawLayers) {
		agencyIndex.addMapDrawLayers(drawLayers);
	}

	public void createAgents(Collection<ObjectProperties> agentProps) {
		Iterator<ObjectProperties> apIter = agentProps.iterator();
		while(apIter.hasNext()) {
			ObjectProperties aDef = apIter.next();
			createAgent(aDef);
		}
	}

	/*
	 * Create an agent from the given agent properties.
	 * See website:
	 * http://www.avajava.com/tutorials/lessons/how-do-i-create-an-object-via-its-multiparameter-constructor-using-reflection.html
	 */
	public Agent createAgent(ObjectProperties properties) {
		String agentClassAlias = properties.get(AgencyKV.Spawn.KEY_AGENTCLASS, null, String.class);
		if(agentClassAlias == null)
			throw new IllegalArgumentException("'agentclass' key not found in agent definition.");

		Class<?> agentClass = allAgentsClassList.get(agentClassAlias);
		if(agentClass == null)
			return null;

		Agent newlyCreatedAgent = null;
		// when the agent object is constructed, it may invoke calls to enable agent updates or set agent draw order,
		// so a placeholder must be inserted before creating the object - then the object's reference is put into the
		// placeholder after the object is created.  
		AgentPlaceholder agentPlaceholder = new AgentPlaceholder(null);
		agencyChangeQ.addAgent(agentPlaceholder);
		try {
			Constructor<?> constructor =
					agentClass.getConstructor(new Class[] { Agency.class, ObjectProperties.class });
			newlyCreatedAgent = (Agent) constructor.newInstance(new Object[] { this, properties });
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

	public void setAgentUpdateOrder(Agent agent, AllowOrder order) {
		agencyChangeQ.setAgentUpdateOrder(new AgentPlaceholder(agent), order);
	}

	public void setAgentDrawOrder(Agent agent, AllowOrder order) {
		agencyChangeQ.setAgentDrawOrder(new AgentPlaceholder(agent), order);
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

	/*
	 * Register, as in add to the list of needed music.
	 */
	public void registerMusic(String musicName) {
		if(agencyEventListener != null)
			agencyEventListener.onRegisterMusic(musicName);
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
	private Collection<Agent> getAgentsByPropertiesInt(final String[] keys, final Object[] vals,
			final boolean firstOnly) {
		final LinkedList<Agent> ret = new LinkedList<Agent>();
		if(keys.length != vals.length)
			throw new IllegalArgumentException("keys[] and vals[] arrays are not of equal length.");

		// loop through list of all agents, ignoring agents that have any wrong key/value pairs 
		agencyIndex.iterateThroughAllAgents(new AgentIter() {
				@Override
				public boolean iterate(Agent agent) {
					if(agent.containsPropertyKV(keys, vals)) {
						ret.add(agent);
						return firstOnly;
					}

					return false;
				}
			});
		return ret;
	}

	public void iterateThroughDrawObjects(AllowOrderListIter objIter) {
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
		world.dispose();
	}

	/*
	 * Call dispose method of each agent in the all agents list.
	 */
	private void disposeAllAgents() {
		agencyIndex.iterateThroughDisposableAgents(new AgentIter() {
			@Override
			public boolean iterate(Agent agent) {
				((DisposableAgent) agent).disposeAgent();
				return false;
			}
		});
	}
}
