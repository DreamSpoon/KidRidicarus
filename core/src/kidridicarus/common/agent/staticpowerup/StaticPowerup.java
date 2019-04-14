package kidridicarus.common.agent.staticpowerup;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.powerup.Powerup;

public abstract class StaticPowerup extends Agent implements DisposableAgent {
	protected StaticPowerupBody body;
	protected StaticPowerupSprite sprite;
	private boolean isUsed;

	// return false if wanting to exit StaticPowerup update method after method returns
	protected abstract boolean doPowerupUpdate(float delta, boolean isPowUsed);
	// return the type of Powerup to pass to the PowerupTakeAgent that contacted this Agent
	protected abstract Powerup getStaticPowerupPow();

	public StaticPowerup(Agency agency, ObjectProperties agentProps) {
		super(agency, agentProps);
		isUsed = false;
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { doDraw(adBatch); }
			});
	}

	// if any agents touching this powerup are able to take it, then push it to them
	private void doContactUpdate() {
		// exit if not used or body not created yet
		if(isUsed || body == null)
			return;
		// any takers?
		PowerupTakeAgent taker = body.getSpine().getTouchingPowerupTaker();
		if(taker == null)
			return;
		if(taker.onTakePowerup(getStaticPowerupPow()))
			isUsed = true;
	}

	private void doUpdate(float delta) {
		// exit if other update function returned false
		if(doPowerupUpdate(delta, isUsed) == false)
			return;
		// remove agent if used
		if(isUsed)
			agency.removeAgent(this);
		// otherwise update sprite
		else if(sprite != null && body != null)
			sprite.update(delta, false, body.getPosition());
	}

	private void doDraw(Eye adBatch) {
		// do not draw powerup if it is used already
		if(sprite != null && !isUsed)
			adBatch.draw(sprite);
	}

	@Override
	public Vector2 getPosition() {
		if(body == null)
			return null;
		return body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		if(body == null)
			return null;
		return body.getBounds();
	}

	@Override
	public void disposeAgent() {
		if(body != null) {
			body.dispose();
			body = null;
		}
		sprite = null;
	}
}
