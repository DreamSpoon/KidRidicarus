package kidridicarus.common.agent.optional;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.GameTeam;

public interface ContactDmgTakeAgent {
	/*
	 * Returns true if damage was taken, otherwise returns false.
	 * AgentTeam is the team of the agent that is pushing damage to this agent.
	 */
	public boolean onTakeDamage(Agent agent, GameTeam aTeam, float amount, Vector2 dmgOrigin);
}
