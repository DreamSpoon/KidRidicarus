package kidridicarus.agency.agent;

import java.util.Iterator;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.ObjectProperties;

/*
 * The Agent can interact with other Agents (like Body in Box2D).
 * The Agent can have ContactSensors (equivalent to Fixture in Box2D).
 * The ContactSensors are used to detect when Agent is on ground, collisions between Agents, etc.
 */
public abstract class Agent {
	protected Agency agency;
	protected ObjectProperties properties;

	public Agent(Agency agency, ObjectProperties properties) {
		this.agency = agency;
		this.properties = properties;
	}

	// TODO Remove these 2 methods, replace with calls to getProperty with something like "current_position",
	//   or "current_bounds". Thus all queries for agent properties are routed through the getProperty method.
	public abstract Vector2 getPosition();
	public abstract Rectangle getBounds();

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
		return properties.get(key, defaultValue, cls);
	}

	public boolean containsPropertyKV(String[] keys, Object[] vals) {
		return properties.containsAllKV(keys, vals);
	}

	public static ObjectProperties createPointAP(String agentClassAlias, Vector2 position) {
		ObjectProperties ret = new ObjectProperties();
		ret.put(AgencyKV.Spawn.KEY_AGENTCLASS, agentClassAlias);
		ret.put(AgencyKV.Spawn.KEY_START_POINT, position);
		return ret;
	}

	public static ObjectProperties createPointAP(String agentClassAlias, Vector2 position, Vector2 velocity) {
		ObjectProperties ret = new ObjectProperties();
		ret.put(AgencyKV.Spawn.KEY_AGENTCLASS, agentClassAlias);
		ret.put(AgencyKV.Spawn.KEY_START_POINT, position);
		ret.put(AgencyKV.Spawn.KEY_START_VELOCITY, velocity);
		return ret;
	}

	public static ObjectProperties createRectangleAP(String agentClassAlias, Rectangle bounds) {
		ObjectProperties ret = new ObjectProperties();
		ret.put(AgencyKV.Spawn.KEY_AGENTCLASS, agentClassAlias);
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
		Vector2 point = agentProps.get(AgencyKV.Spawn.KEY_START_POINT, null, Vector2.class);
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
		return agentProps.get(AgencyKV.Spawn.KEY_START_VELOCITY, null, Vector2.class);
	}

	public static TextureRegion getStartTexRegion(ObjectProperties agentProps) {
		return agentProps.get(AgencyKV.Spawn.KEY_START_TEXREGION, null, TextureRegion.class);
	}
}
