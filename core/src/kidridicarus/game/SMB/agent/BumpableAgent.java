package kidridicarus.game.SMB.agent;

import kidridicarus.agency.agent.Agent;

public interface BumpableAgent {
	// agent on block when block is jump punched by mario
	public void onBump(Agent bumpingAgent);
}
