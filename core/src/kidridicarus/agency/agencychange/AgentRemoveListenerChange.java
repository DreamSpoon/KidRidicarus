package kidridicarus.agency.agencychange;

import kidridicarus.agency.agent.AgentRemoveListener;

public class AgentRemoveListenerChange {
	public AgentPlaceholder ap;
	public AgentRemoveListener arListener;
	public boolean add;

	public AgentRemoveListenerChange(AgentPlaceholder ap, AgentRemoveListener arListener, boolean add) {
		this.ap = ap;
		this.arListener = arListener;
		this.add = add;
	}
}
