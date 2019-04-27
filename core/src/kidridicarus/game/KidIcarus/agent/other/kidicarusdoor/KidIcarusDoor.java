package kidridicarus.game.KidIcarus.agent.other.kidicarusdoor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.halfactor.HalfActor;
import kidridicarus.common.agent.optional.SolidAgent;
import kidridicarus.common.agent.optional.TriggerTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;

// note: solid when closed, non-solid when open
public class KidIcarusDoor extends HalfActor implements TriggerTakeAgent, SolidAgent, DisposableAgent {
	public KidIcarusDoor(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		// start in the "is open" state if the agent is not supposed to expire (i.e. close) immediately
		boolean isOpened = !properties.containsKV(CommonKV.Spawn.KEY_EXPIRE, true);
		body = new KidIcarusDoorBody(this, agency.getWorld(), AP_Tool.getCenter(properties), isOpened);
		brain = new KidIcarusDoorBrain(this, (KidIcarusDoorBody) body, isOpened,
				properties.get(CommonKV.Script.KEY_TARGET_NAME, "", String.class));
		sprite = new KidIcarusDoorSprite(agency.getAtlas(), body.getPosition(), isOpened);
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { brain.processContactFrame(body.processContactFrame()); }
			});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { sprite.processFrame(brain.processFrame(delta)); }
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
	}

	@Override
	public void onTakeTrigger() {
		((KidIcarusDoorBrain) brain).setOpened(false);
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
	public void disposeAgent() {
		body.dispose();
	}
}
