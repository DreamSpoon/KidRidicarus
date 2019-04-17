package kidridicarus.agency.agent;

import java.util.HashMap;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agentproperties.GetPropertyListener;
import kidridicarus.agency.agentproperties.ObjectProperties;

/*
 * TODO: ensure the following is correct:
 * Agents have a key-value list of properties that can be queried.
 * The list can be queried for info such as current position, facing direction, initial velocity, etc.
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
			return gpListener.get(cls);
		return properties.get(key, defaultValue, cls);
	}

	public ObjectProperties getCopyAllProperties() {
		// start with a copy of this Agent's properties list
		ObjectProperties myProperties = properties.cpy();
		// replace the properties that have GetPropertyListeners associated with them
		for(String listenerKey : getPropertyListeners.keySet())
			myProperties.put(listenerKey, getPropertyListeners.get(listenerKey));
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
				if((vals[i] == null && gpListener.get(Object.class) != null) ||
						(!gpListener.get(vals[i].getClass()).equals(vals[i]))) {
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
