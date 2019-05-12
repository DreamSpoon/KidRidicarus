package kidridicarus.game.Metroid.agent.player.samuschunk;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction8;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.Metroid.MetroidKV;

public class SamusChunk extends CorpusAgent {
	private static final float MAX_DROP_TIME = 0.75f;

	private SamusChunkSprite sprite;
	private float stateTimer;
	private boolean isDrawAllowed;

	public SamusChunk(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		stateTimer = 0f;
		isDrawAllowed = true;
		body = new SamusChunkBody(this, agentHooks.getWorld(), AP_Tool.getCenter(properties));
		sprite = new SamusChunkSprite(agentHooks.getAtlas(), body.getPosition(), AP_Tool.safeGetDirection8(properties));
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(processFrame(frameTime)); }
			});
		agentHooks.addDrawListener(CommonInfo.DrawOrder.SPRITE_TOP, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
		agentHooks.createAgentRemoveListener(this, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	private SpriteFrameInput processFrame(FrameTime frameTime) {
		stateTimer += frameTime.timeDelta;
		if(stateTimer > MAX_DROP_TIME) {
			agentHooks.removeThisAgent();
			return null;
		}
		isDrawAllowed = !isDrawAllowed;	// flicker the sprite
		if(isDrawAllowed)
			return SprFrameTool.placeAnim(body.getPosition(), frameTime);
		else
			return null;
	}

	public static ObjectProperties makeAP(Vector2 position, Vector2 velocity, Direction8 startDir) {
		ObjectProperties props = AP_Tool.createPointAP(MetroidKV.AgentClassAlias.VAL_SAMUS_CHUNK,
				position, velocity);
		props.put(CommonKV.KEY_DIRECTION, startDir);
		return props;
	}
}
