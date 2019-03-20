package kidridicarus.agency.agent;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.common.info.CommonCF;

/*
 * Assume that an AgentBody can contain exactly one Box2D body. If more bodies are needed then a body lsit
 * scenario may be fruitful.
 */
public abstract class AgentBody implements Disposable {
	private Agent parent;
	protected Body b2body;
	private Vector2 bodySize;

	public AgentBody(Agent parent) {
		this.parent = parent;
		b2body = null;
		bodySize = new Vector2(0f, 0f);
	}

	public Agent getParent() {
		return parent;
	}

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

	public Vector2 getVelocity() {
		return b2body.getLinearVelocity();
	}

	public void setVelocity(Vector2 velocity) {
		// move if walking
		b2body.setLinearVelocity(velocity);
	}

	public void setVelocity(float x, float y) {
		// move if walking
		b2body.setLinearVelocity(x, y);
	}

	public void zeroVelocity(boolean zeroX, boolean zeroY) {
		b2body.setLinearVelocity(zeroX ? 0f : b2body.getLinearVelocity().x,
				zeroY ? 0f : b2body.getLinearVelocity().y);
	}

	public void applyImpulse(Vector2 impulse) {
		b2body.applyLinearImpulse(impulse, b2body.getWorldCenter(), true);
	}

	public void applyForce(Vector2 f) {
		b2body.applyForceToCenter(f, true);
	}

	public void disableAllContacts() {
		for(Fixture fix : b2body.getFixtureList()) {
			((AgentBodyFilter) fix.getUserData()).categoryBits = CommonCF.NO_CONTACT_CFCAT;
			((AgentBodyFilter) fix.getUserData()).maskBits = CommonCF.NO_CONTACT_CFMASK;
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
