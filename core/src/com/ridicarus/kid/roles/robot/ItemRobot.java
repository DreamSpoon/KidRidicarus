package com.ridicarus.kid.roles.robot;

import com.ridicarus.kid.roles.PlayerRole;
import com.ridicarus.kid.roles.RobotRole;

public abstract class ItemRobot extends RobotRole {
	public enum PowerupType { NONE, MUSHROOM, FIREFLOWER };
	public abstract void use(PlayerRole role);
}
