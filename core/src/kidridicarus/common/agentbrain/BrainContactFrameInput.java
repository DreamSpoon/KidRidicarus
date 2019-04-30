package kidridicarus.common.agentbrain;

import kidridicarus.common.agent.roombox.RoomBox;

/*
 * Despawn boxes can exist independently of keep alive boxes.
 */
public class BrainContactFrameInput {
	public RoomBox room;
	public boolean isKeepAlive;
	public boolean isDespawn;

	public BrainContactFrameInput(RoomBox room, boolean isKeepAlive, boolean isDespawn) {
		this.room = room;
		this.isKeepAlive = isKeepAlive;
		this.isDespawn = isDespawn;
	}
}
