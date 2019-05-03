package kidridicarus.game.SMB1.agent.other.spincoin;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.FrameTime;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.info.SMB1_KV;

public class SpinCoin extends CorpusAgent implements DisposableAgent {
	private static final float COIN_SPIN_TIME = 0.54f;
	private static final Vector2 START_VELOCITY = new Vector2(0f, 3.1f);

	private SpinCoinSprite coinSprite;
	private float stateTimer;

	public SpinCoin(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		stateTimer = 0f;
		body = new SpinCoinBody(this, agency.getWorld(), AP_Tool.getCenter(properties), START_VELOCITY);
		coinSprite = new SpinCoinSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { coinSprite.processFrame(processFrame(frameTime)); }
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(coinSprite); }
			});
	}

	private SpriteFrameInput processFrame(FrameTime frameTime) {
		stateTimer += frameTime.timeDelta;
		if(stateTimer > COIN_SPIN_TIME) {
			agency.removeAgent(this);
			return null;
		}
		return SprFrameTool.placeAnim(body.getPosition(), frameTime);
	}

	@Override
	public void disposeAgent() {
		dispose();
	}

	public static ObjectProperties makeAP(Vector2 position) {
		return AP_Tool.createPointAP(SMB1_KV.AgentClassAlias.VAL_SPINCOIN, position);
	}
}
