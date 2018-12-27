package kidridicarus.agent.bodies.sensor;

import kidridicarus.agent.bodies.AgentBody;

public interface FindGuideAgentCallback {
	public void onBeginContactGuideAgent(AgentBody body);
	public void onEndContactGuideAgent(AgentBody body);
}
