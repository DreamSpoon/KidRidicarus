package kidridicarus.game.SMB1.agent.item.mushroom;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.Agent;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.SMB1.agent.BumpTakeAgent;

abstract class BaseMushroom extends CorpusAgent implements BumpTakeAgent {
	protected abstract TextureRegion getMushroomTexture(TextureAtlas atlas);
	protected abstract Powerup getPowerupPow();

	private BaseMushroomBrain brain;
	private BaseMushroomSprite sprite;

	BaseMushroom(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		body = new BaseMushroomBody(this, agentHooks.getWorld());
		brain = new BaseMushroomBrain(agentHooks, (BaseMushroomBody) body, AP_Tool.getCenter(properties),
				getPowerupPow());
		sprite = new BaseMushroomSprite(agentHooks, getMushroomTexture(agentHooks.getAtlas()),
				brain.getSproutStartPos());
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((BaseMushroomBody) body).processContactFrame());
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
}
