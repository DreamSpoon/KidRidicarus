/*
 * By: David Loucks
 * Approx. Date: 2018.11.08
*/

package com.ridicarus.kid.roles;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.ridicarus.kid.collisionmap.LineSeg;

public abstract class RobotRole {
	// TODO: this is a guess value (0.001f) - test more to refine - may depend upon Pixels Per Meter and Pixels Per Tile
	public static final float ROBOT_VS_VERT_BOUND_EPSILON = 0.001f;

	public abstract void update(float delta);
	public abstract void draw(Batch batch);
	public abstract Body getBody();

	public abstract Rectangle getBounds();
	protected abstract void onInnerTouchBoundLine(LineSeg seg);
	public void onTouchBoundLine(LineSeg seg) {
		Rectangle bounds = getBounds();
		float lineBeginY = seg.getWorldBegin();
		float lineEndY = seg.getWorldEnd();
		float meBeginY = bounds.y;
		float meEndY = bounds.y + bounds.height;
		// touched vertical bound?
		if(!seg.isHorizontal) {
			// check for actual bound touch, not just close call...
			// we want to know if this bound is blocking just a teensy bit or a large amount
//System.out.println("vert line seg check: meIs=" + meBeginY + ", " + meEndY + ", lineIs=" + lineBeginY + ", " + lineEndY);
			if(meBeginY + ROBOT_VS_VERT_BOUND_EPSILON < lineEndY &&
					meEndY - ROBOT_VS_VERT_BOUND_EPSILON > lineBeginY) {
				// bounce off of vertical bounds
				onInnerTouchBoundLine(seg);
			}
		}
	}

	public abstract void onTouchRobot(RobotRole robo);
	public abstract void onTouchGround();
	public abstract void onLeaveGround();

	public abstract void dispose();
}
