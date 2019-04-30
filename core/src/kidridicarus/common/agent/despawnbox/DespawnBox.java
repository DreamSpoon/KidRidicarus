package kidridicarus.common.agent.despawnbox;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.corpusagent.CorpusAgent;
import kidridicarus.common.tool.AP_Tool;

public class DespawnBox extends CorpusAgent implements DisposableAgent {
	public DespawnBox(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new DespawnBoxBody(this, agency.getWorld(), AP_Tool.getBounds(properties));
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}