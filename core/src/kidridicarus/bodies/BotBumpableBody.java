package kidridicarus.bodies;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.roles.PlayerRole;

public interface BotBumpableBody {
	// Could be a player jump punching (head banging?) the block from below, or the enemy on top of the block is
	// bumped because the player jump punched the block below. 
	public void onBump(PlayerRole perp, Vector2 fromCenter);
}
