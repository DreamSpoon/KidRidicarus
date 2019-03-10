package kidridicarus.agency.agencychange;

import java.util.concurrent.LinkedBlockingQueue;

import kidridicarus.agency.AgentUpdateListener;
import kidridicarus.agency.tool.AllowOrder;

public class AgencyChangeQueue {
	private LinkedBlockingQueue<Object> changeQ;

	public interface AgencyChangeCallback { public void change(Object change); }

	public AgencyChangeQueue() {
		changeQ = new LinkedBlockingQueue<Object>();
	}

	public void addAgent(AgentPlaceholder ap) {
		changeQ.add(new AllAgentListChange(ap, true));
	}

	public void removeAgent(AgentPlaceholder ap) {
		changeQ.add(new AllAgentListChange(ap, false));
	}

//	public void setAgentUpdateOrder(AgentPlaceholder ap, AllowOrder order) {
//		changeQ.add(new UpdateOrderChange(ap, order));
//	}

	public void setAgentDrawOrder(AgentPlaceholder ap, AllowOrder order) {
		changeQ.add(new DrawOrderChange(ap, order));
	}

	/*
	 * Iterate through each agent change in queue until queue is empty, invoking callback for each agent.
	 */
	public void process(AgencyChangeCallback accb) {
		while(!changeQ.isEmpty()) {
			Object change = changeQ.poll();
			accb.change(change);
		}
	}

	public void addAgentUpdateListener(AgentPlaceholder ap, AllowOrder newUpdateOrder,
			AgentUpdateListener auListener) {
		changeQ.add(new UpdateListenerChange(ap, newUpdateOrder, auListener, true));
	}

	public void removeAgentUpdateListener(AgentPlaceholder ap, AgentUpdateListener auListener) {
		changeQ.add(new UpdateListenerChange(ap, null, auListener, false));
	}
}
