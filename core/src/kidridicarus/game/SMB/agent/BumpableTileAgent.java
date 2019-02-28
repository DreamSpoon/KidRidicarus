package kidridicarus.game.SMB.agent;

import kidridicarus.agency.agent.Agent;

public interface BumpableTileAgent {
	// brick bumped from below when mario jump punched the brick
	public void onBumpTile(Agent bumpingAgent);
}
