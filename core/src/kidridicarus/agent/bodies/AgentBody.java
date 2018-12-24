package kidridicarus.agent.bodies;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.contacts.AgentBodyFilter;
import kidridicarus.agency.contacts.CFBitSeq;
import kidridicarus.agency.contacts.CFBitSeq.CFBit;
import kidridicarus.agent.Agent;

/*
 * Assume that an AgentBody can contain only 1 Box2D body. If more bodies are needed then a linked agent
 * scenario may be fruitful.
 */
public abstract class AgentBody implements Disposable {
	private float bodyWidth = 0f;
	private float bodyHeight = 0f;
	protected Body b2body = null;
	public abstract Agent getParent();

	public Vector2 getPosition() {
		return b2body.getPosition();
	}

	public void setBodySize(float width, float height) {
		bodyWidth = width;
		bodyHeight = height;
	}

	public Rectangle getBounds() {
		return new Rectangle(b2body.getPosition().x - bodyWidth/2f,
				b2body.getPosition().y - bodyHeight/2f, bodyWidth, bodyHeight);
	}

	public void setActive(boolean active) {
		b2body.setActive(active);
	}

	// other agents can now pass through the body, but body will not fall through floor
	public void makeUncontactable() {

		CFBitSeq catBits = new CFBitSeq(CFBit.SOLID_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SOLID_BOUND_BIT);
		for(Fixture fix : b2body.getFixtureList()) {
			if(!(fix.getUserData() instanceof AgentBodyFilter))
				continue;
			((AgentBodyFilter) fix.getUserData()).categoryBits = catBits;
			((AgentBodyFilter) fix.getUserData()).maskBits = maskBits;
			// the contact filters were changed, so let Box2D know to update contacts here
			fix.refilter();
		}
	}

	// body can pass through everything, to fall off screen
	public void disableContacts() {
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
