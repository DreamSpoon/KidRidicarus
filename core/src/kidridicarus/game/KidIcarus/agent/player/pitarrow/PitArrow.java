package kidridicarus.game.KidIcarus.agent.player.pitarrow;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.KidIcarus.agent.player.pit.Pit;
import kidridicarus.game.info.KidIcarusKV;

public class PitArrow extends CorpusAgent {
	private PitArrowBrain brain;
	private PitArrowSprite sprite;

	public PitArrow(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		Direction4 arrowDir = properties.getDirection4(CommonKV.KEY_DIRECTION, Direction4.NONE);
		body = new PitArrowBody(this, agency.getWorld(), AP_Tool.getCenter(properties),
				AP_Tool.safeGetVelocity(properties), arrowDir);
		brain = new PitArrowBrain(this, (PitArrowBody) body,
				properties.get(CommonKV.KEY_PARENT_AGENT, null, Pit.class),
				properties.getBoolean(CommonKV.Spawn.KEY_EXPIRE, false), arrowDir);
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
		agency.addAgentRemoveListener(new AgentRemoveListener(this, this) {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
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
