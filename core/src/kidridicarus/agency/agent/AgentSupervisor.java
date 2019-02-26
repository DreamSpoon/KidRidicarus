package kidridicarus.agency.agent;

import kidridicarus.agency.tool.BasicAdvice;
import kidridicarus.agency.tool.SuperAdvice;

/*
 * Supervisor is expected to handle stuff for PlayerAgents:
 *   -scripted actions
 *   -relaying advice to the agent
 *   -...
 */
public interface AgentSupervisor {
	public void setFrameAdvice(SuperAdvice superAdvice);
	public BasicAdvice pollFrameAdvice();
}
