package kidridicarus.agent.bodies.sensor;

import kidridicarus.agent.bodies.AgentBody;

public class FindGuideAgentSensor {
	private FindGuideAgentCallback cb;

	public FindGuideAgentSensor(FindGuideAgentCallback cb) {
		this.cb = cb;
	}

	public void onBeginContact(AgentBody body) {
		cb.onBeginContactGuideAgent(body);
	}

	public void onEndContact(AgentBody body) {
		cb.onEndContactGuideAgent(body);
	}
}
