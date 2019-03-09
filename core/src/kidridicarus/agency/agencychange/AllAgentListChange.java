package kidridicarus.agency.agencychange;

/*
 * Change desc: Add/Remove agent to/from list of all agents.
 */
public class AllAgentListChange {
	public AgentPlaceholder ap;
	// if true then add the agent to the list, otherwise remove the agent from the list
	public boolean add;

	public AllAgentListChange(AgentPlaceholder agent, boolean add) {
		this.ap = agent;
		this.add = add;
	}
}
