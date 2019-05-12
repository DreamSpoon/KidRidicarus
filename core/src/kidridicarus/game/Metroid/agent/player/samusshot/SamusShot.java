package kidridicarus.game.Metroid.agent.player.samusshot;

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
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.Metroid.MetroidKV;
import kidridicarus.game.Metroid.agent.player.samus.Samus;

public class SamusShot extends CorpusAgent {
	private SamusShotBrain brain;
	private SamusShotSprite sprite;

	public SamusShot(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		body = new SamusShotBody(this, agentHooks.getWorld(), AP_Tool.getCenter(properties),
				AP_Tool.safeGetVelocity(properties));
		brain = new SamusShotBrain(properties.get(CommonKV.KEY_PARENT_AGENT, null, Samus.class), agentHooks,
				(SamusShotBody) body, properties.getBoolean(CommonKV.Spawn.KEY_EXPIRE, false));
		sprite = new SamusShotSprite(agentHooks.getAtlas(), body.getPosition());
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((SamusShotBody) body).processContactFrame());
				}
			});
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(brain.processFrame(frameTime)); }
			});
		agentHooks.addDrawListener(CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
		agentHooks.createAgentRemoveListener(this, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	// make the AgentProperties (AP) for this class of Agent
	public static ObjectProperties makeAP(Samus parentAgent, Vector2 position, Vector2 velocity,
			boolean isExpireImmediately) {
		ObjectProperties props = AP_Tool.createPointAP(MetroidKV.AgentClassAlias.VAL_SAMUS_SHOT,
				position, velocity);
		props.put(CommonKV.KEY_PARENT_AGENT, parentAgent);
		props.put(CommonKV.Spawn.KEY_EXPIRE, isExpireImmediately);
		return props;
	}
}
