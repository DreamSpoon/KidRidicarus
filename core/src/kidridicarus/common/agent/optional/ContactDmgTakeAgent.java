package kidridicarus.common.agent.optional;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;

// this agent can take contact damage
public interface ContactDmgTakeAgent {
	public void onDamage(Agent agent, float amount, Vector2 fromCenter);
}
