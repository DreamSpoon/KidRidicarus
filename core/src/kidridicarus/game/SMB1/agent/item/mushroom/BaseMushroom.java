package kidridicarus.game.SMB1.agent.item.mushroom;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import kidridicarus.agency.Agency;
import kidridicarus.agency.Agent;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.SMB1.agent.BumpTakeAgent;

public abstract class BaseMushroom extends CorpusAgent implements BumpTakeAgent {
	protected abstract TextureRegion getMushroomTexture(TextureAtlas atlas);
	protected abstract Powerup getPowerupPow();

	private BaseMushroomBrain brain;
	private BaseMushroomSprite sprite;

	public BaseMushroom(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new BaseMushroomBody(this, agency.getWorld());
		brain = new BaseMushroomBrain(this, (BaseMushroomBody) body, AP_Tool.getCenter(properties), getPowerupPow());
		sprite = new BaseMushroomSprite(this, getMushroomTexture(agency.getAtlas()), brain.getSproutStartPos());
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((BaseMushroomBody) body).processContactFrame());
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
}
