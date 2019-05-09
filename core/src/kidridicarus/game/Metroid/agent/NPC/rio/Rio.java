package kidridicarus.game.Metroid.agent.NPC.rio;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;

/*
 * TODO Check that Rio can re-target: as in, lose a target, then wait a bit, then gain a new target successfully.
 */
public class Rio extends CorpusAgent implements ContactDmgTakeAgent {
	private RioBrain brain;
	private RioSprite sprite;

	public Rio(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new RioBody(this, agency.getWorld(), AP_Tool.getCenter(properties),
				AP_Tool.safeGetVelocity(properties));
		brain = new RioBrain(this, (RioBody) body);
		sprite = new RioSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((RioBody) body).processContactFrame());
				}
			});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(brain.processFrame(frameTime)); }
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_BOTTOM, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
		agency.addAgentRemoveListener(new AgentRemoveListener(this, this) {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		return brain.onTakeDamage(agent, amount);
	}
}
