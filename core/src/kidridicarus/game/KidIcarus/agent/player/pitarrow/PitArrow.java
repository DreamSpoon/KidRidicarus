package kidridicarus.game.KidIcarus.agent.player.pitarrow;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.fullactor.FullActor;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.KidIcarus.agent.player.pit.Pit;
import kidridicarus.game.info.KidIcarusKV;

public class PitArrow extends FullActor implements DisposableAgent {
	public PitArrow(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		Direction4 arrowDir = properties.get(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class);
		body = new PitArrowBody(this, agency.getWorld(), AP_Tool.getCenter(properties),
				AP_Tool.getVelocity(properties), arrowDir);
		brain = new PitArrowBrain(this, (PitArrowBody) body,
				properties.get(CommonKV.KEY_PARENT_AGENT, null, Pit.class),
				properties.containsKey(CommonKV.Spawn.KEY_EXPIRE), arrowDir);
		sprite = new PitArrowSprite(agency.getAtlas(), body.getPosition(), arrowDir);
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { brain.processContactFrame(body.processContactFrame()); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) {
				sprite.processFrame(brain.processFrame(body.processFrame(delta)));
			}
		});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
			@Override
			public void draw(Eye eye) { eye.draw(sprite); }
		});
	}

	@Override
	protected Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	protected Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	protected Vector2 getVelocity() {
		return body.getVelocity();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}

	// make the AgentProperties (AP) for this class of Agent
	public static ObjectProperties makeAP(Pit parentAgent, Vector2 position, Vector2 velocity,
			Direction4 arrowDir, boolean isExpireImmediately) {
		ObjectProperties props = AP_Tool.createPointAP(KidIcarusKV.AgentClassAlias.VAL_PIT_ARROW,
				position, velocity);
		props.put(CommonKV.KEY_PARENT_AGENT, parentAgent);
		props.put(CommonKV.KEY_DIRECTION, arrowDir);
		if(isExpireImmediately)
			props.put(CommonKV.Spawn.KEY_EXPIRE, true);
		return props;
	}
}
