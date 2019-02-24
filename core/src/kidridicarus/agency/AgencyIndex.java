package kidridicarus.agency;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.change.AgentWrapper;
import kidridicarus.agency.tool.DrawOrder;
import kidridicarus.game.info.GfxInfo;

/*
 * A list of all agents in the agency, with sub-lists available for draw order, agents receiving updates, etc.
 */
public class AgencyIndex {
	private HashMap<Agent, AgentWrapper> allAgents;
	private List<Agent> updateAgents;
	private TreeMap<Float, LinkedList<Object>> drawObjects;

	public AgencyIndex() {
		allAgents = new HashMap<Agent, AgentWrapper>();
		updateAgents = new LinkedList<Agent>();

		drawObjects = new TreeMap<Float, LinkedList<Object>>();
	}

	/*
	 * New agents are created with enableUpdate set to false and drawOrder set to none.
	 */
	public void addAgent(Agent agent) {
		allAgents.put(agent, new AgentWrapper(false, GfxInfo.LayerDrawOrder.NONE));
	}

	public void removeAgent(Agent agent) {
		// remove agent from updates list
		disableAgentUpdate(agent);
		// remove agent from draw order list
		setAgentDrawOrder(agent, GfxInfo.LayerDrawOrder.NONE);
		// remove agent
		allAgents.remove(agent);
		agent.dispose();
	}

	public void enableAgentUpdate(Agent agent) {
		AgentWrapper aw = allAgents.get(agent);
		if(aw == null)
			throw new IllegalArgumentException("Cannot enable update; agent not in list of all agents: " + agent);
		if(aw.receiveUpdates == false) {
			aw.receiveUpdates = true;
			updateAgents.add(agent);
		}
	}

	public void disableAgentUpdate(Agent agent) {
		AgentWrapper aw = allAgents.get(agent);
		if(aw == null)
			throw new IllegalArgumentException("Cannot disable update; agent not in list of all agents: " + agent);
		if(aw.receiveUpdates == true) {
			aw.receiveUpdates = false;
			updateAgents.remove(agent);
		}
	}

	public List<Agent> getAgentsToUpdate() {
		return updateAgents;
	}

	public void setAgentDrawOrder(Agent agent, DrawOrder drawOrder) {
		AgentWrapper aw = allAgents.get(agent);
		if(aw == null)
			throw new IllegalArgumentException("Cannot set draw order; agent not in list of all agents: " + agent);

		// if no change in draw order then exit
		if(aw.drawOrder.equals(drawOrder))
			return;

		// Since new agents start with draw order = NONE,
		// and the preceding if statement would quit this method if draw order isn't changing,
		// then exactly one of the following is true:
		//   The agent is not in a draw order list and must be added, or
		//   the agent is in a draw order list and must be removed, or
		//   the agent is in a draw order list and must be moved to a different list.

		if(aw.drawOrder.equals(GfxInfo.LayerDrawOrder.NONE))
			// the agent is not in a draw order list and must be added
			addToDrawObjects(agent, drawOrder);
		else if(drawOrder.equals(GfxInfo.LayerDrawOrder.NONE))
			// the agent is in a draw order list and must be removed
			removeFromDrawObjects(agent, aw.drawOrder);
		else
			// the agent is in a draw order list and must be moved to a different list
			switchObjectDrawOrder(agent, aw.drawOrder, drawOrder);

		// update the agent wrapper's draw order
		aw.drawOrder = drawOrder;
	}

	public void addMapDrawLayers(TreeMap<DrawOrder, LinkedList<TiledMapTileLayer>> drawLayers) {
		// iterate through each draw order list
		Iterator<Entry<DrawOrder, LinkedList<TiledMapTileLayer>>> drawOrderiter = drawLayers.entrySet().iterator();
		while(drawOrderiter.hasNext()) {
			// then iterate through each list
			Entry<DrawOrder, LinkedList<TiledMapTileLayer>> drawOrderLayerpair = drawOrderiter.next();
			// do not add layers that are not drawn
			// TODO: put these non-drawn layers somewhere, what if they need to be drawn later?
			if(!drawOrderLayerpair.getKey().draw)
				continue;

			Iterator<TiledMapTileLayer> listIter = drawOrderLayerpair.getValue().iterator();
			while(listIter.hasNext()) {
				TiledMapTileLayer layer = listIter.next();
				addToDrawObjects(layer, drawOrderLayerpair.getKey());
			}
		}
	}

	private void addToDrawObjects(Object obj, DrawOrder drawOrder) {
		LinkedList<Object> objList = drawObjects.get(drawOrder.order);
		// if there is not already an element in the tree for given draw order value then create a list
		if(objList == null) {
			objList = new LinkedList<Object>();
			drawObjects.put(drawOrder.order, objList);
		}
		// add the agent to the list for the given draw order  
		objList.add(obj);
	}

	private void removeFromDrawObjects(Object obj, DrawOrder drawOrder) {
		LinkedList<Object> objList = drawObjects.get(drawOrder.order);
		objList.remove(obj);
		// if the list is empty after removing the object then delete the list from it's parent
		if(objList.isEmpty())
			drawObjects.remove(drawOrder.order);
	}

	private void switchObjectDrawOrder(Object obj, DrawOrder oldDO, DrawOrder newDO) {
		// remove agent from it's current list
		removeFromDrawObjects(obj, oldDO);
		addToDrawObjects(obj, newDO);
	}

	public interface DrawObjectIter {
		// return true to stop iterating after current iteration completes
		public boolean iterate(Object obj);
	}

	public void iterateThroughDrawObjects(DrawObjectIter objIter) {
		Iterator<Entry<Float, LinkedList<Object>>> orderIter = drawObjects.entrySet().iterator();
		while(orderIter.hasNext()) {
			Entry<Float, LinkedList<Object>> pair = orderIter.next();
			Iterator<Object> objListIter = pair.getValue().iterator();
			while(objListIter.hasNext()) {
				// call the method passed to this method by way of object iter, stopping iteration if returns true
				if(objIter.iterate(objListIter.next()))
					break;
			}
		}
	}

	public interface AgentIter {
		// return true to stop iterating after current iteration completes
		public boolean iterate(Agent agent);
	}

	/*
	 * See:
	 * https://stackoverflow.com/questions/1066589/iterate-through-a-hashmap
	 */
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
}
