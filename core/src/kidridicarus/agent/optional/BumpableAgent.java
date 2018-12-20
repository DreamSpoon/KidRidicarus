package kidridicarus.agent.optional;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agent.Agent;

public interface BumpableAgent {
	// brick bumped from below when mario jump punched the brick
	public void onBump(Agent bumpingAgent, Vector2 fromCenter);
}
