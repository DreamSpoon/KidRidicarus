package kidridicarus.agency;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import kidridicarus.agency.agencychange.AgentWrapper;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AllowOrder;
import kidridicarus.agency.tool.AllowOrderList;
import kidridicarus.agency.tool.AllowOrderList.AllowOrderListIter;

/*
 * A list of all agents in the agency, with sub-lists available for:
 *   -agents receiving updates, by update order
 *   -agents to be drawn, by draw order
 *   -agents which, when removed, will also be disposed
 */
public class AgencyIndex {
	private HashMap<Agent, AgentWrapper> allAgents;
	private AllowOrderList orderedUpdateListeners;
	private HashMap<AgentUpdateListener, AllowOrder> allUpdateListeners;
	private AllowOrderList orderedDrawListeners;
	private HashMap<AgentDrawListener, AllowOrder> allDrawListeners;
	private HashSet<DisposableAgent> disposeAgents;

	public AgencyIndex() {
		allAgents = new HashMap<Agent, AgentWrapper>();
		orderedUpdateListeners = new AllowOrderList();
		allUpdateListeners = new HashMap<AgentUpdateListener, AllowOrder>();
		orderedDrawListeners = new AllowOrderList();
		allDrawListeners = new HashMap<AgentDrawListener, AllowOrder>();
		disposeAgents = new HashSet<DisposableAgent>();
	}

	/*
	 * Add an Agent to the index.
	 */
	public void addAgent(Agent agent) {
		allAgents.put(agent, new AgentWrapper());
		// if disposable, then save to list for disposal on agent remove
		if(agent instanceof DisposableAgent)
			disposeAgents.add((DisposableAgent) agent);
	}

	/*
	 * Remove an Agent from the index, calling disposeAgent if needed.
	 */
	// ignore unlikely arg type because arg type is checked by instanceof
	@SuppressWarnings("unlikely-arg-type")
	public void removeAgent(Agent agent) {
		// remove agent from updates list
		removeAgentUpdateListeners(agent);
		// remove agent from draw order list
		removeAgentDrawListeners(agent);
		// remove agent from all agents list
		allAgents.remove(agent);
		// Dispose agent if needed. Call this last because it removes ambiguity re:
		//   Should agent set its draw order to none and disable its updates on disposal?
		//     No, it should not. That functionality will be handed here.
		if(agent instanceof DisposableAgent && disposeAgents.contains(agent)) {
			((DisposableAgent) agent).disposeAgent();
			disposeAgents.remove(agent);
		}
	}

	/*
	 * Add a single update listener and associate it with the given Agent.
	 */
	public void addUpdateListener(Agent agent, AllowOrder updateOrder, AgentUpdateListener auListener) {
		// if updates not allowed then no listener needed! I am Error
		if(!updateOrder.allow)
			throw new IllegalArgumentException("Cannot add update listener with updateOrder.allow==false.");
		AgentWrapper aWrapper = allAgents.get(agent);
		if(aWrapper == null) {
			throw new IllegalArgumentException(
					"Cannot add update listener; agent not in list of all agents: " + agent);
		}
		if(allUpdateListeners.containsKey(auListener)) {
			throw new IllegalArgumentException(
					"Cannot add update listener; listener has already been added: " + auListener);
		}

		allUpdateListeners.put(auListener, updateOrder);
		orderedUpdateListeners.add(auListener, updateOrder);

		// keep track of the agent's update listeners
		aWrapper.updateListeners.add(auListener);
	}

	/*
	 * Remove a single update listener associated with the given Agent.
	 */
	public void removeUpdateListener(Agent agent, AgentUpdateListener auListener) {
		AgentWrapper aWrapper = allAgents.get(agent);
		if(aWrapper == null) {
			throw new IllegalArgumentException(
					"Cannot remove update listener; agent not in list of all agents: " + agent);
		}
		if(!allUpdateListeners.containsKey(auListener)) {
			throw new IllegalArgumentException(
					"Cannot remove update listener; listener was not added: " + auListener);
		}

		// Get the current update order for the listener...
		AllowOrder currentOrder = allUpdateListeners.get(auListener);
		// ... to find and remove the listener from the ordered tree/hashsets. 
		orderedUpdateListeners.change(auListener, currentOrder, AllowOrder.NOT_ALLOWED);
		// remove the listener from the list of all listeners
		allUpdateListeners.remove(auListener);
		// and remove the listener from the Agent's list of listeners
		aWrapper.updateListeners.remove(auListener);
	}

