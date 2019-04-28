package kidridicarus.game.SMB1.agent.player.mariofireball;

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
import kidridicarus.game.SMB1.agent.player.mario.Mario;
import kidridicarus.game.info.SMB1_KV;

public class MarioFireball extends FullActor implements DisposableAgent {
	public MarioFireball(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		boolean isFacingRight;
		// fireball on right?
		if(properties.containsKV(CommonKV.KEY_DIRECTION, CommonKV.VAL_RIGHT)) {
			isFacingRight = true;
			body = new MarioFireballBody(this, agency.getWorld(), AP_Tool.getCenter(properties),
					MarioFireballSpine.MOVE_VEL.cpy().scl(1, -1));
		}
		// fireball on left
		else {
			isFacingRight = false;
			body = new MarioFireballBody(this, agency.getWorld(), AP_Tool.getCenter(properties),
					MarioFireballSpine.MOVE_VEL.cpy().scl(-1, -1));
		}
		brain = new MarioFireballBrain(this, (MarioFireballBody) body,
				properties.get(CommonKV.KEY_PARENT_AGENT, null, Mario.class), isFacingRight);
		sprite = new MarioFireballSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { brain.processContactFrame(body.processContactFrame()); }
			});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { sprite.processFrame(brain.processFrame(body.processFrame(delta))); }
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

	public static ObjectProperties makeAP(Vector2 position, boolean right, Mario parentAgent) {
		ObjectProperties props = AP_Tool.createPointAP(SMB1_KV.AgentClassAlias.VAL_MARIOFIREBALL, position);
		props.put(CommonKV.KEY_PARENT_AGENT, parentAgent);
		if(right)
			props.put(CommonKV.KEY_DIRECTION, CommonKV.VAL_RIGHT);
		else
			props.put(CommonKV.KEY_DIRECTION, CommonKV.VAL_LEFT);
		return props;
	}
}
