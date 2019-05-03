package kidridicarus.game.KidIcarus.agent.player.pitarrow;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.FrameTime;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.KidIcarus.agent.player.pit.Pit;
import kidridicarus.game.info.KidIcarusKV;

public class PitArrow extends CorpusAgent implements DisposableAgent {
	private PitArrowBrain brain;
	private PitArrowSprite sprite;

	public PitArrow(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		Direction4 arrowDir = properties.get(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class);
		body = new PitArrowBody(this, agency.getWorld(), AP_Tool.getCenter(properties),
				AP_Tool.getVelocity(properties), arrowDir);
		brain = new PitArrowBrain(this, (PitArrowBody) body, properties.get(CommonKV.KEY_PARENT_AGENT, null, Pit.class),
				properties.containsKV(CommonKV.Spawn.KEY_EXPIRE, true), arrowDir);
		sprite = new PitArrowSprite(agency.getAtlas(), new PitArrowSpriteFrameInput(body.getPosition(), arrowDir));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((PitArrowBody) body).processContactFrame());
				}
			});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					sprite.processFrame(brain.processFrame(frameTime.timeDelta));
				}
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
	}

	@Override
	public void disposeAgent() {
		dispose();
	}

	// make the AgentProperties (AP) for this class of Agent
	public static ObjectProperties makeAP(Pit parentAgent, Vector2 position, Vector2 velocity, Direction4 arrowDir,
			boolean isExpireImmediately) {
		ObjectProperties props = AP_Tool.createPointAP(KidIcarusKV.AgentClassAlias.VAL_PIT_ARROW, position, velocity);
		props.put(CommonKV.KEY_PARENT_AGENT, parentAgent);
		props.put(CommonKV.KEY_DIRECTION, arrowDir);
		props.put(CommonKV.Spawn.KEY_EXPIRE, isExpireImmediately);
		return props;
	}
}
