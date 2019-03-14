package kidridicarus.common.agent.optional;

import kidridicarus.common.agent.GameAgentObserver;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agent.AgentSupervisor;

public interface PlayerAgent {
	public GameAgentObserver getObserver();
	public AgentSupervisor getSupervisor();
	public RoomBox getCurrentRoom();
}
