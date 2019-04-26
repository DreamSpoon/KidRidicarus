package kidridicarus.game.SMB1.agent.item.mushroom;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.fullactor.FullActor;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.SMB1.agent.BumpTakeAgent;

public abstract class BaseMushroom extends FullActor implements BumpTakeAgent, DisposableAgent {
	protected abstract TextureRegion getMushroomTexture(TextureAtlas atlas);
	protected abstract Powerup getPowerupPow();

	public BaseMushroom(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { brain.processContactFrame(body.processContactFrame()); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { sprite.processFrame(brain.processFrame(body.processFrame(delta))); }
			});
		body = new BaseMushroomBody(this, agency.getWorld());
		brain = new BaseMushroomBrain(this, (BaseMushroomBody) body, AP_Tool.getCenter(properties), getPowerupPow());
		sprite = new BaseMushroomSprite(this, getMushroomTexture(agency.getAtlas()),
				((BaseMushroomBrain) brain).getSproutStartPos());
	}

	@Override
	public void onTakeBump(Agent bumpingAgent) {
		((BaseMushroomBrain) brain).onTakeBump(bumpingAgent);
	}

	@Override
	protected Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	protected Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	protected Vector2 getVelocity() {
		return body.getVelocity();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
