package kidridicarus.game.SMB1.agent.other.brickpiece;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.FrameTime;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.Eye;
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

	public BrickPiece(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		stateTimer = 0f;
		body = new BrickPieceBody(this, agency.getWorld(), AP_Tool.getCenter(properties),
				AP_Tool.getVelocity(properties));
		sprite = new BrickPieceSprite(agency.getAtlas(), body.getPosition(),
				properties.get(CommonKV.Sprite.KEY_START_FRAME, 0, Integer.class));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(processFrame(frameTime)); }
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_TOP, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
		agency.addAgentRemoveListener(new AgentRemoveListener(this, this) {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	private SpriteFrameInput processFrame(FrameTime frameTime) {
		stateTimer += frameTime.timeDelta;
		if(stateTimer > BRICK_DIE_TIME) {
			agency.removeAgent(this);
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
