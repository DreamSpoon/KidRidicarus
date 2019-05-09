package kidridicarus.agency;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentPropertyListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.AllowOrder;
import kidridicarus.agency.tool.AllowOrderList;
import kidridicarus.agency.tool.AllowOrderList.AllowOrderListIter;

/*
 * A list of all Agents in the Agency, and their associated update listeners and draw listeners. Agents can have
 * remove listeners via their AgentWrapper, which receive callback when the Agent is removed from Agency.
 *
 * TODO Agent property listeners should be local by default (Agents create their own property listeners), and can
 * elevate a property to global status so the Agent can be searched by the elevated property (the property string
 * is elevated, not the listener).
 */
class AgencyIndex {
	private HashSet<Agent> allAgents;
	private AllowOrderList orderedUpdateListeners;
	private HashMap<AgentUpdateListener, AllowOrder> allUpdateListeners;
	private AllowOrderList orderedDrawListeners;
	private HashMap<AgentDrawListener, AllowOrder> allDrawListeners;
	// sub-lists of Agents that have properties, indexed by property String
	private HashMap<String, LinkedList<Agent>> globalPropertyKeyAgents;

	AgencyIndex() {
		allAgents = new HashSet<Agent>();
		orderedUpdateListeners = new AllowOrderList();
		allUpdateListeners = new HashMap<AgentUpdateListener, AllowOrder>();
		orderedDrawListeners = new AllowOrderList();
		allDrawListeners = new HashMap<AgentDrawListener, AllowOrder>();
		globalPropertyKeyAgents = new HashMap<String, LinkedList<Agent>>();
	}

	/*
	 * Add an Agent to the index.
	 */
	public void addAgent(Agent agent) {
		allAgents.add(agent);
	}

	/*
	 * Remove an Agent from the index, calling remove listeners if needed.
	 * If remove listeners are present, then they must be called first to prevent altering the rest of the Agent's
	 * state by way of removing draw, update, etc. listeners.
	 */
	public void removeAgent(Agent agent) {
		// if other agents are listening for remove callback then do it, and garbage collect remove listeners
		removeAllAgentRemoveListeners(agent);
		// remove agent from updates list
		removeAllUpdateListeners(agent);
		// remove agent from draw order list
		removeAllDrawListeners(agent);
		// remove property listeners if necessary
		removeAllAgentPropertyListeners(agent);
		// remove agent from all agents list
		allAgents.remove(agent);
	}

	/*
	 * Add a single update listener and associate it with the given Agent.
	 */
	public void addUpdateListener(Agent agent, AgentUpdateListener auListener, AllowOrder updateOrder) {
		// if updates not allowed then no listener needed! I am Error
		if(!updateOrder.allow)
			throw new IllegalArgumentException("Cannot add update listener with updateOrder.allow==false.");
		if(allUpdateListeners.containsKey(auListener)) {
			throw new IllegalArgumentException(
					"Cannot add update listener; listener has already been added: " + auListener);
		}
		// add listener to list of all update listeners
		allUpdateListeners.put(auListener, updateOrder);
		orderedUpdateListeners.add(auListener, updateOrder);
		// add listener to agent
		agent.updateListeners.add(auListener);
	}

	/*
	 * Remove a single update listener associated with the given Agent.
	 */
	public void removeUpdateListener(Agent agent, AgentUpdateListener auListener) {
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
		agent.updateListeners.remove(auListener);
	}

	/*
	 * Remove all update listeners associated with the given Agent.
	 */
	private void removeAllUpdateListeners(Agent agent) {
		for(AgentUpdateListener aul : agent.updateListeners) {
			// remove the listener from the ordered treeset/hashsets
			orderedUpdateListeners.change(aul, allUpdateListeners.get(aul), AllowOrder.NOT_ALLOWED);
			// remove the listener from the hash map of listeners and draw orders
			allUpdateListeners.remove(aul);
		}
		agent.updateListeners.clear();
	}

	/*
	 * Add a single draw listener and associate it with the given Agent.
	 */
	public void addDrawListener(Agent agent, AgentDrawListener adListener, AllowOrder drawOrder) {
		// if draw not allowed then no listener needed! I am Error
		if(!drawOrder.allow)
			throw new IllegalArgumentException("Cannot add draw listener with drawOrder.allow==false.");
		if(allDrawListeners.containsKey(adListener)) {
			throw new IllegalArgumentException(
					"Cannot add draw listener; listener has already been added: " + adListener);
		}
		// add listener to list of all draw listeners
		allDrawListeners.put(adListener, drawOrder);
		orderedDrawListeners.add(adListener, drawOrder);
		// add listener to agent
		agent.drawListeners.add(adListener);
	}

