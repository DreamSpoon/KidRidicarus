package kidridicarus.agency.change;

import kidridicarus.tool.DrawOrder;

/*
 * Extra info about an individual agent. This information is to be used exclusively by the Agency class. 
 */
public class AgentWrapper {
	public boolean receiveUpdates;
	public DrawOrder drawOrder;

	public AgentWrapper(boolean receiveUpdates, DrawOrder drawOrder) {
		this.receiveUpdates = receiveUpdates;
		this.drawOrder = drawOrder;
	}
}
