package kidridicarus.agency.agencychange;

import java.util.LinkedList;
import java.util.List;

import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;

/*
 * Extra info about an individual agent. This information is to be used exclusively by the Agency class. 
 */
public class AgentWrapper {
	public List<AgentUpdateListener> updateListeners;
	public List<AgentDrawListener> drawListeners;

	// the listeners created by this Agent, to listen for removal of other Agents
	public List<AgentRemoveListener> myAgentRemoveListeners;
	// the listeners create by other Agents, which are listening for removal of this Agent
	public List<AgentRemoveListener> otherAgentRemoveListeners;

	public AgentWrapper() {
		updateListeners = new LinkedList<AgentUpdateListener>();
		drawListeners = new LinkedList<AgentDrawListener>();
		myAgentRemoveListeners = new LinkedList<AgentRemoveListener>();
		otherAgentRemoveListeners = new LinkedList<AgentRemoveListener>();
	}
}
