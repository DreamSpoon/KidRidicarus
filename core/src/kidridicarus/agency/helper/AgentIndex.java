package kidridicarus.agency.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kidridicarus.agent.Agent;
import kidridicarus.info.GameInfo.SpriteDrawOrder;

public class AgentIndex {
	private HashMap<Agent, AgentWrapper> allAgents;
	private List<Agent> updateAgents;
	private List<Agent>[] drawAgents;

	@SuppressWarnings("unchecked")
	public AgentIndex() {
		allAgents = new HashMap<Agent, AgentWrapper>();
		updateAgents = new LinkedList<Agent>();

		drawAgents = (LinkedList<Agent>[]) new LinkedList[SpriteDrawOrder.values().length];
		for(int i=0; i<SpriteDrawOrder.values().length; i++)
			drawAgents[i] = new LinkedList<Agent>();
	}

	/*
	 * New agents are created with enableUpdate set to false and drawOrder set to none.
	 */
	public void addAgent(Agent agent) {
		allAgents.put(agent, new AgentWrapper(false, SpriteDrawOrder.NONE));
	}

	public void removeAgent(Agent agent) {
		// remove agent from updates list
		disableAgentUpdate(agent);
		// remove agent from draw order list
		setAgentDrawOrder(agent, SpriteDrawOrder.NONE);
		// remove agent
		allAgents.remove(agent);
		agent.dispose();
	}

	public void enableAgentUpdate(Agent agent) {
		AgentWrapper aw = allAgents.get(agent);
		if(aw == null)
			throw new IllegalArgumentException("Agent does not exist in list of all agents: " + agent);
		if(aw.receiveUpdates == false) {
			aw.receiveUpdates = true;
			updateAgents.add(agent);
		}
	}

	public void disableAgentUpdate(Agent agent) {
		AgentWrapper aw = allAgents.get(agent);
		if(aw == null)
			throw new IllegalArgumentException("Agent does not exist in list of all agents: " + agent);
		if(aw.receiveUpdates == true) {
			aw.receiveUpdates = false;
			updateAgents.remove(agent);
		}
	}

	public List<Agent> getUpdateList() {
		return updateAgents;
	}

	public void setAgentDrawOrder(Agent agent, SpriteDrawOrder drawOrder) {
		AgentWrapper aw = allAgents.get(agent);
		// if no change in draw order then exit
		if(aw.drawOrder == drawOrder)
			return;

		// if draw order is none, then the agent will not be drawn, and must be removed if in a draw list
		if(drawOrder == SpriteDrawOrder.NONE) {
			// if the sprite is already in a draw order list then remove it from it's list
			if(aw.drawOrder != SpriteDrawOrder.NONE) {
				drawAgents[aw.drawOrder.ordinal()].remove(agent);
				aw.drawOrder = SpriteDrawOrder.NONE;
			}
		}
		// agent is to be drawn, so switch from one list to another, or just add
		else {
			// if the agent is in a draw order list, then remove the agent from it's current draw order list
			if(aw.drawOrder != SpriteDrawOrder.NONE)
				drawAgents[aw.drawOrder.ordinal()].remove(agent);

			// add agent to new draw order list
			drawAgents[drawOrder.ordinal()].add(agent);
			aw.drawOrder = drawOrder;
		}
	}

	public Collection<Agent>[] getAgentsToDraw() {
		return drawAgents;
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
		Iterator<Map.Entry<Agent, AgentWrapper>> it = allAgents.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<Agent, AgentWrapper> pair = it.next();
			Agent agent = pair.getKey();
			// call the method passed to this method by way of agent iter, stopping iteration if returns true
			if(agentIter.iterate(agent))
				break;
		}
	}
}
