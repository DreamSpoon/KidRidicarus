package kidridicarus.bodies;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.GameInfo;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.roles.RobotRole;

public abstract class RobotBody implements Disposable {
	protected Body b2body = null;
	private float bodyWidth = 0f;
	private float bodyHeight = 0f;
	private int onGroundCount = 0;

	public abstract RobotRole getRole();

	protected abstract void onTouchVertBoundLine(LineSeg seg);
	public void onTouchBoundLine(LineSeg seg) {
		Rectangle bounds = getBounds();
		float lineBeginY = seg.getB2Begin();
		float lineEndY = seg.getB2End();
		float meBeginY = bounds.y;
		float meEndY = bounds.y + bounds.height;
		// touched vertical bound?
		if(!seg.isHorizontal) {
			// check for actual bound touch, not just close call...
			// we want to know if this bound is blocking just a teensy bit or a large amount
			if(meBeginY + GameInfo.ROBOT_VS_VERT_BOUND_EPSILON < lineEndY &&
					meEndY - GameInfo.ROBOT_VS_VERT_BOUND_EPSILON > lineBeginY) {
				// bounce off of vertical bounds
				onTouchVertBoundLine(seg);
			}
		}
	}

	public void onTouchGround() {
		onGroundCount++;
	}

	public void onLeaveGround() {
		onGroundCount--;
	}

	// Foot sensor might come into contact with multiple boundary lines, so increment for each contact start,
	// and decrement for each contact end. If onGroundCount reaches zero then mario is not on the ground.
	public boolean isOnGround() {
		return onGroundCount > 0;
	}

	public Vector2 getPosition() {
		return b2body.getPosition();
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

	public void setActive(boolean active) {
		b2body.setActive(active);
	}

	public void setBodySize(float width, float height) {
		bodyWidth = width;
		bodyHeight = height;
	}

	public Rectangle getBounds() {
		return new Rectangle(b2body.getPosition().x - bodyWidth/2f,
				b2body.getPosition().y - bodyHeight/2f, bodyWidth, bodyHeight);
	}

	public void makeUntouchable() {
		// other robots can now pass through the body, but body will not fall through floor
		Filter filter = new Filter();
		filter.categoryBits = GameInfo.ROBOT_BIT;
		filter.maskBits = GameInfo.BOUNDARY_BIT;
		for(Fixture fix : b2body.getFixtureList())
			fix.setFilterData(filter);
	}

	public void disableContacts() {
		// body can pass through everything, to fall off screen
		Filter filter = new Filter();
		filter.categoryBits = GameInfo.NOTHING_BIT;
		filter.maskBits = GameInfo.NOTHING_BIT;
		for(Fixture fix : b2body.getFixtureList())
			fix.setFilterData(filter);
	}

	@Override
	public void dispose() {
		if(b2body != null) {
			b2body.getWorld().destroyBody(b2body);
			b2body = null;
		}
	}
}
