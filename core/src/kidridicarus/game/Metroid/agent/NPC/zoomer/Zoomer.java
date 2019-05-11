package kidridicarus.game.Metroid.agent.NPC.zoomer;

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
import kidridicarus.common.tool.AP_Tool;

/*
 * The sensor code. It seems like a million cases due to the 4 possible "up" directions of the zoomer,
 * and the 4 sensor states, and the fact that the zoomer moves left or right. But, this can all be
 * collapsed down to one type of movement. Just rotate your thinking and maybe flip left/right, then
 * check the sensors.
 */
public class Zoomer extends CorpusAgent implements ContactDmgTakeAgent {
	private ZoomerBrain brain;
	private ZoomerSprite sprite;

	public Zoomer(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		body = new ZoomerBody(this, agentHooks.getWorld(), AP_Tool.getCenter(properties),
				AP_Tool.safeGetVelocity(properties));
		brain = new ZoomerBrain(this, agentHooks, (ZoomerBody) body);
		sprite = new ZoomerSprite(agentHooks.getAtlas(), body.getPosition());
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((ZoomerBody) body).processContactFrame());
				}
			});
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(brain.processFrame(frameTime)); }
			});
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.POST_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { ((ZoomerBody) body).postUpdate(); }
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
