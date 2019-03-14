package kidridicarus.game.agent.SMB;

import kidridicarus.agency.agent.Agent;

// TODO replace this class with BumpableAgent, or just delete this class...
public interface BumpableBody {
	// mario jump punched the block from below, and the agent on top of the block is bumped
	public void onBump(Agent bumpingAgent);
}
