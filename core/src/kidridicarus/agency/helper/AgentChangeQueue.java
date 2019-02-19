package kidridicarus.agency.helper;

import java.util.concurrent.LinkedBlockingQueue;

import kidridicarus.info.GameInfo.SpriteDrawOrder;

public class AgentChangeQueue {
	private LinkedBlockingQueue<AgentChange> changeQ;

	public class AgentChange {
		public AgentPlaceholder ap;
		// If true then this change is: add agent to list of all agents,
		// if false then this change is: remove agent from list of all agents.
		// If null then ignore.
		public Boolean addAgent;
		// If true then this change is: enable agent updates,
		// if false then this change is: disable agent updates.
		// If null then ignore.
		public Boolean enableUpdate;
		// If non-null then this change is: set draw order.
		// If null then ignore.
		public SpriteDrawOrder drawOrder;

		public AgentChange(AgentPlaceholder agent, Boolean addAgent, Boolean enableUpdate,
				SpriteDrawOrder newDrawOrder) {
			this.ap = agent;
			this.addAgent = addAgent;
			this.enableUpdate = enableUpdate;
			this.drawOrder = newDrawOrder;
		}
	}

	public interface AgentChangeCallback { public void change(AgentChange agentChange); }

	public AgentChangeQueue() {
		changeQ = new LinkedBlockingQueue<AgentChange>();
	}

	public void addAgent(AgentPlaceholder agent) {
		changeQ.add(new AgentChange(agent, true, null, null));
	}

	public void removeAgent(AgentPlaceholder agent) {
		changeQ.add(new AgentChange(agent, false, null, null));
	}

	public void enableAgentUpdate(AgentPlaceholder agent) {
		changeQ.add(new AgentChange(agent, null, true, null));
	}

	public void disableAgentUpdate(AgentPlaceholder agent) {
		changeQ.add(new AgentChange(agent, null, false, null));
	}

	public void setAgentDrawOrder(AgentPlaceholder agent, SpriteDrawOrder order) {
		changeQ.add(new AgentChange(agent, null, null, order));
	}

	/*
	 * Iterate through each agent change in queue until queue is empty, invoking callback for each agent.
	 */
	public void process(AgentChangeCallback accb) {
		while(!changeQ.isEmpty()) {
			AgentChange ar = changeQ.poll();
			accb.change(ar);
		}
	}
}
