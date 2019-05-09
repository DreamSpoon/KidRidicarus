package kidridicarus.game.KidIcarus.agent.other.vanishpoof;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.info.KidIcarusKV;

public class VanishPoof extends CorpusAgent {
	private static final float POOF_TIME = 2/5f;

	private VanishPoofSprite sprite;
	private float stateTimer;

	public VanishPoof(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		stateTimer = 0f;
		sprite = new VanishPoofSprite(agency.getAtlas(), AP_Tool.getCenter(properties),
				properties.getBoolean(KidIcarusKV.KEY_IS_BIG, false));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(doUpdate(frameTime)); }
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_TOP, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
	}

	private SpriteFrameInput doUpdate(FrameTime frameTime) {
		stateTimer += frameTime.timeDelta;
		if(stateTimer > POOF_TIME) {
			agency.removeAgent(this);
			return null;
		}
		return SprFrameTool.placeAnim(getPosition(), frameTime);
	}

	@Override
	protected Vector2 getPosition() {
		return new Vector2(sprite.getX()+sprite.getWidth()/2f, sprite.getY()+sprite.getHeight()/2f);
	}

	@Override
	protected Rectangle getBounds() {
		return new Rectangle(sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
	}

	public static ObjectProperties makeAP(Vector2 position, boolean isBig) {
		ObjectProperties props = AP_Tool.createPointAP(KidIcarusKV.AgentClassAlias.VAL_VANISH_POOF, position);
		props.put(KidIcarusKV.KEY_IS_BIG, isBig);
		return props;
	}
}
