package com.ridicarus.kid.roles.robot;

import com.badlogic.gdx.math.Vector2;

public interface BumpableBot {
	// brick bumped from below when mario jump punched the brick
	public void onBump(Vector2 fromCenter);
}
