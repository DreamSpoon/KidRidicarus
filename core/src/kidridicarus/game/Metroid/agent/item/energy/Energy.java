package kidridicarus.game.Metroid.agent.item.energy;

import com.badlogic.gdx.math.Vector2;

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
import kidridicarus.game.info.MetroidKV;

public class Energy extends CorpusAgent {
	private EnergyBrain brain;
	private EnergySprite sprite;

	public Energy(AgentHooks agentHooks, ObjectProperties agentProps) {
		super(agentHooks, agentProps);
		body = new EnergyBody(this, agentHooks.getWorld(), AP_Tool.getCenter(agentProps));
		brain = new EnergyBrain(agentHooks, (EnergyBody) body);
		sprite = new EnergySprite(agentHooks.getAtlas(), AP_Tool.getCenter(agentProps));
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((EnergyBody) body).processContactFrame());
				}
			});
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					sprite.processFrame(brain.processFrame(frameTime));
				}
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

	public static ObjectProperties makeAP(Vector2 position) {
		return AP_Tool.createPointAP(MetroidKV.AgentClassAlias.VAL_ENERGY, position);
	}
}
