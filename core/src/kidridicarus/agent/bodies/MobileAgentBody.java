package kidridicarus.agent.bodies;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.GameInfo;

public abstract class MobileAgentBody extends AgentBody {
	private int onGroundCount = 0;

	protected abstract void onContactVertBoundLine(LineSeg seg);
	public void onContactBoundLine(LineSeg seg) {
		Rectangle bounds = getBounds();
		float lineBeginY = seg.getB2Begin();
		float lineEndY = seg.getB2End();
		float meBeginY = bounds.y;
		float meEndY = bounds.y + bounds.height;
		// contacted vertical bound?
		if(!seg.isHorizontal) {
			// check for actual bound contact, not just close call...
			// we want to know if this bound is blocking just a teensy bit or a large amount
			if(meBeginY + GameInfo.BODY_VS_VERT_BOUND_EPSILON < lineEndY &&
					meEndY - GameInfo.BODY_VS_VERT_BOUND_EPSILON > lineBeginY) {
				// bounce off of vertical bounds
				onContactVertBoundLine(seg);
			}
		}
	}

	public void onBeginContactGround() {
		onGroundCount++;
	}

	public void onEndContactGround() {
		onGroundCount--;
	}

	// Foot sensor might come into contact with multiple boundary lines, so increment for each contact start,
	// and decrement for each contact end. If onGroundCount reaches zero then mario is not on the ground.
	public boolean isOnGround() {
		return onGroundCount > 0;
	}

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

	public void zeroVelocity() {
		b2body.setLinearVelocity(0f, 0f);
	}

	public void applyImpulse(Vector2 impulse) {
		b2body.applyLinearImpulse(impulse, b2body.getWorldCenter(), true);
	}
}
