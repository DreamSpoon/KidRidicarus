package kidridicarus.agency.agencychange;

import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.tool.AllowOrder;

public class DrawListenerChange {
	public AgentPlaceholder ap;
	public AllowOrder drawOrder;
	public AgentDrawListener adListener;
	public boolean add;

	public DrawListenerChange(AgentPlaceholder ap, AllowOrder newDrawOrder, AgentDrawListener adListener,
			boolean add) {
		this.ap = ap;
		this.drawOrder = newDrawOrder;
		this.adListener = adListener;
		this.add = add;
	}
}
