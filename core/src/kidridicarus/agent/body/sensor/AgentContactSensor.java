package kidridicarus.agent.body.sensor;

import java.util.LinkedList;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.contact.AgentBodyFilter;
import kidridicarus.agent.Agent;
import kidridicarus.agent.PlayerAgent;

/*
 * Keep track of non-Player agents contacted.
 */
public class AgentContactSensor extends ContactSensor {
	private Object parent;
	private LinkedList<Agent> contacts;

	public AgentContactSensor(Object parent) {
		this.parent = parent;
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

	@Override
	public Object getParent() {
		return parent;
	}

	public Agent getFirstContact() {
		return contacts.getFirst();
	}

	public LinkedList<Agent> getContacts() {
		return contacts;
	}

	public <T> Agent getFirstContactByClass(Class<T> clazz) {
		for(Agent a : contacts) {
			if(clazz.isAssignableFrom(a.getClass()))
				return a;
		}
		return null;
	}

	public <T> LinkedList<Agent> getContactsByClass(Class<T> clazz) {
		LinkedList<Agent> list = new LinkedList<Agent>();
		for(Agent a : contacts) {
			if(clazz.isAssignableFrom(a.getClass()))
				list.add(a);
		}
		return list;
	}

	public static boolean isMoveBlockedByAgent(AgentContactSensor theSensor, Vector2 position, boolean moveRight) {
		for(Agent a : theSensor.getContacts()) {
			// do not check against players
			if(a instanceof PlayerAgent)
				continue;

			// If wants to move right and other agent is on the right side and other agent is moving left
			// then reverse
			if(moveRight && position.x < a.getPosition().x && a.getVelocity().x < 0f)
				return true;
			// If wants to move left and other agent is on the left side and other agent is moving right
			// then reverse
			else if(!moveRight && position.x > a.getPosition().x && a.getVelocity().x > 0f)
				return true;
		}
		return false;
	}
}
