package kidridicarus.common.agent.playeragent;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.agent.roombox.RoomBox;

public abstract class PlayerAgent extends CorpusAgent {
	public abstract PlayerAgentSupervisor getSupervisor();
	public abstract RoomBox getCurrentRoom();

	protected PlayerAgent(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		agentHooks.createAgentRemoveListener(this, new AgentRemoveCallback() {
			@Override
			public void preRemoveAgent() { dispose(); }
		});
	}

	public void removeSelf() {
		agentHooks.removeThisAgent();
	}
}
