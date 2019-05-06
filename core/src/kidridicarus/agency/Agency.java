package kidridicarus.agency;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
import kidridicarus.agency.agentbody.AgentContactFilter;
import kidridicarus.agency.agentbody.AgentContactListener;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.AllowOrder;
import kidridicarus.agency.tool.AllowOrderList.AllowOrderListIter;
import kidridicarus.agency.tool.Ear;
import kidridicarus.agency.tool.EarPlug;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.QQ;

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
 *
 * Notes regarding Box2D World and Agency:
 *   In contrast with Box2D's World.step method, Agency has an Agency.update and Agency.draw method. These
 *   two methods could be combined into a single "step" method, but this option would reduce flexibility.
 *   Reasons for keeping update and draw methods separate:
 *     1) Multiple updates can be run before a draw is called, and
 *     2) Multiple draws can be called between updates.
 *   Why do it this way?
 *     1) Map loading may require running multiple updates to completely load map and sub-Agents - do not draw
 *        during this period in order to avoid showing "error" visuals.
 *     2) "Forced framerate" may require drawing of Agency multiple times between updates
 *        (e.g. 1 update per 3 draw frames to simulate 20 fps - update method is called 20 times per second
 *        while draw method is called 60 times per second).
 */
public class Agency implements Disposable {
	private AgentClassList allAgentsClassList;
	private AgencyChangeQueue agencyChangeQ;
	private AgencyIndex agencyIndex;
	private TextureAtlas atlas;
	private World world;
	// Agency needs an earplug because it looks cool... and lets Agents exchange audio info
	private EarPlug earplug;
	private Eye myEye;
	// how much time has passed (via updates) since this Agency was constructed?
	private float globalTimer;

	public Agency(AgentClassList allAgentsClassList, TextureAtlas atlas) {
		this.allAgentsClassList = allAgentsClassList;
		this.atlas = atlas;

		globalTimer = 0f;
		earplug = new EarPlug();
		myEye = null;

		world = new World(new Vector2(0, -10f), true);
		world.setContactListener(new AgentContactListener());
		world.setContactFilter(new AgentContactFilter());

		agencyChangeQ = new AgencyChangeQueue();
		agencyIndex = new AgencyIndex();
	}

