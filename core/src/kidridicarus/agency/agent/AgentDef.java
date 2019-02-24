package kidridicarus.agency.agent;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.game.info.KVInfo;

public class AgentDef {
	public Rectangle bounds;
	public Vector2 velocity;
	public TextureRegion tileTexRegion;
	// the loader will derive the agent class from the properties
	public MapProperties properties;
	public Object userData;

	public AgentDef() {
		bounds = new Rectangle();
		velocity = new Vector2(0f, 0f);
		properties = new MapProperties();
		tileTexRegion = null;
		userData = null;
	}

	public static AgentDef makePointBoundsDef(String agentClass, Vector2 position) {
		AgentDef adef = new AgentDef();
		adef.properties.put(KVInfo.Spawn.KEY_AGENTCLASS, agentClass);
		adef.bounds.set(position.x, position.y, 0f, 0f);
		return adef;
	}

	public static AgentDef makeBoxBoundsDef(String agentClass, Vector2 position, float width, float height) {
		AgentDef adef = new AgentDef();
		adef.properties.put(KVInfo.Spawn.KEY_AGENTCLASS, agentClass);
		adef.bounds.set(position.x, position.y, width, height);
		return adef;
	}
}
