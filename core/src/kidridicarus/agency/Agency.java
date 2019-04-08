package kidridicarus.agency;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.AgencyIndex.AgentIter;
import kidridicarus.agency.agencychange.AgencyChangeQueue;
import kidridicarus.agency.agencychange.AgencyChangeQueue.AgencyChangeCallback;
import kidridicarus.agency.agencychange.AgentDrawListenerChange;
import kidridicarus.agency.agencychange.AgentPlaceholder;
import kidridicarus.agency.agencychange.AgentRemoveListenerChange;
import kidridicarus.agency.agencychange.AgentUpdateListenerChange;
import kidridicarus.agency.agencychange.AllAgentListChange;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agentcontact.AgentContactFilter;
import kidridicarus.agency.agentcontact.AgentContactListener;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.AllowOrder;
import kidridicarus.agency.tool.AllowOrderList.AllowOrderListIter;
import kidridicarus.agency.tool.Ear;
import kidridicarus.agency.tool.EarPlug;
import kidridicarus.agency.tool.ObjectProperties;

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
	private AgencyChangeQueue agencyChangeQ;
	private AgencyIndex agencyIndex;
	private World world;
	private TextureAtlas atlas;
	private float globalTimer;
	// Agency needs an earplug because it looks cool... and lets Agents exchange audio info
	private EarPlug earplug;

	public Agency(AgentClassList allAgentsClassList) {
		atlas = null;
		globalTimer = 0f;
		earplug = new EarPlug();

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

	public void postUpdate() {
		processChangeQ();
	}

	private void updateAgents(final float delta) {
		// loop through list of agents receiving updates, calling each agent's update method
		agencyIndex.iterateThroughUpdateAgents(new AllowOrderListIter() {
				@Override
				public boolean iterate(Object obj) {
					if(obj instanceof AgentUpdateListener)
						((AgentUpdateListener) obj).update(delta);
					// continue iterating
					return false;
				}
			});
	}

	/*
	 * During the update, agents may have been added/removed from lists:
	 * -list of all agents
	 * -list of agents receiving updates
	 * -agent draw order lists
	 * Process these queues.
	 */
	private void processChangeQ() {
		agencyChangeQ.process(new AgencyChangeCallback() {
				@Override
				public void change(Object change) {
					if(change instanceof AllAgentListChange)
						doAgentListChange((AllAgentListChange) change);
					else if(change instanceof AgentUpdateListenerChange)
						doUpdateListenerChange((AgentUpdateListenerChange) change);
					else if(change instanceof AgentDrawListenerChange)
						doDrawListenerChange((AgentDrawListenerChange) change);
					else if(change instanceof AgentRemoveListenerChange)
						doAgentRemoveListenerChange((AgentRemoveListenerChange) change);
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

	private void doUpdateListenerChange(AgentUpdateListenerChange ulc) {
		if(ulc.add)
			agencyIndex.addUpdateListener(ulc.ap.agent, ulc.updateOrder, ulc.auListener);
		else
			agencyIndex.removeUpdateListener(ulc.ap.agent, ulc.auListener);
	}

	private void doDrawListenerChange(AgentDrawListenerChange dlc) {
		if(dlc.add)
			agencyIndex.addDrawListener(dlc.ap.agent, dlc.drawOrder, dlc.adListener);
		else
			agencyIndex.removeDrawListener(dlc.ap.agent, dlc.adListener);
	}

	private void doAgentRemoveListenerChange(AgentRemoveListenerChange change) {
		if(change.add)
			agencyIndex.addAgentRemoveListener(change.ap.agent, change.arListener);
		else
			agencyIndex.removeAgentRemoveListener(change.ap.agent, change.arListener);
	}

	/*
	 * Create many agents from a collection of agent properties, and return a list of the created Agents.
	 */
	public LinkedList<Agent> createAgents(Collection<ObjectProperties> agentProps) {
		LinkedList<Agent> aList = new LinkedList<Agent>();
		Iterator<ObjectProperties> apIter = agentProps.iterator();
		while(apIter.hasNext())
			aList.add(createAgent(apIter.next()));
		return aList;
	}

	/*
	 * Create an agent from the given agent properties.
	 * See website:
	 * http://www.avajava.com/tutorials/lessons/how-do-i-create-an-object-via-its-multiparameter-constructor-using-reflection.html
	 */
	public Agent createAgent(ObjectProperties properties) {
		String agentClassAlias = properties.get(AgencyKV.Spawn.KEY_AGENT_CLASS, null, String.class);
		if(agentClassAlias == null)
			throw new IllegalArgumentException(AgencyKV.Spawn.KEY_AGENT_CLASS + " key not found in agent definition.");

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

	public void removeAgent(Agent agent) {
		agencyChangeQ.removeAgent(new AgentPlaceholder(agent));
	}

	public void addAgentUpdateListener(Agent agent, AllowOrder updateOrder, AgentUpdateListener auListener) {
		agencyChangeQ.addAgentUpdateListener(new AgentPlaceholder(agent), updateOrder, auListener);
	}

	public void removeAgentUpdateListener(Agent agent, AgentUpdateListener auListener) {
		agencyChangeQ.removeAgentUpdateListener(new AgentPlaceholder(agent), auListener);
	}

	public void addAgentDrawListener(Agent agent, AllowOrder drawOrder, AgentDrawListener adListener) {
		agencyChangeQ.addAgentDrawListener(new AgentPlaceholder(agent), drawOrder, adListener);
	}

	public void removeAgentDrawListener(Agent agent, AgentDrawListener adListener) {
		agencyChangeQ.removeAgentDrawListener(new AgentPlaceholder(agent), adListener);
	}

	public void addAgentRemoveListener(AgentRemoveListener arListener) {
		agencyChangeQ.addAgentRemoveListener(new AgentPlaceholder(arListener.getListeningAgent()), arListener);
	}

	public void removeAgentRemoveListener(AgentRemoveListener arListener) {
		agencyChangeQ.removeAgentRemoveListener(new AgentPlaceholder(arListener.getListeningAgent()), arListener);
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

	public void iterateThroughDrawListeners(AllowOrderListIter doli) {
		agencyIndex.iterateThroughDrawListeners(doli);
	}

	/*
	 * How much time has passed since this Agency was constructed?
	 */
	public float getGlobalTimer() {
		return globalTimer;
	}

	/*
	 * Dispose and remove all Agents but do not dispose Agency.
	 */
	public void disposeAndRemoveAllAgents() {
		agencyIndex.disposeAndRemoveAllAgents();
	}

	public Ear getEar() {
		return earplug.getEar();
	}

	public void setEar(Ear ear) {
		this.earplug.setRealEar(ear);
	}

	/*
	 * Dispose and remove all Agents and dispose Agency.
	 */
	@Override
	public void dispose() {
		disposeAndRemoveAllAgents();
		world.dispose();
	}

	public boolean isValidAgentClassAlias(String agentClassAlias) {
		return allAgentsClassList.get(agentClassAlias) != null;
	}
}
