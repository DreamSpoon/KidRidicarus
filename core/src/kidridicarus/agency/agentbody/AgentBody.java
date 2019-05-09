package kidridicarus.agency.agentbody;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agent;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;

/*
 * Assume that an AgentBody can contain exactly one Box2D body. If more bodies are needed then a body list
 * scenario may be fruitful.
 * Body is brute and dumb - any movements, forces, etc. can be accomplished simply by calling the body's methods,
 * but the methods tend to the simple: setPosition, applyForce, applyImpulse, etc.
 * However, for more organized/coordinated movements, use a spine instead (e.g. SamusSpine.applyDamageKick method).
 * TODO
 *   -use applyForce, applyImpulse, setVelocity paradigm as much as possible, instead of manually modifying
 *    body position
 *   -caller should explicitly invoke the create/destroy methods when manually modifying the position of AgentBody
 *   -remove the defineBody/dispose paradigm, replace with the above described create/destroy method paradigm
 */
public abstract class AgentBody implements Disposable {
	private Agent parent;
	protected World world;
	protected Body b2body;
	// bounds size is for information purposes only, it is not necessarily the current dimensions of b2body
	private Vector2 boundsSize;

	protected abstract void defineBody(Rectangle bounds, Vector2 velocity);
	protected void defineBody(Rectangle bounds) {
		defineBody(bounds, new Vector2(0f, 0f));
	}

	public AgentBody(Agent parent, World world) {
		this.parent = parent;
		this.world = world;
		b2body = null;
		boundsSize = new Vector2(0f, 0f);
	}

	public void checkDoDefineBody(Vector2 position, boolean keepVelocity) {
		// exit if the new position is the same as current position
		if(position.epsilonEquals(b2body.getPosition(), UInfo.POS_EPSILON)) {
			// zero velocity if needed
			if(!keepVelocity)
				b2body.setLinearVelocity(0f,  0f);
			return;
		}
		if(keepVelocity) {
			defineBody(new Rectangle(position.x-getBounds().width/2f, position.y-getBounds().height/2f,
					getBounds().width, getBounds().height), b2body.getLinearVelocity());
		}
		else {
			defineBody(new Rectangle(position.x-getBounds().width/2f, position.y-getBounds().height/2f,
					getBounds().width, getBounds().height), new Vector2(0f, 0f));
		}
	}

	public Agent getParent() {
		return parent;
	}

	public Vector2 getPosition() {
		return b2body.getPosition();
	}

	public void setBoundsSize(float width, float height) {
		boundsSize.set(width, height);
	}

	public Rectangle getBounds() {
		return new Rectangle(b2body.getPosition().x - boundsSize.x/2f, b2body.getPosition().y - boundsSize.y/2f,
				boundsSize.x, boundsSize.y);
	}

	public Vector2 getVelocity() {
		return b2body.getLinearVelocity();
	}

	public void setVelocity(Vector2 velocity) {
		b2body.setLinearVelocity(velocity);
	}

	public void setVelocity(float x, float y) {
		b2body.setLinearVelocity(x, y);
	}

	// convenience method
	public void zeroVelocity(boolean zeroX, boolean zeroY) {
		b2body.setLinearVelocity(
				zeroX ? 0f : b2body.getLinearVelocity().x, zeroY ? 0f : b2body.getLinearVelocity().y);
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
