package kidridicarus.agency.agencychange;

import kidridicarus.agency.AgentUpdateListener;
import kidridicarus.agency.tool.AllowOrder;

public class UpdateListenerChange {
	public AgentPlaceholder ap;
	public AllowOrder updateOrder;
	public AgentUpdateListener auListener;
	public boolean add;

	public UpdateListenerChange(AgentPlaceholder ap, AllowOrder newUpdateOrder, AgentUpdateListener auListener,
			boolean add) {
		this.ap = ap;
		this.updateOrder = newUpdateOrder;
		this.auListener = auListener;
		this.add = add;
	}
}
