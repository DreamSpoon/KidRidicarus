package kidridicarus.game.SMB.agent;

import kidridicarus.agency.agent.Agent;

// a tile agent that can be bumped (i.e. take bumps)
public interface TileBumpTakeAgent {
	// brick bumped from below when mario jump punched the brick
	public void onBumpTile(Agent bumpingAgent);
}
