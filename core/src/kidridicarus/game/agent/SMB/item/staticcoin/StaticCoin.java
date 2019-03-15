package kidridicarus.game.agent.SMB.item.staticcoin;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.game.info.PowerupInfo.PowType;

public class StaticCoin extends Agent implements DisposableAgent {
	private StaticCoinBody body;
	private StaticCoinSprite sprite;
	private boolean isPowerupUsed;

	public StaticCoin(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		isPowerupUsed = false;

		body = new StaticCoinBody(this, agency.getWorld(), Agent.getStartPoint(properties));
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.CONTACT_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = new StaticCoinSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_BOTTOM, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	// if any agents touching this powerup are able to take it, then push it to them
	private void doContactUpdate() {
		// exit if not used or body not created yet
		if(isPowerupUsed || body == null)
			return;
		// any takers?
		PowerupTakeAgent taker = body.getSpine().getTouchingPowerupTaker();
		if(taker == null)
			return;
		// if powerup is taken then set used flag
		if(taker.onTakePowerup(PowType.COIN))
			isPowerupUsed = true;
	}

	private void doUpdate(float delta) {
		if(isPowerupUsed)
			agency.disposeAgent(this);

		sprite.update(agency.getGlobalTimer());
	}

	public void doDraw(AgencyDrawBatch batch){
		// do not draw sprite if powerup is used
		if(isPowerupUsed)
			return;
		batch.draw(sprite);
	}

	@Override
	public Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}