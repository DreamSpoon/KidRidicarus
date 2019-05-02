package kidridicarus.game.Metroid.agent.other.metroiddoor;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.optional.SolidAgent;
import kidridicarus.common.agent.optional.TriggerTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.SprFrameTool;

public class MetroidDoor extends CorpusAgent implements SolidAgent, TriggerTakeAgent, ContactDmgTakeAgent,
		DisposableAgent {
	private MetroidDoorBrain brain;
	private MetroidDoorSprite sprite;

	public MetroidDoor(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new MetroidDoorBody(this, agency.getWorld(), AP_Tool.getCenter(properties));
		boolean isFacingRight = properties.containsKV(CommonKV.KEY_DIRECTION, CommonKV.VAL_RIGHT);
		brain = new MetroidDoorBrain(this, (MetroidDoorBody) body, isFacingRight);
		sprite = new MetroidDoorSprite(agency.getAtlas(), SprFrameTool.placeFaceR(body.getPosition(), isFacingRight));
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
		brain.onTakeTrigger();
	}

	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		return brain.onTakeDamage(agent);
	}

	@Override
	public void disposeAgent() {
		dispose();
	}
}
