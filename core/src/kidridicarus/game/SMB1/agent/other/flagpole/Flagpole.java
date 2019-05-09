package kidridicarus.game.SMB1.agent.other.flagpole;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.agent.optional.TriggerTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;

public class Flagpole extends CorpusAgent implements TriggerTakeAgent {
	private FlagpoleBrain brain;
	private PoleFlagSprite sprite;

	public Flagpole(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new FlagpoleBody(this, agency.getWorld(), AP_Tool.getBounds(properties));
		brain = new FlagpoleBrain(this, (FlagpoleBody) body);
		sprite = new PoleFlagSprite(agency.getAtlas(), brain.getFlagPosAtTop());
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((FlagpoleBody) body).processContactFrame());
				}
			});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(brain.processFrame(frameTime)); }
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
		brain.onTakeTrigger();
	}
}
