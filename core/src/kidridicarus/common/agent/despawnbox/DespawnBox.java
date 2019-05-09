package kidridicarus.common.agent.despawnbox;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.tool.AP_Tool;

public class DespawnBox extends CorpusAgent {
	public DespawnBox(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new DespawnBoxBody(this, agency.getWorld(), AP_Tool.getBounds(properties));
		agency.addAgentRemoveListener(new AgentRemoveListener(this, this) {
			@Override
			public void preRemoveAgent() { dispose(); }
		});
	}
}