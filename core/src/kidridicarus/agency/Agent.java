package kidridicarus.agency;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentPropertyListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.ObjectProperties;

/*
 * Agents have a key-value list of properties that can be queried.
 * The list can be queried for info such as current position, facing direction, initial velocity, etc.
 *
 * TODO
 * Agents will have observable 2x2 perspective: (inward, outward) x (audio, video)
 * where
 * Outward perspective:
 *   Sprite - e.g. Mario fireball sprite in explode animation state
 *   Speaker (sound) - e.g. Goomba playing it's head bopped sound
 * Inward perspective:
 *   Screen center
 *   Music being "heard" - e.g. Mario's Power star Music is heard more loudly than the room music
 *
 * Screen center can be used to determine which Agents will be "observed" for sight output (video, sprites, etc.),
 * and for speaker output (audio, music, etc.).
 * When a "draw" pass is run, it should be run in parts:
 *   1) Find the player with the "inward perspective" that you want to use.
 *   2) Get the screen center from the "inward perspective".
 *   3) Get the music / audio data from "inward perspective" and set music/audio accordingly.
 *   4) Get the sprite/speaker data from Agents within "sight"/"earshot" based on the "inward perspective".
 * How to divide up these parts between the caller and the Agent/Agency is a work in progress.
 *
 * Player Agents will probably have fields for all four classes (working names):
 *    class AgentSprite { ... }		// show visual
 *    class AgentSpeaker { ... }	// play sounds
 *    class AgentEye { ... }		// "see" screen center
 *    class AgentEar { ... }		// "hear" music changes
 */
public abstract class Agent {
	protected final Agency agency;

	List<AgentUpdateListener> updateListeners;
	List<AgentDrawListener> drawListeners;
	HashMap<String, AgentPropertyListener<?>> propertyListeners;
	// the listeners created by this Agent, to listen for removal of other Agents
	List<AgentRemoveListener> myAgentRemoveListeners;
	// the listeners create by other Agents, which are listening for removal of this Agent
	List<AgentRemoveListener> otherAgentRemoveListeners;

	protected Agent(Agency agency, ObjectProperties properties) {
		this.agency = agency;
		updateListeners = new LinkedList<AgentUpdateListener>();
		drawListeners = new LinkedList<AgentDrawListener>();
		propertyListeners = new HashMap<String, AgentPropertyListener<?>>();
		myAgentRemoveListeners = new LinkedList<AgentRemoveListener>();
		otherAgentRemoveListeners = new LinkedList<AgentRemoveListener>();
		// Agent class is set at constructor time and never changes
		final String myAgentClass = properties.getString(AgencyKV.KEY_AGENT_CLASS, null);
		agency.addAgentPropertyListener(this, AgencyKV.KEY_AGENT_CLASS, new AgentPropertyListener<String>(String.class) {
				@Override
				public String getValue() { return myAgentClass; }
			});
	}

	public Agency getAgency() {
		return agency;
	}

	// ignore warning because type safety is maintained by getClass().equals(cls)
	@SuppressWarnings("unchecked")
	public <T> T getProperty(String key, T defaultValue, Class<T> cls) {
		AgentPropertyListener<?> listener = propertyListeners.get(key);
		if(listener == null)
			return defaultValue;
		Object propValue = listener.getValue();
		// safety check for null value, and return null if found
		if(propValue == null)
			return null;
		// if property class doesn't match given class cls, then throw error
		if(!propValue.getClass().equals(cls)) {
			throw new IllegalStateException("Unable to get Agent property=("+key+") because get class=("+
					cls.getName()+") doesn't equal property class=("+propValue.getClass().getName()+") for Agent=("+
					this+") and property value=("+propValue+")");
		}
		return (T) propValue;
	}

	// poll all property listeners and return result via object property list
	public ObjectProperties getAllProperties() {
		ObjectProperties props = new ObjectProperties();
		for(Entry<String, AgentPropertyListener<?>> iter : propertyListeners.entrySet())
			props.put(iter.getKey(), iter.getValue().getValue());
		return props;
	}
}
