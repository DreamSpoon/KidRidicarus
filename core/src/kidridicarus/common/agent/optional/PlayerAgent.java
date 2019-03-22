package kidridicarus.common.agent.optional;

import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.PlayerAgentSupervisor;
import kidridicarus.common.agent.roombox.RoomBox;

public interface PlayerAgent {
	public PlayerAgentSupervisor getSupervisor();
	public RoomBox getCurrentRoom();
	public ObjectProperties getCopyAllProperties();
}
