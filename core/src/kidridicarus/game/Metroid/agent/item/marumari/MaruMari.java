package kidridicarus.game.Metroid.agent.item.marumari;

import kidridicarus.agency.Agency;
import kidridicarus.agency.FrameTime;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;

public class MaruMari extends CorpusAgent implements DisposableAgent {
	private MaruMariBrain brain;
	private MaruMariSprite sprite;

	public MaruMari(Agency agency, ObjectProperties agentProps) {
		super(agency, agentProps);
		body = new MaruMariBody(this, agency.getWorld(), AP_Tool.getCenter(agentProps));
		brain = new MaruMariBrain(this, (MaruMariBody) body);
		sprite = new MaruMariSprite(agency.getAtlas(), AP_Tool.getCenter(agentProps));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((MaruMariBody) body).processContactFrame());
				}
			});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					sprite.processFrame(brain.processFrame(frameTime));
				}
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
	}

	@Override
	public void disposeAgent() {
		dispose();
	}
}
