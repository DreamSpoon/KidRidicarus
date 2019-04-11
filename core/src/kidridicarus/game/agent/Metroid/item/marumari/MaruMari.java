package kidridicarus.game.agent.Metroid.item.marumari;

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
import kidridicarus.game.info.MetroidAudio;
import kidridicarus.game.powerup.MetroidPow;

public class MaruMari extends Agent implements DisposableAgent {
	private MaruMariBody body;
	private MaruMariSprite sprite;
	private boolean isPowerupUsed;
	
	public MaruMari(Agency agency, ObjectProperties agentProps) {
		super(agency, agentProps);

		isPowerupUsed = false;

		body = new MaruMariBody(this, agency.getWorld(), Agent.getStartPoint(agentProps));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = new MaruMariSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
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
		if(taker.onTakePowerup(new MetroidPow.MaruMariPow()))
			isPowerupUsed = true;
	}

	private void doUpdate(float delta) {
		if(isPowerupUsed) {
			agency.getEar().startSinglePlayMusic(MetroidAudio.Music.GET_ITEM);
			agency.removeAgent(this);
		}

		sprite.update(delta, body.getPosition());
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
}
