package kidridicarus.agency.agent.body.optional;

import kidridicarus.agency.agent.Agent;

public interface BumpableBody {
	// mario jump punched the block from below, and the agent on top of the block is bumped
	public void onBump(Agent bumpingAgent);
}
