package kidridicarus.agency.helper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import kidridicarus.agent.Agent;
import kidridicarus.info.GameInfo.SpriteDrawOrder;

/*
 * Wrapper for a draw order array of lists of Agents, with LinkedBlockingQueue and a required process() method.
 */
public class AgentDrawOrderLists {
	private LinkedList<Agent>[] drawAgents;
	private LinkedBlockingQueue<AgentDrawOrder> drawOrderChangeQ;

	@SuppressWarnings("unchecked")
	public AgentDrawOrderLists() {
		drawAgents = (LinkedList<Agent>[]) new LinkedList[SpriteDrawOrder.values().length];
		for(int i=0; i<SpriteDrawOrder.values().length; i++)
			drawAgents[i] = new LinkedList<Agent>();
		drawOrderChangeQ = new LinkedBlockingQueue<AgentDrawOrder>();
	}

	public void setAgentDrawOrder(Agent agent, SpriteDrawOrder order) {
		drawOrderChangeQ.add(new AgentDrawOrder(agent, order));
	}

	public void process() {
		while(!drawOrderChangeQ.isEmpty())
			doSetAgentDrawOrder(drawOrderChangeQ.poll());
	}

	private void doSetAgentDrawOrder(AgentDrawOrder ado) {
		// Check all lists for agent and remove from it's current list...
		for(int i=0; i<SpriteDrawOrder.values().length; i++) {
			if(drawAgents[i].contains(ado.agent))
				drawAgents[i].remove(ado.agent);
		}
		// ... and add to new list.
		if(ado.drawOrder != SpriteDrawOrder.NONE)
			drawAgents[ado.drawOrder.ordinal()].add(ado.agent);
	}

	/*
	 * Call process() after any calls to doSetAgentDrawOrder() or remove(), and before calling this method.
	 */
	public Collection<Agent>[] getAgentsToDraw() {
		return drawAgents;
	}

	public void remove(Agent agent) {
		// check lists of agents to draw, remove agent if found
		for(int i=0; i<SpriteDrawOrder.values().length; i++) {
			if(drawAgents[i].contains(agent))
				drawAgents[i].remove(agent);
		}
	}
}
