package kidridicarus.agent.body.sensor;

import kidridicarus.agency.contact.AgentBodyFilter;

/*
 * Chainable contact sensor.
 */
public abstract class ContactSensor {
	private ContactSensor nextInChain = null;

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

	// In case a contact sensor contacts another contact sensor, it can use getParent on the other contact sensor
	// to figure out what was actually contacted.
	public abstract Object getParent();
}
