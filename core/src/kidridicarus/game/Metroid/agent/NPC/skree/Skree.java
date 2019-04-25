package kidridicarus.game.Metroid.agent.NPC.skree;

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
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.AP_Tool;

public class Skree extends FullActor implements ContactDmgTakeAgent, DisposableAgent {
	private static final Vector2 SPECIAL_OFFSET = UInfo.VectorP2M(0f, -4f);

	public Skree(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new SkreeBody(this, agency.getWorld(), AP_Tool.getCenter(properties).cpy().add(SPECIAL_OFFSET),
				AP_Tool.getVelocity(properties));
		brain = new SkreeBrain(this, (SkreeBody) body);
		sprite = new SkreeSprite(agency.getAtlas(), body.getPosition());
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
		return ((SkreeBrain) brain).onTakeDamage(agent, amount);
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
