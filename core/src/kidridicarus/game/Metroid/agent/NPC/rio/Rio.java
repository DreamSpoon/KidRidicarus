package kidridicarus.game.Metroid.agent.NPC.rio;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.fullactor.FullActor;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;

/*
 * TODO Check that Rio can re-target: as in, lose a target, then wait a bit, then gain a new target successfully.
 */
public class Rio extends FullActor implements ContactDmgTakeAgent, DisposableAgent {
	public Rio(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new RioBody(this, agency.getWorld(), AP_Tool.getCenter(properties),
				AP_Tool.getVelocity(properties));
		brain = new RioBrain(this, (RioBody) body);
		sprite = new RioSprite(agency.getAtlas(), body.getPosition());
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
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_BOTTOM, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
	}

	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		return ((RioBrain) brain).onTakeDamage(agent, amount);
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