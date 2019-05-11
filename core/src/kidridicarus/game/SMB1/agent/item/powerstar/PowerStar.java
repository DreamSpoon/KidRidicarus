package kidridicarus.game.SMB1.agent.item.powerstar;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.Agent;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.SMB1.agent.BumpTakeAgent;
import kidridicarus.game.info.SMB1_KV;

/*
 * TODO:
 * -allow the star to spawn down-right out of bricks like on level 1-1
 * -test the star's onBump method - I could not bump it, needs precise timing - maybe loosen the timing? 
 */
public class PowerStar extends CorpusAgent implements BumpTakeAgent {
	private PowerStarBrain brain;
	private PowerStarSprite sprite;

	public PowerStar(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		body = new PowerStarBody(this, agentHooks.getWorld());
		brain = new PowerStarBrain(agentHooks, (PowerStarBody) body, AP_Tool.getCenter(properties));
		sprite = new PowerStarSprite(agentHooks, agentHooks.getAtlas(), brain.getSproutStartPos());
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(FrameTime frameTime) {
				brain.processContactFrame(((PowerStarBody) body).processContactFrame());
			}
		});
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(brain.processFrame(frameTime)); }
			});
		agentHooks.createAgentRemoveListener(this, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	@Override
	public void onTakeBump(Agent bumpingAgent) {
		brain.onTakeBump(bumpingAgent);
	}

	public static ObjectProperties makeAP(Vector2 position) {
		return AP_Tool.createPointAP(SMB1_KV.AgentClassAlias.VAL_POWERSTAR, position);
	}
}
