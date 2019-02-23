package kidridicarus.agency.agent.body.sensor;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.optional.PlayerAgent;
import kidridicarus.agency.contact.AgentBodyFilter;

/*
 * Keep track of non-Player agents contacted.
 */
public class AgentContactSensor extends ContactSensor {
	private LinkedList<Agent> contacts;

	public AgentContactSensor(Object parent) {
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

	public <T> Agent getFirstContactByClass(Class<T> clazz) {
		for(Agent a : contacts) {
			if(clazz.isAssignableFrom(a.getClass()))
				return a;
		}
		return null;
	}

	public <T> List<Agent> getContactsByClass(Class<T> clazz) {
		List<Agent> aList = new LinkedList<Agent>();
		for(Agent a : contacts) {
			if(clazz.isAssignableFrom(a.getClass()))
				aList.add(a);
		}
		return aList;
	}

	public static boolean isMoveBlockedByAgent(AgentContactSensor theSensor, Vector2 position, boolean moveRight) {
		for(Agent a : theSensor.getContacts()) {
			// do not check against players
			if(a instanceof PlayerAgent)
				continue;

			// If wants to move right and other agent is on the right side then move is blocked
			if(moveRight && position.x < a.getPosition().x)
				return true;
			// If wants to move left and other agent is on the left side then move is blocked
			else if(!moveRight && position.x > a.getPosition().x)
				return true;
		}
		return false;
	}
}
