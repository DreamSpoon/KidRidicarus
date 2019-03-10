package kidridicarus.agency;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import kidridicarus.agency.agencychange.AgentWrapper;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agent.DrawableAgent;
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
//	private AllowOrderList orderedUpdateAgents;
	private AllowOrderList orderedUpdateListeners;
	private HashMap<AgentUpdateListener, AllowOrder> allUpdateListeners;
	private AllowOrderList drawObjects;
	private HashSet<DisposableAgent> disposeAgents;

	public AgencyIndex() {
		allAgents = new HashMap<Agent, AgentWrapper>();
//		orderedUpdateAgents = new AllowOrderList();
		orderedUpdateListeners = new AllowOrderList();
		allUpdateListeners = new HashMap<AgentUpdateListener, AllowOrder>();
		drawObjects = new AllowOrderList();
		disposeAgents = new HashSet<DisposableAgent>();
	}

	/*
	 * New agents are created with enableUpdate set to false and drawOrder set to none.
	 */
	public void addAgent(Agent agent) {
		allAgents.put(agent, new AgentWrapper());
		// if disposable, then save to list for disposal on agent remove
		if(agent instanceof DisposableAgent)
			disposeAgents.add((DisposableAgent) agent);
	}

	// ignore unlikely arg type because arg type is checked by instanceof
	@SuppressWarnings("unlikely-arg-type")
	public void removeAgent(Agent agent) {
		// remove agent from updates list
//		setAgentUpdateOrderNone(agent);
		removeAgentUpdateListeners(agent);
		// remove agent from draw order list
		setAgentDrawOrderNone(agent);
		// remove agent from all agents list
		allAgents.remove(agent);
		// Dispose agent if needed. Call this last because it removes ambiguity re:
		//   Should agent set its draw order to none and disable its updates on disposal?
		//     No, it should not.
		if(agent instanceof DisposableAgent && disposeAgents.contains(agent)) {
			((DisposableAgent) agent).disposeAgent();
			disposeAgents.remove(agent);
		}
	}

	/*	public void setAgentUpdateOrder(Agent agent, AllowOrder newUpdateOrder) {
		if(!(agent instanceof UpdatableAgent)) {
			throw new IllegalArgumentException(
					"Cannot set draw order; agent not instance of UpdatableAgent: " + agent);
		}
		AgentWrapper aWrapper = allAgents.get(agent);
		if(aWrapper == null) {
			throw new IllegalArgumentException(
					"Cannot set draw order; agent not in list of all agents: " + agent);
		}
		orderedUpdateAgents.change(agent, aWrapper.updateOrder, newUpdateOrder);
		// update the agent wrapper's draw order
		aWrapper.updateOrder = newUpdateOrder;
	}

	public void setAgentUpdateOrderNone(Agent agent) {
		setAgentUpdateOrder(agent, new AllowOrder(false, 0f));
	}
*/
	public void setAgentDrawOrder(Agent agent, AllowOrder newDrawOrder) {
		if(!(agent instanceof DrawableAgent)) {
			throw new IllegalArgumentException(
					"Cannot set draw order; agent not instance of DrawableAgent: " + agent);
		}
		AgentWrapper aWrapper = allAgents.get(agent);
		if(aWrapper == null) {
			throw new IllegalArgumentException(
					"Cannot set draw order; agent not in list of all agents: " + agent);
		}

		drawObjects.change(agent, aWrapper.drawOrder, newDrawOrder);
		// update the agent wrapper's draw order
		aWrapper.drawOrder = newDrawOrder;
	}

	public void setAgentDrawOrderNone(Agent agent) {
		setAgentDrawOrder(agent, AllowOrder.NOT_ALLOWED);
	}

/*	public void addMapDrawLayers(TreeMap<AllowOrder, LinkedList<TiledMapTileLayer>> drawLayers) {
		// iterate through each draw order list
		Iterator<Entry<AllowOrder, LinkedList<TiledMapTileLayer>>> drawOrderiter = drawLayers.entrySet().iterator();
		while(drawOrderiter.hasNext()) {
			// then iterate through each list
			Entry<AllowOrder, LinkedList<TiledMapTileLayer>> drawOrderLayerpair = drawOrderiter.next();
			// do not add layers that are not drawn
			// TODO: put these non-drawn layers somewhere, what if they need to be drawn later?
			if(!drawOrderLayerpair.getKey().allow)
				continue;

			Iterator<TiledMapTileLayer> listIter = drawOrderLayerpair.getValue().iterator();
			while(listIter.hasNext()) {
				TiledMapTileLayer layer = listIter.next();
				drawObjects.add(layer, drawOrderLayerpair.getKey());
			}
		}
	}
*/
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

	public void iterateThroughDisposableAgents(AgentIter agentIter) {
		for(DisposableAgent agent : disposeAgents) {
			// call the method passed to this method by way of agent iter, stopping iteration if returns true
			if(agentIter.iterate((Agent) agent))
				break;
		}
	}

	public void iterateThroughDrawObjects(AllowOrderListIter doi) {
		drawObjects.iterateList(doi);
	}

//	public void iterateThroughUpdateAgents(AllowOrderListIter doi) {
//		orderedUpdateAgents.iterateList(doi);
//	}

	public void addUpdateListener(Agent agent, AllowOrder updateOrder, AgentUpdateListener auListener) {
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
		// and remove the listener from the agents list of listeners
		aWrapper.updateListeners.remove(auListener);
	}

	public void iterateThroughUpdateAgents(AllowOrderListIter uoi) {
		orderedUpdateListeners.iterateList(uoi);
	}

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
}
