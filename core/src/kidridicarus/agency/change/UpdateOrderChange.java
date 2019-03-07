package kidridicarus.agency.change;

import kidridicarus.common.tool.AllowOrder;

/*
 * Change the "updates enabled" state of an agent.
 */
public class UpdateOrderChange {
	public AgentPlaceholder ap;
	public AllowOrder updateOrder;

	public UpdateOrderChange(AgentPlaceholder agent, AllowOrder updateOrder) {
		this.ap = agent;
		this.updateOrder = updateOrder;
	}
}
