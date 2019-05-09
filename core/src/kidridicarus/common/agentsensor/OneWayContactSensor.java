package kidridicarus.common.agentsensor;

import java.util.LinkedList;
import java.util.List;

import kidridicarus.agency.Agent;
import kidridicarus.agency.agentbody.AgentBodyFilter;
import kidridicarus.agency.agentbody.AgentContactSensor;

/*
 * Keep a list of either begin contacts, or end contacts, but not both.
 */
public class OneWayContactSensor extends AgentContactSensor {
	private LinkedList<Agent> contacts;
	private boolean isBeginSensor;

	public OneWayContactSensor(Object parent, boolean isBeginSensor) {
		super(parent);
		this.isBeginSensor = isBeginSensor;
		contacts = new LinkedList<Agent>();
	}

	@Override
	public void onBeginSense(AgentBodyFilter abf) {
		if(!isBeginSensor)
			return;

		Agent agent = AgentBodyFilter.getAgentFromFilter(abf);
		if(agent != null && !contacts.contains(agent))
			contacts.add(agent);
	}

	@Override
	public void onEndSense(AgentBodyFilter abf) {
		if(isBeginSensor)
			return;

		Agent agent = AgentBodyFilter.getAgentFromFilter(abf);
		if(agent != null && !contacts.contains(agent))
			contacts.add(agent);
	}

	public List<Agent> getAndResetContacts() {
		List<Agent> aList = new LinkedList<Agent>();
		aList.addAll(contacts);
		contacts.clear();
		return aList;
	}

	// ignore unchecked cast because isAssignableFrom method is used to check class
	@SuppressWarnings("unchecked")
	public <T> List<T> getOnlyAndResetContacts(Class<T> cls) {
		List<T> cList = new LinkedList<T>();
		for(Agent agent : contacts) {
			if(cls.isAssignableFrom(agent.getClass()))
				cList.add((T) agent);
		}
		contacts.clear();
		return cList;
	}
}
