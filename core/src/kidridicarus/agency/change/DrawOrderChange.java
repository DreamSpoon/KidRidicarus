package kidridicarus.agency.change;

import kidridicarus.agency.tool.DrawOrder;

public class DrawOrderChange {
	public AgentPlaceholder ap;
	public DrawOrder drawOrder;

	public DrawOrderChange(AgentPlaceholder agent, DrawOrder newDrawOrder) {
		this.ap = agent;
		this.drawOrder = newDrawOrder;
	}
}
