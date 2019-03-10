package kidridicarus.agency.agencychange;

import java.util.LinkedList;

import kidridicarus.agency.AgentUpdateListener;
import kidridicarus.agency.tool.AllowOrder;

/*
 * Extra info about an individual agent. This information is to be used exclusively by the Agency class. 
 */
public class AgentWrapper {
	public LinkedList<AgentUpdateListener> updateListeners;
//	public AllowOrder updateOrder;
	public AllowOrder drawOrder;

	public AgentWrapper() {
//		this.updateOrder = new AllowOrder(false, 0f);
		updateListeners = new LinkedList<AgentUpdateListener>();
		drawOrder = AllowOrder.NOT_ALLOWED;
	}
}
