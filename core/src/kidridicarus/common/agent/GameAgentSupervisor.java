package kidridicarus.common.agent;

import kidridicarus.agency.agent.AgentSupervisor;

public abstract class GameAgentSupervisor extends AgentSupervisor {
	public abstract boolean isSwitchToOtherChar();
	public abstract String getNextLevelName();
}
