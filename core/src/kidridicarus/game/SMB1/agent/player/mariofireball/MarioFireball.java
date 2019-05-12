package kidridicarus.game.SMB1.agent.player.mariofireball;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.SMB1.SMB1_KV;
import kidridicarus.game.SMB1.agent.player.mario.Mario;

public class MarioFireball extends CorpusAgent {
	private MarioFireballBrain brain;
	private MarioFireballSprite sprite;

	public MarioFireball(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		boolean isFacingRight;
		// fireball on right?
		if(properties.getDirection4(CommonKV.KEY_DIRECTION, Direction4.NONE).isRight()) {
			isFacingRight = true;
			body = new MarioFireballBody(this, agentHooks.getWorld(), AP_Tool.getCenter(properties),
					MarioFireballSpine.MOVE_VEL.cpy().scl(1, -1));
		}
		// fireball on left
		else {
			isFacingRight = false;
			body = new MarioFireballBody(this, agentHooks.getWorld(), AP_Tool.getCenter(properties),
					MarioFireballSpine.MOVE_VEL.cpy().scl(-1, -1));
		}
		brain = new MarioFireballBrain(properties.get(CommonKV.KEY_PARENT_AGENT, null, Mario.class), agentHooks,
				(MarioFireballBody) body, isFacingRight);
		sprite = new MarioFireballSprite(agentHooks.getAtlas(), body.getPosition());
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((MarioFireballBody) body).processContactFrame());
				}
			});
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(brain.processFrame(frameTime)); }
			});
		agentHooks.addDrawListener(CommonInfo.DrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
		agentHooks.createAgentRemoveListener(this, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
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
