package kidridicarus.game.Metroid.agent.item.energy;

import com.badlogic.gdx.math.Vector2;

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
import kidridicarus.game.info.MetroidKV;

public class Energy extends CorpusAgent {
	private EnergyBrain brain;
	private EnergySprite sprite;

	public Energy(Agency agency, ObjectProperties agentProps) {
		super(agency, agentProps);
		body = new EnergyBody(this, agency.getWorld(), AP_Tool.getCenter(agentProps));
		brain = new EnergyBrain(this, (EnergyBody) body);
		sprite = new EnergySprite(agency.getAtlas(), AP_Tool.getCenter(agentProps));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((EnergyBody) body).processContactFrame());
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
		agency.addAgentRemoveListener(new AgentRemoveListener(this, this) {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	public static ObjectProperties makeAP(Vector2 position) {
		return AP_Tool.createPointAP(MetroidKV.AgentClassAlias.VAL_ENERGY, position);
	}
}