	/*
	 * Remove a single draw listener associated with the given Agent.
	 */
	public void removeDrawListener(Agent agent, AgentDrawListener adListener) {
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
		agent.drawListeners.remove(adListener);
	}

	/*
	 * Remove all draw listeners associated with the given Agent.
	 */
	private void removeAllDrawListeners(Agent agent) {
		for(AgentDrawListener adl : agent.drawListeners) {
			// remove the listener from the ordered treeset/hashsets
			orderedDrawListeners.change(adl, allDrawListeners.get(adl), AllowOrder.NOT_ALLOWED);
			// remove the listener from the hash map of listeners and draw orders
			allDrawListeners.remove(adl);
		}
		agent.drawListeners.clear();
	}

	/*
	 * Associate a single listener with the given agent and other Agent. An Agent is allowed to add a remove
	 * listener to itself. This is a good way to handle "dispose" functionality.
	 * Note: AgencyIndex doesn't directly keep a list of remove listeners, each AgentWrapper keeps a its own list.
	 */
	public void addAgentRemoveListener(Agent agent, AgentRemoveListener arListener) {
		// This Agent keeps a ref to the listener, so that this Agent can delete the listener when this Agent
		// is removed (garbage collection).
		agent.myAgentRemoveListeners.add(arListener);
		// The other Agent keeps a ref to the listener, so that the other Agent can callback this Agent when
		// other Agent is removed (agent removal callback).
		arListener.getOtherAgent().otherAgentRemoveListeners.add(arListener);
	}

	/*
	 * Disassociate a single listener from the given agent and other Agent.
	 */
	public void removeAgentRemoveListener(Agent agent, AgentRemoveListener arListener) {
		agent.myAgentRemoveListeners.remove(arListener);
		arListener.getOtherAgent().otherAgentRemoveListeners.remove(arListener);
	}

	/*
	 * 1) Agent removal callbacks,
	 * 2) Disassociate all listeners associated with the given Agent,
	 * 3) Ensuring other Agent's references to these listeners are also disassociated. 
	 */
	private void removeAllAgentRemoveListeners(Agent agent) {
		// first, do all Agent removal callbacks (the other Agents are listening for this Agent's removal)
		for(AgentRemoveListener otherListener : agent.otherAgentRemoveListeners) {
			otherListener.preRemoveAgent();
			// since the move listener has been called, the listener itself must now be disassociated from other Agent
			otherListener.getListeningAgent().myAgentRemoveListeners.remove(otherListener);
		}
		// second, remove all of my listener references held by other agents
		for(AgentRemoveListener myListener : agent.myAgentRemoveListeners) {
			myListener.getOtherAgent().otherAgentRemoveListeners.remove(myListener);
		}
		// third, remove all of my listeners
		agent.myAgentRemoveListeners.clear();
	}

	public void addAgentPropertyListener(Agent agent, AgentPropertyListener<?> listener, String propertyKey,
			Boolean isGlobal) {
		// if the property is global then add the property key to a global list
		if(isGlobal) {
			LinkedList<Agent> subList;
			// If the property String isn't in the global list, then create an empty sub-list and add the new sub-list
			// to the global list.
			if(!globalPropertyKeyAgents.containsKey(propertyKey)) {
				subList = new LinkedList<Agent>();
				globalPropertyKeyAgents.put(propertyKey, subList);
			}
			// otherwise get existing sub-list
			else
				subList = globalPropertyKeyAgents.get(propertyKey);
			// add agent to sub-list for this property String, and associate the property listener with agent
			subList.add(agent);
			// keep a list of Agent property keys are global, for removal purposes
			agent.globalPropertyKeys.add(propertyKey);
		}
		// add the property to the Agent locally
		agent.propertyListeners.put(propertyKey, listener);
	}

	public void removeAgentPropertyListener(Agent agent, String propertyKey) {
		// if the property is a global property then remove it from the global property list
		if(agent.globalPropertyKeys.contains(propertyKey)) {
			// if the property String isn't in the global list, then throw exception
			if(!globalPropertyKeyAgents.containsKey(propertyKey)) {
				throw new IllegalArgumentException("Cannot remove listener for property=("+propertyKey+
						") from agent="+agent);
			}
			// remove agent from the property sub-list
			LinkedList<Agent> subList = globalPropertyKeyAgents.get(propertyKey);
			subList.remove(agent);
			// remove the sub-list if it is empty, to prevent accumulating empty lists
			if(subList.isEmpty())
				globalPropertyKeyAgents.remove(propertyKey);
			// remove the key from the Agent's list of global property keys
			agent.globalPropertyKeys.remove(propertyKey);
		}
		// remove property listener from agent locally
		agent.propertyListeners.remove(propertyKey);
	}

