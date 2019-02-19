package kidridicarus.agency.helper;

import kidridicarus.info.GameInfo.SpriteDrawOrder;

/*
 * Extra info about an individual agent that the agent must not be allowed to modify.
 */
class AgentWrapper {
	boolean receiveUpdates;
	SpriteDrawOrder drawOrder;

	AgentWrapper(boolean receiveUpdates, SpriteDrawOrder drawOrder) {
		this.receiveUpdates = receiveUpdates;
		this.drawOrder = drawOrder;
	}
}
