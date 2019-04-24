package kidridicarus.common.agent.proactoragent;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.general.MotileBoundsAgent;

public abstract class ProactorAgent extends MotileBoundsAgent {
	protected ProactorAgentBody body;
	protected ProactorAgentBrain brain;
	protected ProactorAgentSprite sprite;

	public ProactorAgent(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = null;
		brain = null;
		sprite = null;
	}
}
