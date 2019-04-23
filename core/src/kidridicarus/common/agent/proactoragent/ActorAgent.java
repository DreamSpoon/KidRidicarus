package kidridicarus.common.agent.proactoragent;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.general.MotileBoundsAgent;

/*
 * RoleAgent needs to be renamed, thoughts?
 *
 * corporeal
 * material
 * tangible
 * fancy
 * think
 * reckon
 * agnostic
 * atheist
 * free thinker
 * beatnik
 * hippie
 * bohemian
 * sage
 * conscious
 * actor
 * 
 * 
 * TODO:
 * This Agent acts and reacts, so create another Agent type (a supertype of this type, maybe?) that
 * is a ReactorAgent - it only reacts, e.g. a powerup which does not update position.  
 */
public abstract class ActorAgent extends MotileBoundsAgent {
	protected ActorAgentBody body;
	protected ActorAgentBrain brain;
	protected ActorAgentSprite sprite;

	public ActorAgent(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = null;
		brain = null;
		sprite = null;
/*		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { brain.processContactFrame(body.processContactFrame()); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { sprite.processFrame(brain.processFrame(body.processFrame(delta))); }
		});
*/
	}
}
