package kidridicarus.agency.helper;

import kidridicarus.info.GameInfo.SpriteDrawOrder;

class AgentWrapper {
	boolean receiveUpdates;
	SpriteDrawOrder drawOrder;

	AgentWrapper(boolean receiveUpdates, SpriteDrawOrder drawOrder) {
		this.receiveUpdates = receiveUpdates;
		this.drawOrder = drawOrder;
	}
}
