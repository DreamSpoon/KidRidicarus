package kidridicarus.agency.tool;

import java.util.HashMap;

import kidridicarus.common.tool.Direction4;

/*
 * HashMap based generic properties list, with String keys.
 * Some conversion may occur when getting properties with certain class return types.
 * e.g. If a property is stored in the HashMap as a String, and the get method is called with class type Float,
 *   then the get method will attempt to convert the String to a Float and return a Float.
 *   TODO implement the visa versa: if property is stored in HashMap as Float, and String return type is requested -
 *   then convert the Float and return the String equivalent.
 */
public class ObjectProperties {
	private HashMap<String, Object> properties;

	public ObjectProperties() {
		properties = new HashMap<String, Object>();
	}

	public ObjectProperties cpy() {
		ObjectProperties op = new ObjectProperties();
		op.properties.putAll(properties);
		return op;
	}

	public void put(String key, Object value) {
		properties.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String key, Object defaultValue, Class<T> cls) {
		if(Integer.class.equals(cls)) {
			Object test = properties.getOrDefault(key, defaultValue);
			if(test == null)
				return null;
			else if(test instanceof Integer)
				return (T) test;
			else if(test instanceof String)
				return (T) Integer.valueOf((String) test);
		}
		// if the requested return type is Float
		else if(Float.class.equals(cls)) {
			Object test = properties.getOrDefault(key, defaultValue);
			if(test == null)
				return null;
			else if(test instanceof Float)
				return (T) test;
			else if(test instanceof String)
				return (T) Float.valueOf((String) test);
		}
		else if(Boolean.class.equals(cls)) {
			Object test = properties.getOrDefault(key, defaultValue);
			if(test == null)
				return null;
			else if(test instanceof Boolean)
				return (T) test;
			else if(test instanceof String)
				return (T) Boolean.valueOf((String) test);
		}
		else if(Direction4.class.equals(cls)) {
			Object test = properties.getOrDefault(key, defaultValue);
			if(test == null)
				return null;
			else if(test instanceof Direction4)
				return (T) test;
			else if(test instanceof String)
				return (T) Direction4.fromString((String) test);
		}
		return (T) properties.getOrDefault(key, defaultValue);
	}

	public boolean containsKey(String key) {
		return properties.containsKey(key);
	}

	public boolean containsKV(String key, Object val) {
		return properties.containsKey(key) && (val == null || properties.get(key).equals(val));
	}

	public boolean containsAllKV(String[] keys, Object[] vals) {
		for(int i=0; i<keys.length; i++) {
			// if the key is not found, or the value doesn't match then return false 
			if(!properties.containsKey(keys[i]) || (vals[i] != null && !properties.get(keys[i]).equals(vals[i])))
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(String key : properties.keySet())
			sb.append("[Key][Val]=[" + key + "][" + properties.get(key) + "]\n");
		return sb.toString();
	}
}
