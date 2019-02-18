package kidridicarus.agency.helper;

import kidridicarus.agent.Agent;
import kidridicarus.info.GameInfo.SpriteDrawOrder;

public class AgentDrawOrder {
	Agent agent;
	SpriteDrawOrder drawOrder;

	public AgentDrawOrder(Agent agent, SpriteDrawOrder drawOrder) {
		this.agent = agent;
		this.drawOrder = drawOrder;
	}
}
