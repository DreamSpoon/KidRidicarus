package com.ridicarus.kid.roles.robot;

import com.badlogic.gdx.math.Vector2;

public interface DamageableBot {
	public void onDamage(float amount, Vector2 fromCenter);
}
