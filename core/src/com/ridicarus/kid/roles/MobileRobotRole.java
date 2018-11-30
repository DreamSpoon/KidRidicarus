package com.ridicarus.kid.roles;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.ridicarus.kid.collisionmap.LineSeg;

public abstract class MobileRobotRole implements RobotRole {
	protected Vector2 velocity;

	protected abstract void onInnerTouchBoundLine(LineSeg seg);
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
			if(meBeginY + ROBOT_VS_VERT_BOUND_EPSILON < lineEndY &&
					meEndY - ROBOT_VS_VERT_BOUND_EPSILON > lineBeginY) {
				// bounce off of vertical bounds
				onInnerTouchBoundLine(seg);
			}
		}
	}

	protected void reverseVelocity(boolean x, boolean y) {
		if(x)
			velocity.x = -velocity.x;
		if(y)
			velocity.y = -velocity.y;
	}

	protected void setVelocity(float x, float y) {
		velocity.x = x;
		velocity.y = y;
	}
}
