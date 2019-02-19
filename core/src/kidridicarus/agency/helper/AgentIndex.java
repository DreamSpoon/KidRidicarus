package kidridicarus.agency.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agent.Agent;
import kidridicarus.info.GameInfo.SpriteDrawOrder;

/*
 * A list of agents, with sub-lists available upon request.
 * Sub-lists relate to which agents receive updates and agent draw orders.
 */
public class AgentIndex implements Disposable {
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
/*
 * TODO: Fix this (2019.02.19 12.34.12):
 * WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by org.lwjgl.LWJGLUtil$3 (file:/C:/Users/pluser/.gradle/caches/modules-2/files-2.1/org.lwjgl.lwjgl/lwjgl/2.9.2/a9d80fe5935c7a9149f6584d9777cfd471f65489/lwjgl-2.9.2.jar) to method java.lang.ClassLoader.findLibrary(java.lang.String)
WARNING: Please consider reporting this to the maintainers of org.lwjgl.LWJGLUtil$3
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release
Exception in thread "LWJGL Application" java.lang.IllegalArgumentException: Agent does not exist in list of all agents: kidridicarus.agent.Metroid.player.SamusShot@308a81eb
	at kidridicarus.agency.helper.AgentIndex.disableAgentUpdate(AgentIndex.java:62)
	at kidridicarus.agency.helper.AgentIndex.removeAgent(AgentIndex.java:41)
	at kidridicarus.agency.Agency$1.change(Agency.java:217)
	at kidridicarus.agency.helper.AgentChangeQueue.process(AgentChangeQueue.java:65)
	at kidridicarus.agency.Agency.processAgentChangeQ(Agency.java:210)
	at kidridicarus.agency.Agency.updateAgents(Agency.java:206)
	at kidridicarus.agency.Agency.update(Agency.java:140)
	at kidridicarus.agencydirector.AgencyDirector.update(AgencyDirector.java:121)
	at kidridicarus.screen.PlayScreen.update(PlayScreen.java:114)
	at kidridicarus.screen.PlayScreen.render(PlayScreen.java:59)
	at com.badlogic.gdx.Game.render(Game.java:46)
	at kidridicarus.MyKidRidicarus.render(MyKidRidicarus.java:65)
	at com.badlogic.gdx.backends.lwjgl.LwjglApplication.mainLoop(LwjglApplication.java:225)
	at com.badlogic.gdx.backends.lwjgl.LwjglApplication$1.run(LwjglApplication.java:126)
 */
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

	/*
	 * Call dispose method of each agent in the all agents list.
	 */
	@Override
	public void dispose() {
		iterateThroughAllAgents(new AgentIter() {
				@Override
				public boolean iterate(Agent agent) {
					agent.dispose();
					return false;
				}
			});
	}
}
