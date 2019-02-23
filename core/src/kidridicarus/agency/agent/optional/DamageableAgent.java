package kidridicarus.agency.agent.optional;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;

public interface DamageableAgent {
	public void onDamage(Agent agent, float amount, Vector2 fromCenter);
}
