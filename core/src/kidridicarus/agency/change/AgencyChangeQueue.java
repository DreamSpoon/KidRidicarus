package kidridicarus.agency.change;

import java.util.concurrent.LinkedBlockingQueue;

import kidridicarus.common.tool.AllowOrder;

public class AgencyChangeQueue {
	private LinkedBlockingQueue<Object> changeQ;

	public interface AgencyChangeCallback { public void change(Object change); }

	public AgencyChangeQueue() {
		changeQ = new LinkedBlockingQueue<Object>();
	}

	public void addAgent(AgentPlaceholder ap) {
		changeQ.add(new AgentListChange(ap, true));
	}

	public void removeAgent(AgentPlaceholder ap) {
		changeQ.add(new AgentListChange(ap, false));
	}

	public void setAgentUpdateOrder(AgentPlaceholder ap, AllowOrder order) {
		changeQ.add(new UpdateOrderChange(ap, order));
	}

	public void setAgentDrawOrder(AgentPlaceholder ap, AllowOrder order) {
		changeQ.add(new DrawOrderChange(ap, order));
	}

//	public void setPhysicTile(int x, int y, boolean solid) {
//		changeQ.add(new TileChange(x, y, solid));
//	}

	/*
	 * Iterate through each agent change in queue until queue is empty, invoking callback for each agent.
	 */
	public void process(AgencyChangeCallback accb) {
		while(!changeQ.isEmpty()) {
			Object change = changeQ.poll();
			accb.change(change);
		}
	}
}
