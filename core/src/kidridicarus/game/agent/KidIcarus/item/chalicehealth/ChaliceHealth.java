package kidridicarus.game.agent.KidIcarus.item.chalicehealth;

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
import kidridicarus.game.info.KidIcarusAudio;
import kidridicarus.game.info.KidIcarusKV;
import kidridicarus.game.powerup.KidIcarusPow;

public class ChaliceHealth extends Agent implements DisposableAgent {
	private ChaliceHealthBody body;
	private ChaliceHealthSprite sprite;
	private boolean isPowerupUsed;

	public ChaliceHealth(Agency agency, ObjectProperties agentProps) {
		super(agency, agentProps);
		isPowerupUsed = false;
		body = new ChaliceHealthBody(this, agency.getWorld(), Agent.getStartPoint(agentProps));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(); }
			});
		sprite = new ChaliceHealthSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { doDraw(adBatch); }
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
		if(taker.onTakePowerup(new KidIcarusPow.ChaliceHealthPow()))
			isPowerupUsed = true;
	}

	private void doUpdate() {
		if(isPowerupUsed) {
			agency.removeAgent(this);
			agency.getEar().playSound(KidIcarusAudio.Sound.General.HEART_PICKUP);
		}
	}

	private void doDraw(Eye adBatch) {
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
		return Agent.createPointAP(KidIcarusKV.AgentClassAlias.VAL_CHALICE_HEALTH, position);
	}
}
