package kidridicarus.agency.agencychange;

import kidridicarus.common.tool.AllowOrder;

public class DrawOrderChange {
	public AgentPlaceholder ap;
	public AllowOrder drawOrder;

	public DrawOrderChange(AgentPlaceholder agent, AllowOrder newDrawOrder) {
		this.ap = agent;
		this.drawOrder = newDrawOrder;
	}
}
