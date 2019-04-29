package kidridicarus.common.agent.halfactor;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.agentsprite.AgentSprite;
import kidridicarus.common.agent.general.PlacedBoundsAgent;

public abstract class HalfActor extends PlacedBoundsAgent {
	protected HalfActorBody body;
	protected HalfActorBrain brain;
	protected AgentSprite sprite;

	public HalfActor(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = null;
		brain = null;
		sprite = null;
	}
}
