package kidridicarus.game.KidIcarus.agent.other.kidicarusdoor;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveListener;
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

	public KidIcarusDoor(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		// default to open state unless Agent is supposed to "expire immediately" (a closed door is a dead door?)
		boolean isOpened = !properties.getBoolean(CommonKV.Spawn.KEY_EXPIRE, false);
		body = new KidIcarusDoorBody(this, agency.getWorld(), AP_Tool.getCenter(properties), isOpened);
		brain = new KidIcarusDoorBrain(this, (KidIcarusDoorBody) body, isOpened, AP_Tool.getTargetName(properties));
		sprite = new KidIcarusDoorSprite(agency.getAtlas(), body.getPosition(), isOpened);
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((KidIcarusDoorBody) body).processContactFrame());
				}
			});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(brain.processFrame()); }
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
		agency.addAgentRemoveListener(new AgentRemoveListener(this, this) {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	@Override
	public void onTakeTrigger() {
		brain.setOpened(false);
	}
}
