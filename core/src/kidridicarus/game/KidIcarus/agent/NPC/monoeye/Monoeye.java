package kidridicarus.game.KidIcarus.agent.NPC.monoeye;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.proactoragent.ProactorAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;

/*
 * Monoeye doesn't like it when gawkers stare at Monoeye, so Monoeye will target the gawker and attempt to
 * ogle them in a downward direction.
 * QQ
 */
public class Monoeye extends ProactorAgent implements ContactDmgTakeAgent, DisposableAgent {
	public Monoeye(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new MonoeyeBody(
				this, agency.getWorld(), AP_Tool.getCenter(properties), AP_Tool.getVelocity(properties));
		brain = new MonoeyeBrain(this, (MonoeyeBody) body);
		sprite = new MonoeyeSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { brain.processContactFrame(body.processContactFrame()); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) {
					sprite.processFrame(brain.processFrame(body.processFrame(delta)));
				}
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
	}

	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		return ((MonoeyeBrain) brain).onTakeDamage(agent);
	}

	@Override
	public Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	protected Vector2 getVelocity() {
		return body.getVelocity();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
