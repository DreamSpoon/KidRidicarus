package kidridicarus.agent.bodies;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agent.bodies.sensor.WalkingSensor.WalkingSensorType;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.GameInfo;

public abstract class MobileGroundAgentBody extends MobileAgentBody {
	private int onGroundCount = 0;

	public void onBodyBeginContact(LineSeg lineSeg) {
		if(!lineSeg.isHorizontal)
			onContactVertBoundLine(lineSeg);
	}

	public void onBeginContactSensor(WalkingSensorType sensorType, LineSeg lineSeg) {
		if(sensorType == WalkingSensorType.FOOT && lineSeg.isHorizontal && lineSeg.upNormal)
			onBeginContactGround();
	}

	public void onEndContactSensor(WalkingSensorType sensorType, LineSeg lineSeg) {
		if(sensorType == WalkingSensorType.FOOT && lineSeg.isHorizontal && lineSeg.upNormal)
			onEndContactGround();
	}

	public void onBeginContactGround() {
		onGroundCount++;
	}

	public void onEndContactGround() {
		onGroundCount--;
	}

	protected abstract void onContactWall(LineSeg seg);
	private void onContactVertBoundLine(LineSeg seg) {
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
				onContactWall(seg);
			}
		}
	}

	// Foot sensor might come into contact with multiple boundary lines, so increment for each contact start,
	// and decrement for each contact end. If onGroundCount reaches zero then mario is not on the ground.
	public boolean isOnGround() {
		return onGroundCount > 0;
	}
}
