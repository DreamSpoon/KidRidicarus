package kidridicarus.agent.body.option;

import kidridicarus.agent.Agent;

public interface BumpableBody {
	// mario jump punched the block from below, and the agent on top of the block is bumped
	public void onBump(Agent bumpingAgent);
}
