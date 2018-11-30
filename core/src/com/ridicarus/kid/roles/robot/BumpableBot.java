package com.ridicarus.kid.roles.robot;

import com.badlogic.gdx.math.Vector2;
import com.ridicarus.kid.roles.PlayerRole;

public interface BumpableBot {
	// brick bumped from below when mario jump punched the brick
	public void onBump(PlayerRole perp, Vector2 fromCenter);
}
