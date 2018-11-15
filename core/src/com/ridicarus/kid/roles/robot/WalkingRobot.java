package com.ridicarus.kid.roles.robot;

import com.badlogic.gdx.math.Vector2;
import com.ridicarus.kid.roles.RobotRole;

public abstract class WalkingRobot extends RobotRole {
	protected Vector2 velocity;
	protected void reverseVelocity(boolean x, boolean y){
		if(x)
			velocity.x = -velocity.x;
		if(y)
			velocity.y = -velocity.y;
	}
}
