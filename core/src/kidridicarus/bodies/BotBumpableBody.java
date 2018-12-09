package kidridicarus.bodies;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.roles.PlayerRole;

public interface BotBumpableBody {
	// brick bumped from below when mario jump punched the brick
	public void onBump(PlayerRole perp, Vector2 fromCenter);
}
