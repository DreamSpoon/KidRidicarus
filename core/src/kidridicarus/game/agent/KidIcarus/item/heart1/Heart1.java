package kidridicarus.game.agent.KidIcarus.item.heart1;

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
import kidridicarus.game.info.KidIcarusAudio;
import kidridicarus.game.powerup.KidIcarusPow;

public class Heart1 extends Agent implements DisposableAgent {
	private static final float LIVE_TIME = 23/6f;

	private Heart1Body body;
	private Heart1Sprite sprite;
	private boolean isPowerupUsed;
	private float moveStateTimer;

	public Heart1(Agency agency, ObjectProperties agentProps) {
		super(agency, agentProps);

		isPowerupUsed = false;
		moveStateTimer = 0f;

		body = new Heart1Body(this, agency.getWorld(), Agent.getStartPoint(agentProps));
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.CONTACT_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = new Heart1Sprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
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
		if(taker.onTakePowerup(new KidIcarusPow.HeartsPow(1)))
			isPowerupUsed = true;
	}

	private void doUpdate(float delta) {
		if(isPowerupUsed)
			agency.getEar().playSound(KidIcarusAudio.Sound.General.HEART_PICKUP);
		if(isPowerupUsed || moveStateTimer > LIVE_TIME)
			agency.removeAgent(this);

		moveStateTimer += delta;
	}

	private void doDraw(AgencyDrawBatch batch) {
		// do not draw powerup if it is used already
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
