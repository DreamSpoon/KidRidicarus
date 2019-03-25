package kidridicarus.common.agentsensor;

import java.util.LinkedList;
import java.util.List;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.agentcontact.AgentContactSensor;

/*
 * Keep track of agents contacted.
 */
public class AgentContactHoldSensor extends AgentContactSensor {
	private LinkedList<Agent> contacts;

	public AgentContactHoldSensor(Object parent) {
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
		Agent agent = AgentBodyFilter.getAgentFromFilter(abf);
		if(agent != null && contacts.contains(agent))
			contacts.remove(agent);
	}

	public Agent getFirstContact() {
		return contacts.getFirst();
	}

	public List<Agent> getContacts() {
		return contacts;
	}

	// ignore unchecked cast because isAssignableFrom method is used to check class
	@SuppressWarnings("unchecked")
	public <T> T getFirstContactByClass(Class<T> cls) {
		for(Agent agent : contacts) {
			if(cls.isAssignableFrom(agent.getClass()))
				return (T) agent;
		}
		return null;
	}

	// ignore unchecked cast because isAssignableFrom method is used to check class
	@SuppressWarnings("unchecked")
	public <T> List<T> getContactsByClass(Class<T> cls) {
		List<T> cList = new LinkedList<T>();
		for(Agent agent : contacts) {
			if(cls.isAssignableFrom(agent.getClass()))
				cList.add((T) agent);
		}
		return cList;
	}
}
