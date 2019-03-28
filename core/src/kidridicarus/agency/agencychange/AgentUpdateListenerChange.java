package kidridicarus.agency.agencychange;

import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.AllowOrder;

public class AgentUpdateListenerChange {
	public AgentPlaceholder ap;
	public AllowOrder updateOrder;
	public AgentUpdateListener auListener;
	public boolean add;

	public AgentUpdateListenerChange(AgentPlaceholder ap, AllowOrder newUpdateOrder, AgentUpdateListener auListener,
			boolean add) {
		this.ap = ap;
		this.updateOrder = newUpdateOrder;
		this.auListener = auListener;
		this.add = add;
	}
}
