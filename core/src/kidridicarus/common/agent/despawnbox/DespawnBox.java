package kidridicarus.common.agent.despawnbox;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.tool.AP_Tool;

public class DespawnBox extends CorpusAgent {
	public DespawnBox(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		body = new DespawnBoxBody(this, agentHooks.getWorld(), AP_Tool.getBounds(properties));
		agentHooks.createAgentRemoveListener(this, new AgentRemoveCallback() {
			@Override
			public void preRemoveAgent() { dispose(); }
		});
	}
}