package kidridicarus.agency.agent;

import java.util.HashMap;

/*
 * Unique keys only, replacements will be made if a non-unique string is given to put.
 */
public class AgentProperties {
	private HashMap<String, Object> properties;

	public AgentProperties() {
		properties = new HashMap<String, Object>();
	}

	public void put(String key, Object value) {
		properties.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String key, Object defaultValue, Class<T> poo) {
		// if the requested return type is Float
		if(Float.class.equals(poo)) {
			Object test = properties.getOrDefault(key, defaultValue);
			if(test == null)
				return null;
			else if(test instanceof Float)
				return (T) test;
			else if(test instanceof String)
				return (T) Float.valueOf((String) test);
		}
		return (T) properties.getOrDefault(key, defaultValue);
	}

	public boolean containsKey(String key) {
		return properties.containsKey(key);
	}

	public boolean containsKV(String key, Object val) {
		if(properties.containsKey(key) && (val == null || properties.get(key).equals(val)))
			return true;
		return false;
	}

	public boolean containsAllKV(String[] keys, Object[] vals) {
		for(int i=0; i<keys.length; i++) {
			// if the key is not found, or the value doesn't match then return false 
			if(!properties.containsKey(keys[i]) || (vals[i] != null && !properties.get(keys[i]).equals(vals[i])))
				return false;
		}
		return true;
	}
}
