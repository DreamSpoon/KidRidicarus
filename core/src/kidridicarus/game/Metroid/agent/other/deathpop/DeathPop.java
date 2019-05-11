package kidridicarus.game.Metroid.agent.other.deathpop;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
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
import kidridicarus.game.info.MetroidKV;

public class DeathPop extends CorpusAgent {
	private static final float POP_TIME = 3f/60f;

	private DeathPopSprite sprite;
	private float stateTimer;
	private Vector2 position;

	public DeathPop(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		stateTimer = 0f;
		position = AP_Tool.getCenter(properties);
		sprite = new DeathPopSprite(agentHooks.getAtlas(), position);
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(processFrame(frameTime)); }
			});
		agentHooks.addDrawListener(CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
	}

	private SpriteFrameInput processFrame(FrameTime frameTime) {
		if(stateTimer > POP_TIME) {
			agentHooks.removeThisAgent();
			return null;
		}
		stateTimer += frameTime.timeDelta;
		return SprFrameTool.placeAnim(position, frameTime);
	}

	public static ObjectProperties makeAP(Vector2 position) {
		return AP_Tool.createPointAP(MetroidKV.AgentClassAlias.VAL_DEATH_POP, position);
	}
}
