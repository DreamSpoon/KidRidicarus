package kidridicarus.common.agent.quarteractor;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.agency.agent.AgentSprite;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.general.PlacedBoundsAgent;

public abstract class QuarterActor extends PlacedBoundsAgent {
	protected AgentBody body;
	protected QuarterActorBrain brain;
	protected AgentSprite sprite;

	public QuarterActor(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = null;
		brain = null;
		sprite = null;
	}
}
