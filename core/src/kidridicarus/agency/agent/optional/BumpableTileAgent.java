package kidridicarus.agency.agent.optional;

import kidridicarus.agency.agent.Agent;

public interface BumpableTileAgent {
	// brick bumped from below when mario jump punched the brick
	public void onBumpTile(Agent bumpingAgent);
}
