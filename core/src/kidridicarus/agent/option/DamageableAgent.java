package kidridicarus.agent.option;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agent.Agent;

public interface DamageableAgent {
	public void onDamage(Agent perp, float amount, Vector2 fromCenter);
}
