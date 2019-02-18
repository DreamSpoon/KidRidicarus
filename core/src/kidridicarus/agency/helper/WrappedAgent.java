package kidridicarus.agency.helper;

import kidridicarus.agent.Agent;
import kidridicarus.info.GameInfo.SpriteDrawOrder;

public class WrappedAgent {
	private Agent agent;
	private boolean receiveUpdates;
	private SpriteDrawOrder drawOrder;

	public WrappedAgent(Agent agent, boolean receiveUpdates, SpriteDrawOrder drawOrder) {
		this.agent = agent;
		this.receiveUpdates = receiveUpdates;
		this.drawOrder = drawOrder;
	}

	public Agent getAgent() {
		return agent;
	}
	
	public boolean isReceiveUpdate() {
		return this.receiveUpdates;
	}

	public SpriteDrawOrder drawOrder() {
		return drawOrder;
	}
}
