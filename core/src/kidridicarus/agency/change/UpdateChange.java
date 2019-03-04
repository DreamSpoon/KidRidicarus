package kidridicarus.agency.change;

/*
 * Change the "updates enabled" state of an agent.
 */
public class UpdateChange {
	public AgentPlaceholder ap;
	public boolean enableUpdate;

	public UpdateChange(AgentPlaceholder agent, boolean enableUpdate) {
		this.ap = agent;
		this.enableUpdate = enableUpdate;
	}
}
