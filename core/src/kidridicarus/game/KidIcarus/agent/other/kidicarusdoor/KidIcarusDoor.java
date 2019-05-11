package kidridicarus.game.KidIcarus.agent.other.kidicarusdoor;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.agent.optional.SolidAgent;
import kidridicarus.common.agent.optional.TriggerTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;

// note: solid when closed, non-solid when open
public class KidIcarusDoor extends CorpusAgent implements TriggerTakeAgent, SolidAgent {
	private KidIcarusDoorBrain brain;
	private KidIcarusDoorSprite sprite;

	public KidIcarusDoor(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		// default to open state unless Agent is supposed to "expire immediately" (a closed door is a dead door?)
		boolean isOpened = !properties.getBoolean(CommonKV.Spawn.KEY_EXPIRE, false);
		body = new KidIcarusDoorBody(this, agentHooks.getWorld(), AP_Tool.getCenter(properties), isOpened);
		brain = new KidIcarusDoorBrain(this, agentHooks, (KidIcarusDoorBody) body, isOpened,
				AP_Tool.getTargetName(properties));
		sprite = new KidIcarusDoorSprite(agentHooks.getAtlas(), body.getPosition(), isOpened);
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((KidIcarusDoorBody) body).processContactFrame());
				}
			});
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(brain.processFrame()); }
			});
		agentHooks.addDrawListener(CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
		agentHooks.createAgentRemoveListener(this, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	@Override
	public void onTakeTrigger() {
		brain.setOpened(false);
	}
}
