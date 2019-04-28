package kidridicarus.game.Metroid.agent.other.metroiddoor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.optional.SolidAgent;
import kidridicarus.common.agent.optional.TriggerTakeAgent;
import kidridicarus.common.agent.quarteractor.QuarterActor;
import kidridicarus.common.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;

public class MetroidDoor extends QuarterActor implements SolidAgent, TriggerTakeAgent, ContactDmgTakeAgent,
		DisposableAgent {
	public MetroidDoor(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new MetroidDoorBody(this, agency.getWorld(), AP_Tool.getCenter(properties));
		boolean isFacingRight = properties.containsKV(CommonKV.KEY_DIRECTION, CommonKV.VAL_RIGHT);
		brain = new MetroidDoorBrain(this, (MetroidDoorBody) body, isFacingRight);
		sprite = new MetroidDoorSprite(agency.getAtlas(),
				new SpriteFrameInput(true, body.getPosition(), !isFacingRight));
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
		((MetroidDoorBrain) brain).onTakeTrigger();
	}

	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		return ((MetroidDoorBrain) brain).onTakeDamage(agent);
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
