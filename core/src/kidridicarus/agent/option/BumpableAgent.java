package kidridicarus.agent.option;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agent.Agent;

public interface BumpableAgent {
	// brick bumped from below when mario jump punched the brick
	public void onBump(Agent perp, Vector2 fromCenter);
}
