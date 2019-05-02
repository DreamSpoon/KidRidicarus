package kidridicarus.game.SMB1.agent.other.flagpole;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.agent.optional.TriggerTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;

public class Flagpole extends CorpusAgent implements TriggerTakeAgent, DisposableAgent {
	private FlagpoleBrain brain;
	private PoleFlagSprite sprite;

	public Flagpole(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new FlagpoleBody(this, agency.getWorld(), AP_Tool.getBounds(properties));
		brain = new FlagpoleBrain(this, (FlagpoleBody) body);
		sprite = new PoleFlagSprite(agency.getAtlas(), brain.getFlagPosAtTop());
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) {
					brain.processContactFrame(((FlagpoleBody) body).processContactFrame());
				}
			});
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
	public void disposeAgent() {
		dispose();
	}
}