	// remove all property listeners associated with agent
	private void removeAllAgentPropertyListeners(Agent agent) {
		// first, remove agent from all the sub-lists that link to it via its global property Strings
		for(String propertyKey : agent.globalPropertyKeys) {
			LinkedList<Agent> subList = globalPropertyKeyAgents.get(propertyKey);
			subList.remove(agent);
			// remove the sub-list if it is empty, to prevent accumulating empty lists
			if(subList.isEmpty())
				globalPropertyKeyAgents.remove(propertyKey);
		}
		// last, remove all property keys and listeners from agent
		agent.globalPropertyKeys.clear();
		agent.propertyListeners.clear();
	}

	/*
	 * Search all Agents with properties for matches against the given properties, return list of matching Agents.
	 * Does not return null, will return empty list if needed.
	 * Note: The Agents in the returned list may have extra properties as well, not just the given properties -
	 * this search is an inclusive search.
	 */
	public LinkedList<Agent> getAgentsByProperties(String[] keys, Object[] vals, boolean firstOnly) {
		if(keys.length != vals.length)
			throw new IllegalArgumentException("keys[] and vals[] arrays are not of equal length.");
		// if search keys array is empty then return empty list
		LinkedList<Agent> ret = new LinkedList<Agent>();
		if(keys.length == 0)
			return ret;
		// find the first search property key that exists in the global list
		String matchingKey = null;
		for(String key : keys) {
			if(globalPropertyKeyAgents.containsKey(key)) {
				matchingKey = key;
				break;
			}
		}
		// If a list does not yet exist for any of the search property keys, then return empty list since zero
		// Agents have global property keys matching search criteria.
		if(matchingKey == null)
			return ret;
		// Check the sub-list for Agents that match the given properties (test against only the search properties
		// for matches).
		// TODO Iterate through shortest available sub-list, instead of defaulting to first sub-list.
		for(Agent agent : globalPropertyKeyAgents.get(matchingKey)) {
			if(isPropertiesMatch(agent.propertyListeners, keys, vals)) {
				ret.add(agent);
				if(firstOnly)
					return ret;
			}
		}
		// Return list of Agents whose properties:
		//   -include at least all the given properties
		//   -with values that match all the given search values
		return ret;
	}

	private boolean isPropertiesMatch(HashMap<String, AgentPropertyListener<?>> propertyListeners, String[] keys,
			Object[] vals) {
		for(int i=0; i<keys.length; i++) {
			// if listener doesn't exist for key then return false, because Agent doesn't have one of the properties
			AgentPropertyListener<?> listener = propertyListeners.get(keys[i]);
			if(listener == null) {
				return false;
			}
			// If the given value is null, and the listener returns non-null, then return false due to mismatch, or
			// if the value returned by the listener does not match given value, then return false due to mismatch.
			Object listenerVal = listener.getValue();
			if(listenerVal == null) {
				if(vals[i] != null) {
					return false;
				}
			}
			else if(!listenerVal.equals(vals[i])) {
				return false;
			}
		}
		// return true because all search properties match
		return true;
	}

	public void iterateThroughUpdateListeners(AllowOrderListIter uoli) {
		orderedUpdateListeners.iterateList(uoli);
	}

	public void iterateThroughDrawListeners(AllowOrderListIter doli) {
		orderedDrawListeners.iterateList(doli);
	}

	/*
	 * Batch remove all Agents from Agency, calling Agent remove listeners as needed.
	 * Remove listeners are called first to prevent changing Agent's state until all remove listeners are called.
	 * See:
	 * https://stackoverflow.com/questions/1066589/iterate-through-a-hashmap
	 */
	public void removeAllAgents() {
		for(Agent agent : allAgents) {
			removeAllAgentRemoveListeners(agent);
			agent.updateListeners.clear();
			agent.drawListeners.clear();
			agent.globalPropertyKeys.clear();
			agent.propertyListeners.clear();
		}
		orderedUpdateListeners.clear();
		allUpdateListeners.clear();
		orderedDrawListeners.clear();
		allDrawListeners.clear();
		// clear all sub-lists within allPropertyAgents
		Iterator<Entry<String, LinkedList<Agent>>> iter = globalPropertyKeyAgents.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, LinkedList<Agent>> pair = iter.next();
			LinkedList<Agent> subList = pair.getValue();
			subList.clear();
		}
		globalPropertyKeyAgents.clear();
		allAgents.clear();
	}
}
