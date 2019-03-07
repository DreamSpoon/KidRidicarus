package kidridicarus.agency.change;

import kidridicarus.common.tool.AllowOrder;

/*
 * Extra info about an individual agent. This information is to be used exclusively by the Agency class. 
 */
public class AgentWrapper {
//	public boolean receiveUpdates;
	public AllowOrder updateOrder;
	public AllowOrder drawOrder;

	public AgentWrapper() {
//		this.receiveUpdates = false;
		this.updateOrder = new AllowOrder(false, 0f);
		this.drawOrder = new AllowOrder(false, 0f);
	}
}
