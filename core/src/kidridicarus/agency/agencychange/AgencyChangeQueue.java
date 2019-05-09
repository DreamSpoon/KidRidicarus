package kidridicarus.agency.agencychange;

import java.util.concurrent.LinkedBlockingQueue;

import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentPropertyListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.AllowOrder;

public class AgencyChangeQueue {
	public enum AgencyChangeType { AGENT_LIST, UPDATE_LISTENER, DRAW_LISTENER, REMOVE_LISTENER, PROPERTY_LISTENER }
	public class AgencyChange {
		public AgencyChangeType changeType;
		public boolean isAdd;
		public AgentPlaceholder ap;
		public Object otherData1;
		public Object otherData2;
		public Object otherData3;

		public AgencyChange(AgencyChangeType changeType, boolean add, AgentPlaceholder ap, Object otherData1,
				Object otherData2, Object otherData3) {
			this.changeType = changeType;
			this.isAdd = add;
			this.ap = ap;
			this.otherData1 = otherData1;
			this.otherData2 = otherData2;
			this.otherData3 = otherData3;
		}
	}

	private LinkedBlockingQueue<AgencyChange> changeQ;

	public interface AgencyChangeCallback { public void change(AgencyChange change); }

	public AgencyChangeQueue() {
		changeQ = new LinkedBlockingQueue<AgencyChange>();
	}

	// iterate through each Agent change in queue until queue is empty
	public void process(AgencyChangeCallback accb) {
		while(!changeQ.isEmpty()) {
			AgencyChange change = changeQ.poll();
			accb.change(change);
		}
	}

	public void addAgent(AgentPlaceholder ap) {
		changeQ.add(new AgencyChange(AgencyChangeType.AGENT_LIST, true, ap, null, null, null));
	}

	public void removeAgent(AgentPlaceholder ap) {
		changeQ.add(new AgencyChange(AgencyChangeType.AGENT_LIST, false, ap, null, null, null));
	}

	public void addAgentUpdateListener(AgentPlaceholder ap, AgentUpdateListener auListener, AllowOrder updateOrder) {
		changeQ.add(new AgencyChange(AgencyChangeType.UPDATE_LISTENER, true, ap, auListener, updateOrder, null));
	}

	public void removeAgentUpdateListener(AgentPlaceholder ap, AgentUpdateListener auListener) {
		changeQ.add(new AgencyChange(AgencyChangeType.UPDATE_LISTENER, false, ap, auListener, null, null));
	}

	public void addAgentDrawListener(AgentPlaceholder ap, AgentDrawListener adListener, AllowOrder drawOrder) {
		changeQ.add(new AgencyChange(AgencyChangeType.DRAW_LISTENER, true, ap, adListener, drawOrder, null));
	}

	public void removeAgentDrawListener(AgentPlaceholder ap, AgentDrawListener adListener) {
		changeQ.add(new AgencyChange(AgencyChangeType.DRAW_LISTENER, false, ap, adListener, null, null));
	}

	public void addAgentRemoveListener(AgentPlaceholder ap, AgentRemoveListener arListener) {
		changeQ.add(new AgencyChange(AgencyChangeType.REMOVE_LISTENER, true, ap, arListener, null, null));
	}

	public void removeAgentRemoveListener(AgentPlaceholder ap, AgentRemoveListener arListener) {
		changeQ.add(new AgencyChange(AgencyChangeType.REMOVE_LISTENER, false, ap, arListener, null, null));
	}

	public void addAgentPropertyListener(AgentPlaceholder ap, AgentPropertyListener<?> apListener,
			String propertyKey, boolean isGlobal) {
		changeQ.add(new AgencyChange(AgencyChangeType.PROPERTY_LISTENER, true, ap, apListener, propertyKey, isGlobal));
	}

	public void removeAgentPropertyListener(AgentPlaceholder ap, AgentPropertyListener<?> apListener,
			String propertyKey) {
		changeQ.add(new AgencyChange(AgencyChangeType.PROPERTY_LISTENER, false, ap, apListener, propertyKey, null));
	}
}
