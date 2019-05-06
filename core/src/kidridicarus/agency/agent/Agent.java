package kidridicarus.agency.agent;

import java.util.HashMap;
import java.util.Map.Entry;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agentproperties.GetPropertyListener;
import kidridicarus.agency.agentproperties.ObjectProperties;

/*
 * TODO: ensure the following is correct:
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
 *    class AgentEye { ... }		// screen center
 *    class AgentEar { ... }		// music changes
 */
public abstract class Agent {
	protected Agency agency;

	/*
	 * Agents keep an internal list of properties that they can share.
	 * Subclasses can override this method via GetPropertyListener so that properties that vary over time,
	 * or custom properties, can be queried efficiently:
	 * Agent properties may change over time and need updating. Instead of updating the entire list of properties
	 * when a single property needs to be queried, just return the one updated property. In this fashion, the
	 * properties list doesn't need to be updated, and the agent can return values without needing to constantly
	 * update the properties list.
	 */
	protected ObjectProperties properties;
	private HashMap<String, GetPropertyListener> getPropertyListeners;

	protected Agent(Agency agency, ObjectProperties properties) {
		this.agency = agency;
		this.properties = properties;
		this.getPropertyListeners = new HashMap<String, GetPropertyListener>();
	}

	public Agency getAgency() {
		return agency;
	}

	protected void addGetPropertyListener(String key, GetPropertyListener gpListener) {
		getPropertyListeners.put(key, gpListener);
	}

	public <T> T getProperty(String key, Object defaultValue, Class<T> cls) {
		GetPropertyListener gpListener = getPropertyListeners.get(key);
		if(gpListener != null)
			return gpListener.getByClass(cls);
		return properties.get(key, defaultValue, cls);
	}

	/*
	 * Returns a newly created ObjectProperties object, with keys and values copied from the Agent's properties.
	 * Note and warning:
	 *   The value of each key/value pair of properties is a copy of the *reference* to the value. If the values
	 * in the properties set are non-primitives (e.g. if a property value is a reference to a LinkedList), then
	 * the code that calls this method can modify internal Agent property information without going through
	 * "the proper channels" (i.e. without calling the Agent's accessor methods).
	 *
	 * TODO:
	 * 1) Create method getCopyAllProperties to create and return a "safe" ObjectProperties object, which can
	 * be manipulated in any way, without affecting the Agent's internal properties information.
	 * Or:
	 * 2) Each Agent must ensure that its ObjectProperties object, and its GetPropertyListeners, return only "safe"
	 * data (i.e. copies of the internal state information instead of direct references).
	 *
	 * Use option 2 in the short term, apply option 1 later.
	 */
	public ObjectProperties getAllProperties() {
		// start with a copy of this Agent's properties list
		ObjectProperties myProperties = properties.cpy();
		// replace the properties that have GetPropertyListeners associated with them
		for(Entry<String, GetPropertyListener> iter : getPropertyListeners.entrySet())
			myProperties.put(iter.getKey(), iter.getValue().get());
		// return the result
		return myProperties;
	}

	/*
	 * Check each of the given key/value pairs against this Agent's properties (both custom and regular) and return
	 * true if all key/value pairs match. Otherwise return false.
	 */
	public boolean containsPropertyKV(String[] keys, Object[] vals) {
		for(int i=0; i<keys.length; i++) {
			// if a listener exists for this key then use the custom listener
			GetPropertyListener gpListener = getPropertyListeners.get(keys[i]);
			if(gpListener != null) {
				// If the given value is null, and the custom get returns non-null, then return false
				// due to mismatch, or
				// if the value returned by the custom get listener does not match given value, then return false
				// due to mismatch.
				if((vals[i] == null && gpListener.get() != null) ||
						!gpListener.getByClass(vals[i].getClass()).equals(vals[i])) {
					return false;
				}
			}
			// Else check against regular properties and, if the key is not found or the value doesn't match,
			// then return false. 
			else if(!properties.containsKey(keys[i]) ||
						(vals[i] != null && !properties.get(keys[i], null, vals[i].getClass()).equals(vals[i]))) {
				return false;
			}
		}
		// no mismatches found
		return true;
	}
}
