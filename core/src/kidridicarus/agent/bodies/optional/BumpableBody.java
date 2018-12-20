package kidridicarus.agent.bodies.optional;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agent.Agent;

public interface BumpableBody {
	// Could be a player jump punching (head banging?) the block from below, or the enemy on top of the block is
	// bumped because the player jump punched the block below. 
	public void onBump(Agent bumpingAgent, Vector2 fromCenter);
}
