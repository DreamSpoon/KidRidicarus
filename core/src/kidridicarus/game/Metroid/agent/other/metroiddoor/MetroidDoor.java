package kidridicarus.game.Metroid.agent.other.metroiddoor;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentPropertyListener;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.optional.SolidAgent;
import kidridicarus.common.agent.optional.TriggerTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.SprFrameTool;

public class MetroidDoor extends CorpusAgent implements SolidAgent, TriggerTakeAgent, ContactDmgTakeAgent {
	private MetroidDoorBrain brain;
	private MetroidDoorSprite sprite;

	public MetroidDoor(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		body = new MetroidDoorBody(this, agentHooks.getWorld(), AP_Tool.getCenter(properties));
		boolean isFacingRight = properties.getDirection4(CommonKV.KEY_DIRECTION, Direction4.NONE).isRight();
		brain = new MetroidDoorBrain(agentHooks, (MetroidDoorBody) body, isFacingRight);
		sprite = new MetroidDoorSprite(agentHooks.getAtlas(), SprFrameTool.placeFaceR(body.getPosition(), isFacingRight));
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(brain.processFrame(frameTime)); }
			});
		agentHooks.addDrawListener(CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
		agentHooks.createAgentRemoveListener(this, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { dispose(); }
			});

		final String strName = properties.getString(CommonKV.Script.KEY_NAME, null);
		agentHooks.addPropertyListener(true, CommonKV.Script.KEY_NAME,
				new AgentPropertyListener<String>(String.class) {
				@Override
				public String getValue() { return strName; }
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
}
