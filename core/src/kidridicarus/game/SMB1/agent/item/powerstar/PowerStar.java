package kidridicarus.game.SMB1.agent.item.powerstar;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.fullactor.FullActor;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.SMB1.agent.BumpTakeAgent;
import kidridicarus.game.info.SMB1_KV;

/*
 * TODO:
 * -allow the star to spawn down-right out of bricks like on level 1-1
 * -test the star's onBump method - I could not bump it, needs precise timing - maybe loosen the timing? 
 */
public class PowerStar extends FullActor implements BumpTakeAgent, DisposableAgent {
	public PowerStar(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { brain.processContactFrame(body.processContactFrame()); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { sprite.processFrame(brain.processFrame(body.processFrame(delta))); }
			});
		body = new PowerStarBody(this, agency.getWorld());
		brain = new PowerStarBrain(this, (PowerStarBody) body, AP_Tool.getCenter(properties));
		sprite = new PowerStarSprite(this, agency.getAtlas(), ((PowerStarBrain) brain).getSproutStartPos());
	}

	@Override
	public void onTakeBump(Agent bumpingAgent) {
		((PowerStarBrain) brain).onTakeBump(bumpingAgent);
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

	public static ObjectProperties makeAP(Vector2 position) {
		return AP_Tool.createPointAP(SMB1_KV.AgentClassAlias.VAL_POWERSTAR, position);
	}
}
