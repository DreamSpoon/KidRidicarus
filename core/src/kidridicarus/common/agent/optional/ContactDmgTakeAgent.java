package kidridicarus.common.agent.optional;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;

public interface ContactDmgTakeAgent {
	// returns true if damage was taken, otherwise returns false
	public boolean onTakeDamage(Agent agent, float amount, Vector2 fromCenter);
}
