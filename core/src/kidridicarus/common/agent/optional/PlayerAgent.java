package kidridicarus.common.agent.optional;

import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.AgentSupervisor;

public interface PlayerAgent {
	public AgentSupervisor getSupervisor();
	public RoomBox getCurrentRoom();
	public ObjectProperties getCopyAllProperties();
}
