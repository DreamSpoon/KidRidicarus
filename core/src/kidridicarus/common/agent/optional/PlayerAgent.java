package kidridicarus.common.agent.optional;

import kidridicarus.common.agent.GameAgentObserver;
import kidridicarus.common.agent.AgentSupervisor;
import kidridicarus.common.agent.general.Room;

public interface PlayerAgent {
	public GameAgentObserver getObserver();
	public AgentSupervisor getSupervisor();
	public Room getCurrentRoom();
}
