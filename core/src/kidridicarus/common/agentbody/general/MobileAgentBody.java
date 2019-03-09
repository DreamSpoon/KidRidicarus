package kidridicarus.common.agentbody.general;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentbody.AgentBody;

public abstract class MobileAgentBody extends AgentBody {
	public Vector2 getVelocity() {
		return b2body.getLinearVelocity();
	}

	public void setVelocity(float x, float y) {
		b2body.setLinearVelocity(x, y);
	}

	public void setVelocity(Vector2 velocity) {
		// move if walking
		b2body.setLinearVelocity(velocity);
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
}
