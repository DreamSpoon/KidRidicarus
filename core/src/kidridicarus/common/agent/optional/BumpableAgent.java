package kidridicarus.common.agent.optional;

import kidridicarus.agency.agent.Agent;

public interface BumpableAgent {
	// agent on block when block is jump punched by mario
	public void onBump(Agent bumpingAgent);
}
