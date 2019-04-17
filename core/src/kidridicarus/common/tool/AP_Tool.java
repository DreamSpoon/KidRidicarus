package kidridicarus.common.tool;

import java.util.Iterator;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.common.info.CommonKV;

/*
 * Title: Agent Properties tool
 * Desc: Includes convenience methods for:
 *   -creating Agent properties
 *   -retrieving individual Agent properties
 */
public class AP_Tool {
	public static ObjectProperties createAP(String agentClassAlias) {
		ObjectProperties ret = new ObjectProperties();
		ret.put(AgencyKV.KEY_AGENT_CLASS, agentClassAlias);
		return ret;
	}

	public static ObjectProperties createPointAP(String agentClassAlias, Vector2 position) {
		ObjectProperties ret = new ObjectProperties();
		ret.put(AgencyKV.KEY_AGENT_CLASS, agentClassAlias);
		ret.put(CommonKV.KEY_POSITION, position);
		return ret;
	}

	public static ObjectProperties createPointAP(String agentClassAlias, Vector2 position, Vector2 velocity) {
		ObjectProperties ret = new ObjectProperties();
		ret.put(AgencyKV.KEY_AGENT_CLASS, agentClassAlias);
		ret.put(CommonKV.KEY_POSITION, position);
		ret.put(CommonKV.KEY_VELOCITY, velocity);
		return ret;
	}

	public static ObjectProperties createRectangleAP(String agentClassAlias, Rectangle bounds) {
		ObjectProperties ret = new ObjectProperties();
		ret.put(AgencyKV.KEY_AGENT_CLASS, agentClassAlias);
		ret.put(CommonKV.KEY_BOUNDS, bounds);
		return ret;
	}

	public static ObjectProperties createTileAP(MapProperties mapProps, Rectangle bounds,
			TextureRegion tileTexRegion) {
		ObjectProperties agentProps = createRectangleAP(mapProps, bounds);
		// add a reference to the start tile texture region if non-null is given 
		if(tileTexRegion != null)
			agentProps.put(CommonKV.KEY_TEXREGION, tileTexRegion);
		return agentProps;
	}

	public static ObjectProperties createRectangleAP(MapProperties mapProps, Rectangle bounds) {
		ObjectProperties agentProps = createMapAP(mapProps);
		// copy the bounds rectangle to the agent properties
		agentProps.put(CommonKV.KEY_BOUNDS, bounds);
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

	public static Vector2 getCenter(ObjectProperties agentProps) {
		Vector2 point = agentProps.get(CommonKV.KEY_POSITION, null, Vector2.class);
		if(point != null)
			return point;
		Rectangle bounds = agentProps.get(CommonKV.KEY_BOUNDS, null, Rectangle.class);
		if(bounds != null)
			return bounds.getCenter(new Vector2());
		return null;
	}

	public static Vector2 getCenter(Agent agent) {
		Vector2 point = agent.getProperty(CommonKV.KEY_POSITION, null, Vector2.class);
		if(point != null)
			return point;
		Rectangle bounds = agent.getProperty(CommonKV.KEY_BOUNDS, null, Rectangle.class);
		if(bounds != null)
			return bounds.getCenter(new Vector2());
		return null;
	}

	public static Rectangle getBounds(ObjectProperties agentProps) {
		return agentProps.get(CommonKV.KEY_BOUNDS, null, Rectangle.class);
	}

	public static Rectangle getBounds(Agent agent) {
		return agent.getProperty(CommonKV.KEY_BOUNDS, null, Rectangle.class);
	}

	public static Vector2 getVelocity(ObjectProperties agentProps) {
		return agentProps.get(CommonKV.KEY_VELOCITY, null, Vector2.class);
	}

	public static TextureRegion getTexRegion(ObjectProperties agentProps) {
		return agentProps.get(CommonKV.KEY_TEXREGION, null, TextureRegion.class);
	}

	public static Direction8 getDirection8(ObjectProperties agentProps) {
		return agentProps.get(CommonKV.KEY_DIRECTION, Direction8.NONE, Direction8.class);
	}

	public static boolean getFacingRight(Agent agent) {
		return agent.getProperty(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class) == Direction4.RIGHT;
	}
}
