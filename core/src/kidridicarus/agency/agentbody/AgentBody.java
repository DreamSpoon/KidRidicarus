package kidridicarus.agency.agentbody;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.agentcontact.CFBitSeq;

/*
 * Assume that an AgentBody can contain exactly one Box2D body. If more bodies are needed then a linked agent
 * scenario may be fruitful.
 */
public abstract class AgentBody implements Disposable {
	protected Body b2body = null;
	private Vector2 bodySize = new Vector2(0f, 0f);

	public abstract Agent getParent();

	public Vector2 getPosition() {
		return b2body.getPosition();
	}

	public void setBodySize(float width, float height) {
		bodySize.set(width, height);
	}

	public Vector2 getBodySize() {
		return bodySize.cpy();
	}

	public Rectangle getBounds() {
		return new Rectangle(b2body.getPosition().x - bodySize.x/2f, b2body.getPosition().y - bodySize.y/2f,
				bodySize.x, bodySize.y);
	}

	// body can pass through everything, maybe to fall off screen
	// TODO delete this method, implement differently
	public void disableAllContacts() {
		CFBitSeq catBits = new CFBitSeq();
		CFBitSeq maskBits = new CFBitSeq();
		for(Fixture fix : b2body.getFixtureList()) {
			if(!(fix.getUserData() instanceof AgentBodyFilter))
				continue;
			((AgentBodyFilter) fix.getUserData()).categoryBits = catBits;
			((AgentBodyFilter) fix.getUserData()).maskBits = maskBits;
			// the contact filters were changed, so let Box2D know to update contacts here
			fix.refilter();
		}
	}

	@Override
	public void dispose() {
		if(b2body != null) {
			b2body.getWorld().destroyBody(b2body);
			b2body = null;
		}
	}
}
