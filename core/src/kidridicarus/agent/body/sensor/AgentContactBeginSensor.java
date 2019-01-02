package kidridicarus.agent.body.sensor;

import java.util.LinkedList;

import kidridicarus.agency.contact.AgentBodyFilter;
import kidridicarus.agent.Agent;

public class AgentContactBeginSensor extends ContactSensor {
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
	}

	public LinkedList<Agent> getAndResetContacts() {
		LinkedList<Agent> list = new LinkedList<Agent>();
		list.addAll(contacts);
		contacts.clear();
		return list;
	}
}
