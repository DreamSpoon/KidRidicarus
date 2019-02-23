package kidridicarus.agency.change;

/*
 * Change desc: Add/Remove agent to/from list of all agents.
 */
public class AgentListChange {
	public AgentPlaceholder ap;
	// if true then add the agent to the list, otherwise remove the agent from the list
	public boolean add;

	public AgentListChange(AgentPlaceholder agent, boolean add) {
		this.ap = agent;
		this.add = add;
	}
}
