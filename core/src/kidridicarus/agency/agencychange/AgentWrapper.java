package kidridicarus.agency.agencychange;

import kidridicarus.common.tool.AllowOrder;

/*
 * Extra info about an individual agent. This information is to be used exclusively by the Agency class. 
 */
public class AgentWrapper {
	public AllowOrder updateOrder;
	public AllowOrder drawOrder;

	public AgentWrapper() {
		this.updateOrder = new AllowOrder(false, 0f);
		this.drawOrder = new AllowOrder(false, 0f);
	}
}
