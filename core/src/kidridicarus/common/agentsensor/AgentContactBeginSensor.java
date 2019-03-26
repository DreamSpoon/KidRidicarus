package kidridicarus.common.agentsensor;

import java.util.LinkedList;
import java.util.List;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.agentcontact.AgentContactSensor;

public class AgentContactBeginSensor extends AgentContactSensor {
	private LinkedList<Agent> contacts;

	public AgentContactBeginSensor(Object parent) {
		super(parent);
		contacts = new LinkedList<Agent>();
	}

	@Override
	public void onBeginSense(AgentBodyFilter abf) {
		Agent agent = AgentBodyFilter.getAgentFromFilter(abf);
		if(agent != null && !contacts.contains(agent))
			contacts.add(agent);
	}

	@Override
	public void onEndSense(AgentBodyFilter abf) {
		// only begin sense is needed
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
