package kidridicarus.agency.agent.optional;

import kidridicarus.agency.agent.AgentObserverPlus;
import kidridicarus.agency.agent.AgentSupervisor;
import kidridicarus.agency.agent.general.Room;

public interface PlayerAgent {
	public AgentObserverPlus getObserver();
	public AgentSupervisor getSupervisor();
	public Room getCurrentRoom();
	public boolean isDead();
	public boolean isAtLevelEnd();
	public float getStateTimer();
}
