package kidridicarus.game.KidIcarus.agent.item.chalicehealth;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;

public class ChaliceHealth extends CorpusAgent {
	private ChaliceHealthBrain brain;
	private ChaliceHealthSprite sprite;

	public ChaliceHealth(Agency agency, ObjectProperties agentProps) {
		super(agency, agentProps);
		body = new ChaliceHealthBody(this, agency.getWorld(), AP_Tool.getCenter(agentProps));
		brain = new ChaliceHealthBrain(this, (ChaliceHealthBody) body);
		sprite = new ChaliceHealthSprite(agency.getAtlas(), AP_Tool.getCenter(agentProps));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((ChaliceHealthBody) body).processContactFrame());
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
}
