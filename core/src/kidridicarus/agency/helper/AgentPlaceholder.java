package kidridicarus.agency.helper;

import kidridicarus.agent.Agent;

/*
 * Using this class allows references to agents to be added to a list before the agent reference is available.
 * E.g. Use the placeholder when creating agents and adding to agent change queue, since the agent reference
 * doesn't exist until after the agent constructor is finished running - and the agent constructor may invoke
 * enable disable updates / set draw order.
 */
public class AgentPlaceholder {
	public Agent agent;
	public AgentPlaceholder(Agent agent) {
		this.agent = agent;
	}
}