	/*
	 * Remove all update listeners associated with the given Agent.
	 */
	private void removeAgentUpdateListeners(Agent agent) {
		AgentWrapper aWrapper = allAgents.get(agent);
		if(aWrapper == null) {
			throw new IllegalArgumentException(
					"Cannot remove update listener; agent not in list of all agents: " + agent);
		}
		for(AgentUpdateListener aul : aWrapper.updateListeners) {
			// remove the listener from the ordered treeset/hashsets
			orderedUpdateListeners.change(aul, allUpdateListeners.get(aul), AllowOrder.NOT_ALLOWED);
			// remove the listener from the hash map of listeners and draw orders
			allUpdateListeners.remove(aul);
		}
		aWrapper.updateListeners.clear();
	}

	/*
	 * Add a single draw listener and associate it with the given Agent.
	 */
	public void addDrawListener(Agent agent, AllowOrder drawOrder, AgentDrawListener adListener) {
		// if draw not allowed then no listener needed! I am Error
		if(!drawOrder.allow)
			throw new IllegalArgumentException("Cannot add draw listener with drawOrder.allow==false.");
		AgentWrapper aWrapper = allAgents.get(agent);
		if(aWrapper == null) {
			throw new IllegalArgumentException(
					"Cannot add draw listener; agent not in list of all agents: " + agent);
		}
		if(allDrawListeners.containsKey(adListener)) {
			throw new IllegalArgumentException(
					"Cannot add draw listener; listener has already been added: " + adListener);
		}

		allDrawListeners.put(adListener, drawOrder);
		orderedDrawListeners.add(adListener, drawOrder);

		// keep track of the agent's draw listeners
		aWrapper.drawListeners.add(adListener);
	}

	/*
	 * Remove a single draw listener associated with the given Agent.
	 */
	public void removeDrawListener(Agent agent, AgentDrawListener adListener) {
		AgentWrapper aWrapper = allAgents.get(agent);
		if(aWrapper == null) {
			throw new IllegalArgumentException(
					"Cannot remove draw listener; agent not in list of all agents: " + agent);
		}
		if(!allDrawListeners.containsKey(adListener)) {
			throw new IllegalArgumentException(
					"Cannot remove draw listener; listener was not added: " + adListener);
		}

		// Get the current draw order for the listener...
		AllowOrder currentOrder = allDrawListeners.get(adListener);
		// ... to find and remove the listener from the ordered tree/hashsets. 
		orderedDrawListeners.change(adListener, currentOrder, AllowOrder.NOT_ALLOWED);
		// remove the listener from the list of all listeners
		allDrawListeners.remove(adListener);
		// and remove the listener from the Agent's list of listeners
		aWrapper.drawListeners.remove(adListener);
	}

	/*
	 * Remove all draw listeners associated with the given Agent.
	 */
	private void removeAgentDrawListeners(Agent agent) {
		AgentWrapper aWrapper = allAgents.get(agent);
		if(aWrapper == null) {
			throw new IllegalArgumentException(
					"Cannot remove draw listener; agent not in list of all agents: " + agent);
		}
		for(AgentDrawListener adl : aWrapper.drawListeners) {
			// remove the listener from the ordered treeset/hashsets
			orderedDrawListeners.change(adl, allDrawListeners.get(adl), AllowOrder.NOT_ALLOWED);
			// remove the listener from the hash map of listeners and draw orders
			allDrawListeners.remove(adl);
		}
		aWrapper.drawListeners.clear();
	}

	/*
	 * See:
	 * https://stackoverflow.com/questions/1066589/iterate-through-a-hashmap
	 */
	public interface AgentIter {
		// return true to stop iterating after current iteration completes
		public boolean iterate(Agent agent);
	}
	public void iterateThroughAllAgents(AgentIter agentIter) {
		Iterator<Map.Entry<Agent, AgentWrapper>> iter = allAgents.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<Agent, AgentWrapper> pair = iter.next();
			Agent agent = pair.getKey();
			// call the method passed to this method by way of agent iter, stopping iteration if returns true
			if(agentIter.iterate(agent))
				break;
		}
	}

	public void iterateThroughUpdateAgents(AllowOrderListIter uoli) {
		orderedUpdateListeners.iterateList(uoli);
	}

	public void iterateThroughDrawListeners(AllowOrderListIter doli) {
		orderedDrawListeners.iterateList(doli);
	}

	public void disposeAndRemoveAllAgents() {
		for(DisposableAgent agent : disposeAgents)
			agent.disposeAgent();
		// clear agent and agent related lists, reseting this object back to constructor finish state
		allAgents.clear();
		orderedUpdateListeners.clear();
		allUpdateListeners.clear();
		orderedDrawListeners.clear();
		allDrawListeners.clear();
		disposeAgents.clear();
	}
}
