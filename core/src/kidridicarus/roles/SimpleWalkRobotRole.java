package kidridicarus.roles;

import com.badlogic.gdx.math.Vector2;

public abstract class SimpleWalkRobotRole implements RobotRole {
	private Vector2 constVelocity = new Vector2();

	protected void reverseConstVelocity(boolean x, boolean y) {
		if(x)
			constVelocity.x = -constVelocity.x;
		if(y)
			constVelocity.y = -constVelocity.y;
	}

	protected void setConstVelocity(Vector2 v) {
		setConstVelocity(v.x, v.y);
	}

	protected void setConstVelocity(float x, float y) {
		constVelocity.x = x;
		constVelocity.y = y;
	}

	protected Vector2 getConstVelocity() {
		return constVelocity;
	}
}
