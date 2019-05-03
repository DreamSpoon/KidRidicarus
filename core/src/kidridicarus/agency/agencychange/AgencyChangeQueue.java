package kidridicarus.agency.agencychange;

import java.util.concurrent.LinkedBlockingQueue;

import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.AllowOrder;

public class AgencyChangeQueue {
	private LinkedBlockingQueue<Object> changeQ;

	public interface AgencyChangeCallback { public void change(Object change); }

	public AgencyChangeQueue() {
		changeQ = new LinkedBlockingQueue<Object>();
	}

	// iterate through each Agent change in queue until queue is empty
	public void process(AgencyChangeCallback accb) {
		while(!changeQ.isEmpty()) {
			Object change = changeQ.poll();
			accb.change(change);
		}
	}

	public void addAgent(AgentPlaceholder ap) {
		changeQ.add(new AllAgentListChange(ap, true));
	}

	public void removeAgent(AgentPlaceholder ap) {
		changeQ.add(new AllAgentListChange(ap, false));
	}

	public void addAgentUpdateListener(AgentPlaceholder ap, AllowOrder newUpdOrder, AgentUpdateListener auListener) {
		changeQ.add(new AgentUpdateListenerChange(ap, newUpdOrder, auListener, true));
	}

	public void removeAgentUpdateListener(AgentPlaceholder ap, AgentUpdateListener auListener) {
		changeQ.add(new AgentUpdateListenerChange(ap, null, auListener, false));
	}

	public void addAgentDrawListener(AgentPlaceholder ap, AllowOrder newDrawOrder, AgentDrawListener adListener) {
		changeQ.add(new AgentDrawListenerChange(ap, newDrawOrder, adListener, true));
	}

	public void removeAgentDrawListener(AgentPlaceholder ap, AgentDrawListener adListener) {
		changeQ.add(new AgentDrawListenerChange(ap, null, adListener, false));
	}

	public void addAgentRemoveListener(AgentPlaceholder ap, AgentRemoveListener arListener) {
		changeQ.add(new AgentRemoveListenerChange(ap, arListener, true));
	}

	public void removeAgentRemoveListener(AgentPlaceholder ap, AgentRemoveListener arListener) {
		changeQ.add(new AgentRemoveListenerChange(ap, arListener, false));
	}
}
