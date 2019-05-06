package kidridicarus.game.Metroid.agent.player.samuschunk;

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
import kidridicarus.common.tool.Direction8;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.info.MetroidKV;

public class SamusChunk extends CorpusAgent {
	private static final float MAX_DROP_TIME = 0.75f;

	private SamusChunkSprite sprite;
	private float stateTimer;
	private boolean isDrawAllowed;

	public SamusChunk(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		stateTimer = 0f;
		isDrawAllowed = true;
		body = new SamusChunkBody(this, agency.getWorld(), AP_Tool.getCenter(properties));
		sprite = new SamusChunkSprite(agency.getAtlas(), body.getPosition(), AP_Tool.getDirection8(properties));
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
		if(stateTimer > MAX_DROP_TIME) {
			agency.removeAgent(this);
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
