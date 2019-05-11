package kidridicarus.game.KidIcarus.agent.item.chalicehealth;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveCallback;
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

	public ChaliceHealth(AgentHooks agentHooks, ObjectProperties agentProps) {
		super(agentHooks, agentProps);
		body = new ChaliceHealthBody(this, agentHooks.getWorld(), AP_Tool.getCenter(agentProps));
		brain = new ChaliceHealthBrain(agentHooks, (ChaliceHealthBody) body);
		sprite = new ChaliceHealthSprite(agentHooks.getAtlas(), AP_Tool.getCenter(agentProps));
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((ChaliceHealthBody) body).processContactFrame());
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
}
