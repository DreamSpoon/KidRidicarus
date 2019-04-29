package kidridicarus.common.agent.quarteractor;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.agentsprite.AgentSprite;
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
