package kidridicarus.roles.robot;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.roles.PlayerRole;

public interface DamageableBot {
	public void onDamage(PlayerRole perp, float amount, Vector2 fromCenter);
}
