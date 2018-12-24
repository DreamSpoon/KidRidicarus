package kidridicarus.agent.optional;

import kidridicarus.agent.Agent;

public interface BumpableAgent {
	// agent on block when block is jump punched by mario
	public void onBump(Agent bumpingAgent);
}
