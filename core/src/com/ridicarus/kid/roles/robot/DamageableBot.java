package com.ridicarus.kid.roles.robot;

import com.badlogic.gdx.math.Vector2;
import com.ridicarus.kid.roles.PlayerRole;

public interface DamageableBot {
	public void onDamage(PlayerRole perp, float amount, Vector2 fromCenter);
}
