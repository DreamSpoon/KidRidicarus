package kidridicarus.game.Metroid.agent.NPC.skree;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.AP_Tool;

public class Skree extends CorpusAgent implements ContactDmgTakeAgent {
	private static final Vector2 SPECIAL_OFFSET = UInfo.VectorP2M(0f, -4f);

	private SkreeBrain brain;
	private SkreeSprite sprite;

	public Skree(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		body = new SkreeBody(this, agentHooks.getWorld(), AP_Tool.getCenter(properties).cpy().add(SPECIAL_OFFSET),
				AP_Tool.safeGetVelocity(properties));
		brain = new SkreeBrain(this, agentHooks, (SkreeBody) body);
		sprite = new SkreeSprite(agentHooks.getAtlas(), body.getPosition());
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((SkreeBody) body).processContactFrame());
				}
			});
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(brain.processFrame(frameTime)); }
			});
		agentHooks.addDrawListener(CommonInfo.DrawOrder.SPRITE_BOTTOM, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
		agentHooks.createAgentRemoveListener(this, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		return brain.onTakeDamage(agent, amount);
	}
}
