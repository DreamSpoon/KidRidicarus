package kidridicarus.common.agentbrain;

import kidridicarus.common.agent.roombox.RoomBox;

public class RoomingBrainFrameInput extends BrainFrameInput {
	public RoomBox roomBox;
	public boolean isContactKeepAlive;
	public boolean isContactDespawn;
	public RoomingBrainFrameInput(float timeDelta, RoomBox roomBox, boolean isContactKeepAlive,
			boolean isContactDespawn) {
		super(timeDelta);
		this.roomBox = roomBox;
		this.isContactKeepAlive = isContactKeepAlive;
		this.isContactDespawn = isContactDespawn;
	}
}
