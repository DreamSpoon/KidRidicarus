package kidridicarus.common.agent.optional;

import kidridicarus.agency.agent.AgentSupervisor;
import kidridicarus.common.agent.AgentObserverPlus;
import kidridicarus.common.agent.general.Room;

public interface PlayerAgent {
	public AgentObserverPlus getObserver();
	public AgentSupervisor getSupervisor();
	public Room getCurrentRoom();
	public boolean isDead();
	public boolean isAtLevelEnd();
	public float getStateTimer();
}
