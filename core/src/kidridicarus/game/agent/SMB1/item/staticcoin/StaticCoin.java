package kidridicarus.game.agent.SMB1.item.staticcoin;

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
import kidridicarus.game.info.SMB1_Audio;
import kidridicarus.game.powerup.SMB1_Pow;

public class StaticCoin extends Agent implements DisposableAgent {
	private StaticCoinBody body;
	private StaticCoinSprite sprite;
	private boolean isPowerupUsed;

	public StaticCoin(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		isPowerupUsed = false;

		body = new StaticCoinBody(this, agency.getWorld(), Agent.getStartPoint(properties));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(); }
			});
		sprite = new StaticCoinSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_BOTTOM, new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { doDraw(adBatch); }
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
		if(taker.onTakePowerup(new SMB1_Pow.CoinPow()))
			isPowerupUsed = true;
	}

	private void doUpdate() {
		if(isPowerupUsed) {
			agency.getEar().playSound(SMB1_Audio.Sound.COIN);
			agency.removeAgent(this);
		}

		sprite.update(agency.getGlobalTimer());
	}

	private void doDraw(Eye adBatch){
		// do not draw sprite if powerup is used
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
}