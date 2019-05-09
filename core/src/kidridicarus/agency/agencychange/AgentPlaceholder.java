package kidridicarus.agency.agencychange;

import kidridicarus.agency.Agent;

/*
 * Using this class allows references to agents to be added to a list before the agent reference is available.
 * This is necessary to allow proper chronological order insertion of agency changes into the change Q, because...
 * When an Agent's constructor is called it might create draw/update order changes, so to insert the Agent
 * add event before the update/draw order change events in the queue, a placeholder/wrapper class is necessary.
 * Otherwise the order of operations in the agency change queue will be wrong:
 *   The "add this agent to the all agents list" event would come after the "add update listener" and
 *   "add draw listener" events for this same agent! 
 */
public class AgentPlaceholder {
	public Agent agent;
	public AgentPlaceholder(Agent agent) {
		this.agent = agent;
	}
}
