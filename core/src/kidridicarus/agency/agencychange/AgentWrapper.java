package kidridicarus.agency.agencychange;

import java.util.LinkedList;

import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;

/*
 * Extra info about an individual agent. This information is to be used exclusively by the Agency class. 
 */
public class AgentWrapper {
	public LinkedList<AgentUpdateListener> updateListeners;
	public LinkedList<AgentDrawListener> drawListeners;

	public AgentWrapper() {
		updateListeners = new LinkedList<AgentUpdateListener>();
		drawListeners = new LinkedList<AgentDrawListener>();
	}
}
