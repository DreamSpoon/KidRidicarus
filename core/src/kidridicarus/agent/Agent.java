package kidridicarus.agent;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;

/*
 * Best Box2D analogy is the Body class. The agent can interact with other agents.
 * Also, the agent can have "contacts" (equivalent to Fixture in Box2D).
 * The "contacts" are used to detect when agent is on ground, collisions between agents, etc.
 */
public abstract class Agent implements Disposable {
	protected Agency agency;
	protected MapProperties properties;

	public Agent(Agency agency, AgentDef adef) {
		this.agency = agency;
		if(adef != null)
			properties = adef.properties;
	}

	public MapProperties getProperties() {
		return properties;
	}

	public abstract void update(float delta);
	public abstract void draw(Batch batch);
	public abstract Vector2 getPosition(); 
	public abstract Rectangle getBounds();
	public abstract Vector2 getVelocity();
}
