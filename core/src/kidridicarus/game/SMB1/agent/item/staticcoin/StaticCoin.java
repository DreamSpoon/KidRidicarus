package kidridicarus.game.SMB1.agent.item.staticcoin;

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

public class StaticCoin extends CorpusAgent {
	private StaticCoinBrain brain;
	private StaticCoinSprite sprite;

	public StaticCoin(AgentHooks agentHooks, ObjectProperties agentProps) {
		super(agentHooks, agentProps);
		body = new StaticCoinBody(this, agentHooks.getWorld(), AP_Tool.getCenter(agentProps));
		brain = new StaticCoinBrain(agentHooks, (StaticCoinBody) body);
		sprite = new StaticCoinSprite(agentHooks.getAtlas(), AP_Tool.getCenter(agentProps));
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((StaticCoinBody) body).processContactFrame());
				}
			});
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
	}
}
