package kidridicarus.agency.agencychange;

import kidridicarus.agency.agent.Agent;

/*
 * Using this class allows references to agents to be added to a list before the agent reference is available.
 * E.g. Use the placeholder when calling an agent's constructor, while adding the agent to the list of
 * all agents queue. Since the agent reference doesn't exist until after the agent constructor is finished running -
 * and the agent constructor may invoke enable disable updates / set draw order.
 * Otherwise the order of operations in the agency change queue will be wrong:
 *   The "add this agent to the all agents list" event would come after the "set draw order" and "enable update"
 *   events for this same agent! 
 */
public class AgentPlaceholder {
	public Agent agent;
	public AgentPlaceholder(Agent agent) {
		this.agent = agent;
	}
}