	/*
	 * Process all AgencyIndex change requests that occurred during Agency update.
	 * Processing of changes to AgencyIndex is delayed until end of the update, in order to maintain as consistent a
	 * state as possible during the update. Thus every Agent's state is kept as independent as possible from every
	 * other Agent's state. Agents can still interact, of course, but their interactions are self-governed by way of
	 * the update queue (and their requested update order).
	 * This paradigm is intended to prevent undefined/unexpected results from chaotic/random updating of Agents.
	 * If the coder desires, a chaotic/random order of updates can still be achieved by setting update order to
	 * random values. Controlled chaos, so to speak...
	 * An Agent can be created in two independent situations:
	 *   1) During Agency update. Typically this occurs when an NPC spawner creates an NPC Agent.
	 *   2) Not during Agency update. Typically this occurs when the main game class creates the initial Agents
	 *      like map, NPC spawners, etc.
	 *
	 * 1) Agent created during Agency update
	 *   To implements this scheme, the Agent (and it's sub-components, like AgentSprite) use this paradigm:
	 *     A) Agent Constructor
	 *        -Agent is given startup info from Agency such as it's position, etc.
	 *        -treat this as the first "update" of the Agent, and set the Agent sprite's position, texture, etc.
	 *        -if the Agent creates an updateListener, then the updateListener will not be called until the next
	 *         Agency.update is run, even if the updateListener is set for "later" then "now" in the Agency update.
	 *           e.g. if Agency.update is calling update listeners at update order = 0.5, and the Agent being
	 *           updated creates a new updateListener with update order = 0.7, then the new updateListener is NOT
	 *           called until after the Agency finishes the current update, and the next Agency.update is run.
	 *        -if the Agent creates a new drawListener, then it will be added to the Agency's list of
	 *         drawListeners at the end of the update, and the new drawListener will be run when the next
	 *         Agency.draw is run
	 *     B) Agent update (via updateListener)
	 *       -process changes that occurred during update frame - e.g. body contacts, changes in position
	 *       -update Agent sprite (if necessary) using processed information from previous step
	 *       -this is the only opportunity to modify states, the Agent must NOT modify state during Agency.draw
	 *     C) Agent draw (via drawListener)
	 *       -draw the sprite
	 *       -update the camera - the code probably should not allow for this, but it does...
	 *         -any Agent CAN set the current position/viewing direction of the camera but,
	 *         -only one Agent SHOULD set these values
	 *
	 * work in progress:
	 * 2) Agent created not during update (e.g. game creates initial Agents)
	 *   Agency agency = new Agency();	// Agency is constructed (zero Agents in Agency)
	 *   // Agency update method is run (no Agents to update, so nothing is updated)
	 *   agency.update();	// update nothing
	 *   // Agency draw method is run (still, zero Agents in Agency - so nothing to draw yet!)
	 *   agency.draw();		// draw nothing
	 *   // Agent constructor is run, but Agent is technically not in Agency until first Agency.update is run
	 *   Agent agentX = agency.createAgent(propertiesX);
	 *   // agentX is in the changeQ of agency, but will not be drawn if agency.draw is invoked
	 *   agency.draw();		// draw nothing
	 *   //
	 *   agency.update();	// agentX is added to Agency, but it's update method is NOT called
	 *   // At this point, agentX is in the list of all Agents, so it can be drawn (and also found by way of
	 *   // the method getAgentByProperties. It did not receive a call to its update method during Agency.update
	 *   // because the update listener was only in the queue to be added to Agency, and not in the list of "active"
	 *   // update listeners.
	 *   agency.draw();	// draw agentX
	 *   agency.update();	// agentX's update listener is called (for the first time!)
	 *   agency.draw();	// draw agentX
	 *   agency.update();	// agentX's update listener is called (second time...)
	 *	 ...
	 *
	 * work in progress:
	 * Increment globalTimer before or after running each Agent's update method?
	 * Note: Sprite animation timers are updated "before" calling setRegion because of this order of operations:
	 *   Sprite is constructed, and can set it's position/size/texture at constructor time.
	 *   Sprite is drawn.
	 *   Sprite is updated.
	 *   Sprite is drawn.
	 *   Sprite is updated.
	 *   Sprite is ...
	 *   ...
	 * Sprite is drawn for the first time BEFORE its update method is run, therefore its first update should
	 * increment the animation timer BEFORE setting the texture region using the animation timer.
	 */
	public void update(final float timeDelta) {
		world.step(timeDelta, 6, 2);
		globalTimer += timeDelta;

		// loop through list of agents receiving updates, calling each agent's update method
		agencyIndex.iterateThroughUpdateAgents(new AllowOrderListIter() {
				@Override
				public boolean iterate(Object obj) {
					if(obj instanceof AgentUpdateListener)
						((AgentUpdateListener) obj).update(new FrameTime(timeDelta, globalTimer));
					// continue iterating
					return false;
				}
			});
		// apply changes
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
	public List<Agent> createAgents(Collection<ObjectProperties> agentProps) {
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
		String agentClassAlias = properties.get(AgencyKV.KEY_AGENT_CLASS, null, String.class);
		if(agentClassAlias == null)
			throw new IllegalArgumentException(AgencyKV.KEY_AGENT_CLASS + " key not found in agent definition.");

		Class<?> agentClass = allAgentsClassList.get(agentClassAlias);
		if(agentClass == null)
			return null;

		Agent newlyCreatedAgent = null;
		// When the agent object is constructed, it may invoke calls to enable agent updates or set agent draw order,
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

	public Ear getEar() {
		return earplug.getEar();
	}

	public void setEar(Ear ear) {
		this.earplug.setEar(ear);
	}

	public void setEye(Eye eye) {
		this.myEye = eye;
	}

	public Eye getEye() {
		return myEye;
	}

	public void draw() {
		if(myEye == null)
			return;
		myEye.begin();
		agencyIndex.iterateThroughDrawListeners(new AllowOrderListIter() {
			@Override
			public boolean iterate(Object obj) {
				if(obj instanceof AgentDrawListener)
					((AgentDrawListener) obj).draw(myEye);
				else
					QQ.pr("unknown object in draw list iteration object: " + obj);
				// return false to continue iterating
				return false;
			}
		});
		myEye.end();
	}

	public boolean isValidAgentClassAlias(String agentClassAlias) {
		return allAgentsClassList.get(agentClassAlias) != null;
	}

	public World getWorld() {
		return world;
	}

	public TextureAtlas getAtlas() {
		return atlas;
	}

	/*
	 * Remove all Agents but do not dispose Agency.
	 */
	public void removeAllAgents() {
		agencyIndex.removeAllAgents();
	}

	/*
	 * Dispose and remove all Agents and dispose Agency.
	 * Note: Agency must not be disposed during a frame.
	 */
	@Override
	public void dispose() {
		removeAllAgents();
		world.dispose();
	}

	/*
	 * Returns null if target is not found.
	 */
	public static Agent getTargetAgent(Agency agency, String targetName) {
		if(targetName == null || targetName.equals(""))
			return null;
		return agency.getFirstAgentByProperties(
				new String[] { CommonKV.Script.KEY_NAME }, new String[] { targetName });
	}

}
