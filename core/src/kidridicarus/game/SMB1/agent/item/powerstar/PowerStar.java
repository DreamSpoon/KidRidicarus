package kidridicarus.game.SMB1.agent.item.powerstar;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.Agent;
import kidridicarus.agency.agent.AgentRemoveListener;
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

	public PowerStar(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new PowerStarBody(this, agency.getWorld());
		brain = new PowerStarBrain(this, (PowerStarBody) body, AP_Tool.getCenter(properties));
		sprite = new PowerStarSprite(this, agency.getAtlas(), brain.getSproutStartPos());
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(FrameTime frameTime) {
				brain.processContactFrame(((PowerStarBody) body).processContactFrame());
			}
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(brain.processFrame(frameTime)); }
			});
		agency.addAgentRemoveListener(new AgentRemoveListener(this, this) {
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
