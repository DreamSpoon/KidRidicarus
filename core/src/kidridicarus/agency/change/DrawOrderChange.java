package kidridicarus.agency.change;

import kidridicarus.tool.DrawOrder;

public class DrawOrderChange {
	public AgentPlaceholder ap;
	public DrawOrder drawOrder;

	public DrawOrderChange(AgentPlaceholder agent, DrawOrder newDrawOrder) {
		this.ap = agent;
		this.drawOrder = newDrawOrder;
	}
}
