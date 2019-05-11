package kidridicarus.game.SMB1.agent.other.brickpiece;

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
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.info.SMB1_KV;

public class BrickPiece extends CorpusAgent {
	// bricks should be auto-removed when off screen, use this timeout for other cases
	private static final float BRICK_DIE_TIME = 7f;

	private BrickPieceSprite sprite;
	private float stateTimer;

	public BrickPiece(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		stateTimer = 0f;
		body = new BrickPieceBody(this, agentHooks.getWorld(), AP_Tool.getCenter(properties),
				AP_Tool.safeGetVelocity(properties));
		sprite = new BrickPieceSprite(agentHooks.getAtlas(), body.getPosition(),
				properties.getInteger(CommonKV.Sprite.KEY_START_FRAME, 0));
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
		if(stateTimer > BRICK_DIE_TIME) {
			agentHooks.removeThisAgent();
			return null;
		}
		return SprFrameTool.placeAnim(body.getPosition(), frameTime);
	}

	public static ObjectProperties makeAP(Vector2 position, Vector2 velocity, int startFrame) {
		ObjectProperties props = AP_Tool.createPointAP(SMB1_KV.AgentClassAlias.VAL_BRICKPIECE, position, velocity);
		props.put(CommonKV.Sprite.KEY_START_FRAME, startFrame);
		return props;
	}
}
