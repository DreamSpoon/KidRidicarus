package kidridicarus.agent.body.sensor;

import kidridicarus.agency.contact.AgentBodyFilter;

/*
 * Chainable contact sensor.
 */
public abstract class ContactSensor {
	private Object parent;
	private ContactSensor nextInChain;
	
	public ContactSensor(Object parent) {
		this.parent = parent;
		nextInChain = null;
	}

	// In case a contact sensor contacts another contact sensor, it can use getParent on the other contact sensor
	// to figure out what was actually contacted.
	public Object getParent() {
		return parent;
	}

	public void chainTo(ContactSensor nextInChain) {
		this.nextInChain = nextInChain;
	}

	public void onBeginContact(AgentBodyFilter abf) {
		onBeginSense(abf);
		if(nextInChain != null)
			nextInChain.onBeginContact(abf);
		
	}

	public void onEndContact(AgentBodyFilter abf) {
		onEndSense(abf);
		if(nextInChain != null)
			nextInChain.onEndContact(abf);
	}

	public abstract void onBeginSense(AgentBodyFilter abf);
	public abstract void onEndSense(AgentBodyFilter abf);
}
