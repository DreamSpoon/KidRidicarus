package kidridicarus.game.agent.KidIcarus.item.angelheart;

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
import kidridicarus.game.info.KidIcarusKV;
import kidridicarus.game.powerup.KidIcarusPow;

public class AngelHeart extends Agent implements DisposableAgent {
	private static final float LIVE_TIME = 23/6f;

	enum AngelHeartSize { SMALL, HALF, FULL }

	private AngelHeartBody body;
	private AngelHeartSprite sprite;
	private int heartCount;
	private boolean isPowerupUsed;
	private float moveStateTimer;

	public AngelHeart(Agency agency, ObjectProperties agentProps) {
		super(agency, agentProps);

		isPowerupUsed = false;
		moveStateTimer = 0f;
		heartCount = agentProps.get(KidIcarusKV.KEY_HEART_COUNT, 1, Integer.class);
		AngelHeartSize heartSize; 
		switch(heartCount) {
			case 1:
				heartSize = AngelHeartSize.SMALL;
				break;
			case 5:
				heartSize = AngelHeartSize.HALF;
				break;
			case 10:
				heartSize = AngelHeartSize.FULL;
				break;
			default:
				throw new IllegalStateException(
						"Unable to spawn this Agent because of irregular heart count: "+heartCount);
		}

		body = new AngelHeartBody(this, agency.getWorld(), Agent.getStartPoint(agentProps));
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.CONTACT_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = new AngelHeartSprite(agency.getAtlas(), body.getPosition(), heartSize);
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch adBatch) { doDraw(adBatch); }
			});
	}

	// if any agents touching this powerup are able to take it, then push it to them
	private void doContactUpdate() {
		// exit if not used or if body is missing
		if(isPowerupUsed || body == null)
			return;
		// any takers?
		PowerupTakeAgent taker = body.getSpine().getTouchingPowerupTaker();
		if(taker == null)
			return;
		if(taker.onTakePowerup(new KidIcarusPow.AngelHeartPow(heartCount)))
			isPowerupUsed = true;
	}

	private void doUpdate(float delta) {
		if(isPowerupUsed || moveStateTimer > LIVE_TIME) {
			// if used during lifetime then play sound, otherwise this is life timeout despawn
			if(moveStateTimer <= LIVE_TIME)
				agency.getEar().playSound(KidIcarusAudio.Sound.General.HEART_PICKUP);
			agency.removeAgent(this);
			return;
		}
		moveStateTimer += delta;
	}

	private void doDraw(AgencyDrawBatch adBatch) {
		// do not draw powerup if it is used already
		if(isPowerupUsed)
			return;

		adBatch.draw(sprite);
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

	public static ObjectProperties makeAP(Vector2 position, int heartCount) {
		ObjectProperties props = Agent.createPointAP(KidIcarusKV.AgentClassAlias.VAL_ANGEL_HEART, position);
		props.put(KidIcarusKV.KEY_HEART_COUNT, heartCount);
		return props;
	}
}
