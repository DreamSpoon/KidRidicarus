package kidridicarus.agency.agent;

import java.util.HashMap;
import java.util.Iterator;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.GetPropertyListener;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.Direction8;

/*
 * The Agent can interact with other Agents (like Body in Box2D).
 * The Agent can have ContactSensors (equivalent to Fixture in Box2D).
 * The ContactSensors are used to detect when Agent is on ground, when Agents start/end contact, etc.
 * 
 */
public abstract class Agent {
	protected Agency agency;
	protected ObjectProperties properties;
	private HashMap<String, GetPropertyListener> getPropertyListeners;

	// TODO Remove these 2 methods, replace with calls to getProperty with something like "current_position",
	//   or "current_bounds". Thus all queries for agent properties are routed through the getProperty method.
	public abstract Vector2 getPosition();
	public abstract Rectangle getBounds();

	public Agent(Agency agency, ObjectProperties properties) {
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

	/*
	 * Agents keep an internal list of properties that they can share.
	 * Subclasses can override this method so that properties that vary over time can be queried efficiently.
	 * Agent properties may change over time and need updating. Instead of updating the entire list of properties
	 * when a single property needs to be queried, just return the one updated property. In this fashion, the
	 * properties list doesn't need to be updated, the agent can return values without needing to store them in the
	 * properties list.
	 * THINK: dynamic vs static properties?
	 */
	public <T> T getProperty(String key, Object defaultValue, Class<T> cls) {
		GetPropertyListener gpListener = getPropertyListeners.get(key);
		if(gpListener != null)
			return gpListener.get(cls);
		return properties.get(key, defaultValue, cls);
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

	public static ObjectProperties createAP(String agentClassAlias) {
		ObjectProperties ret = new ObjectProperties();
		ret.put(AgencyKV.Spawn.KEY_AGENT_CLASS, agentClassAlias);
		return ret;
	}

	public static ObjectProperties createPointAP(String agentClassAlias, Vector2 position) {
		ObjectProperties ret = new ObjectProperties();
		ret.put(AgencyKV.Spawn.KEY_AGENT_CLASS, agentClassAlias);
		ret.put(AgencyKV.Spawn.KEY_START_POS, position);
		return ret;
	}

	public static ObjectProperties createPointAP(String agentClassAlias, Vector2 position, Vector2 velocity) {
		ObjectProperties ret = new ObjectProperties();
		ret.put(AgencyKV.Spawn.KEY_AGENT_CLASS, agentClassAlias);
		ret.put(AgencyKV.Spawn.KEY_START_POS, position);
		ret.put(AgencyKV.Spawn.KEY_START_VEL, velocity);
		return ret;
	}

	public static ObjectProperties createRectangleAP(String agentClassAlias, Rectangle bounds) {
		ObjectProperties ret = new ObjectProperties();
		ret.put(AgencyKV.Spawn.KEY_AGENT_CLASS, agentClassAlias);
		ret.put(AgencyKV.Spawn.KEY_START_BOUNDS, bounds);
		return ret;
	}

	public static ObjectProperties createTileAP(MapProperties mapProps, Rectangle bounds,
			TextureRegion tileTexRegion) {
		ObjectProperties agentProps = createRectangleAP(mapProps, bounds);
		// add a reference to the start tile texture region if non-null is given 
		if(tileTexRegion != null)
			agentProps.put(AgencyKV.Spawn.KEY_START_TEXREGION, tileTexRegion);
		return agentProps;
	}

	public static ObjectProperties createRectangleAP(MapProperties mapProps, Rectangle bounds) {
		ObjectProperties agentProps = createMapAP(mapProps);
		// copy the bounds rectangle to the agent properties
		agentProps.put(AgencyKV.Spawn.KEY_START_BOUNDS, bounds);
		return agentProps;
	}

	private static ObjectProperties createMapAP(MapProperties mapProps) {
		// copy the map properties to the agent properties
		ObjectProperties agentProps = new ObjectProperties();
		Iterator<String> keyIter = mapProps.getKeys();
		while(keyIter.hasNext()) {
			String key = keyIter.next();
			// add the map property to the agent properties map
			agentProps.put(key, mapProps.get(key));
		}
		return agentProps;
	}

	public static Vector2 getStartPoint(ObjectProperties agentProps) {
		Vector2 point = agentProps.get(AgencyKV.Spawn.KEY_START_POS, null, Vector2.class);
		if(point != null)
			return point;
		Rectangle bounds = agentProps.get(AgencyKV.Spawn.KEY_START_BOUNDS, null, Rectangle.class);
		if(bounds != null)
			return bounds.getCenter(new Vector2());
		return null;
	}

	public static Rectangle getStartBounds(ObjectProperties agentProps) {
		return agentProps.get(AgencyKV.Spawn.KEY_START_BOUNDS, null, Rectangle.class);
	}

	public static Vector2 getStartVelocity(ObjectProperties agentProps) {
		return agentProps.get(AgencyKV.Spawn.KEY_START_VEL, null, Vector2.class);
	}

	public static TextureRegion getStartTexRegion(ObjectProperties agentProps) {
		return agentProps.get(AgencyKV.Spawn.KEY_START_TEXREGION, null, TextureRegion.class);
	}

	public static Direction8 getStartDirection8(ObjectProperties agentProps) {
		return agentProps.get(CommonKV.KEY_DIRECTION, Direction8.NONE, Direction8.class);
	}
}
