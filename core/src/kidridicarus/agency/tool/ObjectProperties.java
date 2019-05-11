package kidridicarus.agency.tool;

import java.util.HashMap;
import java.util.Map.Entry;

import kidridicarus.common.tool.Direction4;

/*
 * HashMap based generic properties list, with String keys.
 * Two types of get available:
 *   1) Raw get: returns value (or default value) associated with key - no class conversion.
 *   2) Typed get convenience methods: returns value (or default value) associated with key, and converts if needed
 *      and able (throwing exception if not able to convert).
 */
public class ObjectProperties {
	private HashMap<String, Object> properties;

	public ObjectProperties() {
		properties = new HashMap<String, Object>();
	}

	public void put(String key, Object value) {
		properties.put(key, value);
	}

	public void putAll(ObjectProperties objProps) {
		properties.putAll(objProps.properties);
	}

	/*
	 * See:
	 * https://stackoverflow.com/questions/4584541/check-if-a-class-object-is-subclass-of-another-class-object-in-java
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String key, Object defaultValue, Class<T> cls) {
		// if the property is not in the list then return the default value
		if(!properties.containsKey(key))
			return (T) defaultValue;
		Object returnVal = properties.get(key);
		// safety null check
		if(returnVal == null)
			return null;
		// the class of the property value must be equal to, or a superclass of, cls - if not then throw error
		if(!cls.isAssignableFrom(returnVal.getClass())) {
			throw new IllegalStateException("Cannot get object property because class to get is not assignable "+
					"from class of property found, where key="+key+", class to get="+cls.getName()+
					", class of property found="+returnVal.getClass().getName());
		}
		return (T) returnVal;
	}

	public String getString(String key, String defaultValue) {
		if(!properties.containsKey(key))
			return defaultValue;
		Object test = properties.get(key);
		if(test instanceof String)
			return (String) test;
		throw new IllegalStateException(
				"Cannot get String property for key=("+key+"), because associated value is not a String.");
	}

	public Integer getInteger(String key, Integer defaultValue) {
		if(!properties.containsKey(key))
			return defaultValue;
		Object test = properties.get(key);
		if(test instanceof Integer)
			return (Integer) test;
		else if(test instanceof String)
			return Integer.valueOf((String) test);
		throw new IllegalStateException(
				"Cannot get Integer property for key=("+key+"), because associated value is not an Integer.");
	}

	public Float getFloat(String key, Float defaultValue) {
		if(!properties.containsKey(key))
			return defaultValue;
		Object test = properties.get(key);
		if(test instanceof Float)
			return (Float) test;
		else if(test instanceof String)
			return Float.valueOf((String) test);
		throw new IllegalStateException(
				"Cannot get Float property for key=("+key+"), because associated value is not a Float.");
	}

	public Boolean getBoolean(String key, Boolean defaultValue) {
		if(!properties.containsKey(key))
			return defaultValue;
		Object test = properties.get(key);
		if(test instanceof Boolean)
			return (Boolean) test;
		else if(test instanceof String)
			return Boolean.valueOf((String) test);
		throw new IllegalStateException(
				"Cannot get Boolean property for key=("+key+"), because associated value is not a Boolean.");
	}

	public Direction4 getDirection4(String key, Direction4 defaultValue) {
		if(!properties.containsKey(key))
			return defaultValue;
		Object test = properties.get(key);
		if(test instanceof Direction4)
			return (Direction4) test;
		else if(test instanceof String)
			return Direction4.fromString((String) test);
		throw new IllegalStateException(
				"Cannot get Direction4 property for key=("+key+"), because associated value is not a Direction4.");
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Entry<String, Object> key : properties.entrySet())
			sb.append("[Key][Val]=[" + key.getKey() + "][" + key.getValue() + "]\n");
		return sb.toString();
	}
}
