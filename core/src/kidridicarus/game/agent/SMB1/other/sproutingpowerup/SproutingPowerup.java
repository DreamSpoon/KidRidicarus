package kidridicarus.game.agent.SMB1.other.sproutingpowerup;

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
import kidridicarus.common.agentsprite.basic.AnimSprite;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.powerup.Powerup;

public abstract class SproutingPowerup extends Agent implements DisposableAgent {
	private static final float SPROUT_TIME = 1f;
	private static final float SPROUT_OFFSET = UInfo.P2M(-13f);

	private enum SproutingMoveState { SPROUT, WALK, END }

	protected SproutingPowerupBody body;
	protected AnimSprite sprite;
	private AgentDrawListener myDrawListener;
	private Vector2 initSpawnPosition;
	private PowerupTakeAgent powerupTaker;
	private float sproutingMoveStateTimer;
	private SproutingMoveState sproutingMoveState;

	protected abstract void finishSprout();
	protected abstract void postSproutUpdate(PowerupTakeAgent powerupTaker);
	// return the type of Powerup to pass to the PowerupTakeAgent that contacted this Agent
	protected abstract Powerup getPowerupPow();

	public SproutingPowerup(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		powerupTaker = null;
		initSpawnPosition = Agent.getStartPoint(properties);
		sproutingMoveStateTimer = 0f;
		sproutingMoveState = SproutingMoveState.SPROUT;

		body = null;
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = null;
		myDrawListener = new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { doDraw(adBatch); }
			};
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_BOTTOM, myDrawListener);
	}

	protected Vector2 getSproutStartPos() {
		return initSpawnPosition.cpy().add(0f, SPROUT_OFFSET);
	}

	protected Vector2 getSproutEndPos() {
		return initSpawnPosition;
	}

	// if any agents touching this powerup are able to take it, then push it to them
	private void doContactUpdate() {
		// exit if used or body not created yet
		if(powerupTaker != null || body == null)
			return;
		// any takers?
		PowerupTakeAgent taker = body.getSpine().getTouchingPowerupTaker();
		if(taker == null)
			return;
		// if powerup is taken then set used flag
		if(taker.onTakePowerup(getPowerupPow()))
			powerupTaker = taker;
	}

	private void doUpdate(float delta) {
		processMove(delta);
		processSprite(delta);
	}

	private void processMove(float delta) {
		SproutingMoveState nextMoveState = getNextMoveState();
		boolean moveStateChanged = nextMoveState != sproutingMoveState;
		switch(nextMoveState) {
			case SPROUT:
				break;
			case WALK:
				// if just finished sprouting then create agent body and change sprite draw order
				if(moveStateChanged) {
					// change from bottom to middle sprite draw order
					agency.removeAgentDrawListener(this, myDrawListener);
					myDrawListener = new AgentDrawListener() {
							@Override
							public void draw(Eye adBatch) { doDraw(adBatch); }
						};
					agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_MIDDLE, myDrawListener);

					finishSprout();
				}
				else
					postSproutUpdate(null);
				break;
			case END:
				postSproutUpdate(powerupTaker);

				// powerup used, so dispose this agent
				agency.removeAgent(this);
				break;
		}
		sproutingMoveStateTimer = moveStateChanged ? 0f : sproutingMoveStateTimer+delta;
		sproutingMoveState = nextMoveState;
	}

	private SproutingMoveState getNextMoveState() {
		if(powerupTaker != null)
			return SproutingMoveState.END;
		else if(sproutingMoveState == SproutingMoveState.WALK || (sproutingMoveState == SproutingMoveState.SPROUT && sproutingMoveStateTimer > SPROUT_TIME))
			return SproutingMoveState.WALK;
		else
			return SproutingMoveState.SPROUT;
	}

	private void processSprite(float delta) {
		Vector2 position = new Vector2();
		switch(sproutingMoveState) {
			case SPROUT:
				position.set(initSpawnPosition.cpy().add(0f,
						SPROUT_OFFSET * (SPROUT_TIME - sproutingMoveStateTimer) / SPROUT_TIME));
				break;
			case WALK:
			case END:
				position.set(body.getPosition());
				break;
		}
		sprite.update(delta, false, position);
	}

	private void doDraw(Eye adBatch){
		// draw sprite if powerup is not used
		if(powerupTaker == null)
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
