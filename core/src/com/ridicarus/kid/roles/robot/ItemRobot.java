package com.ridicarus.kid.roles.robot;

import com.badlogic.gdx.math.Vector2;
import com.ridicarus.kid.roles.PlayerRole;
import com.ridicarus.kid.roles.RobotRole;

public abstract class ItemRobot extends RobotRole {
	public enum PowerupType { NONE, MUSHROOM, FIREFLOWER, POWERSTAR };

	protected Vector2 maxVelocity;

	public abstract void use(PlayerRole role);

	protected void setVelocity(float x, float y) {
		maxVelocity.x = x;
		maxVelocity.y = y;
	}

	protected void reverseVelocity(boolean x, boolean y) {
		if(x)
			maxVelocity.x = -maxVelocity.x;
		if(y)
			maxVelocity.y = -maxVelocity.y;
	}
}
