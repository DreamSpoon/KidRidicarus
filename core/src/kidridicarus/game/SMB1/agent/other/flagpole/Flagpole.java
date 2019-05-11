package kidridicarus.game.SMB1.agent.other.flagpole;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveCallback;
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

	public Flagpole(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		body = new FlagpoleBody(this, agentHooks.getWorld(), AP_Tool.getBounds(properties));
		brain = new FlagpoleBrain(this, (FlagpoleBody) body);
		sprite = new PoleFlagSprite(agentHooks.getAtlas(), brain.getFlagPosAtTop());
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((FlagpoleBody) body).processContactFrame());
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

	@Override
	public void onTakeTrigger() {
		brain.onTakeTrigger();
	}
}
