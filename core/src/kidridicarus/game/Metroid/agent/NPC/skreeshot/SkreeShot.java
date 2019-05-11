package kidridicarus.game.Metroid.agent.NPC.skreeshot;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.info.MetroidKV;

public class SkreeShot extends CorpusAgent {
	private SkreeShotBrain brain;
	private SkreeShotSprite sprite;

	public SkreeShot(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		body = new SkreeShotBody(this, agentHooks.getWorld(), AP_Tool.getCenter(properties),
				AP_Tool.safeGetVelocity(properties));
		brain = new SkreeShotBrain(this, agentHooks, (SkreeShotBody) body);
		sprite = new SkreeShotSprite(agentHooks.getAtlas(), body.getPosition());
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((SkreeShotBody) body).processContactFrame());
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

	public static ObjectProperties makeAP(Vector2 position, Vector2 velocity) {
		return AP_Tool.createPointAP(MetroidKV.AgentClassAlias.VAL_SKREE_SHOT, position, velocity);
	}
}
