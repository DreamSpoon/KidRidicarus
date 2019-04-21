package kidridicarus.game.SMB1.agent;

import kidridicarus.agency.agent.Agent;

public interface BumpTakeAgent {
	// agent on block when block is jump punched by mario
	public void onTakeBump(Agent bumpingAgent);
}
