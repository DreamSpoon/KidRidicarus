package kidridicarus.agency.agencychange;

import kidridicarus.agency.Agent;

/*
 * Using this class allows references to Agents to be added to a list before the Agent reference is available,
 * this is necessary to allow proper chronological order insertion of Agency changes into the change queue, because:
 * When an Agent's constructor is called it might create draw/update listener changes, so a placeholder is needed to
 * insert the "Agent add" event before the "update/draw listener add" events in the Agency change queue.
 * Otherwise the order of operations in the agency change queue will be wrong:
 *   The "add Agent to the all Agents list" event would come after the "add update listener" and
 *   "add draw listener" events for this Agent!
 */
public class AgentPlaceholder {
	public Agent agent;
	public AgentPlaceholder(Agent agent) {
		this.agent = agent;
	}
}
