package kidridicarus.common.agent.fullactor;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentSprite;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.general.MotileBoundsAgent;

public abstract class FullActor extends MotileBoundsAgent {
	protected FullActorBody body;
	protected FullActorBrain brain;
	protected AgentSprite sprite;

	public FullActor(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = null;
		brain = null;
		sprite = null;
	}
}
