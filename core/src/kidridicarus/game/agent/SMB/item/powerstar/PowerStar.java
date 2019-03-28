package kidridicarus.game.agent.SMB.item.powerstar;

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
import kidridicarus.common.info.UInfo;
import kidridicarus.game.agent.SMB.BumpTakeAgent;
import kidridicarus.game.agent.SMB.other.floatingpoints.FloatingPoints;
import kidridicarus.game.powerup.SMB_Pow;

/*
 * TODO:
 * -allow the star to spawn down-right out of bricks like on level 1-1
 * -test the star's onBump method - I could not bump it, needs precise timing - maybe loosen the timing? 
 */
public class PowerStar extends Agent implements BumpTakeAgent, DisposableAgent {
	private static final float SPROUT_TIME = 0.5f;
	private static final Vector2 MAX_BOUNCE_VEL = new Vector2(0.5f, 2f); 
	private static final float SPROUT_OFFSET = UInfo.P2M(-13f);
	private enum MoveState { SPROUT, WALK, END }

	private PowerStarBody body;
	private PowerStarSprite sprite;
	private AgentDrawListener drawListener;

	private float moveStateTimer;
	private MoveState moveState;
	private Vector2 initSpawnPosition;
	private boolean isFacingRight;
	private boolean isPowerupUsed;
	private Agent powerupTaker;

	public PowerStar(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		initSpawnPosition = Agent.getStartPoint(properties);

		moveStateTimer = 0f;
		moveState = MoveState.SPROUT;
		isFacingRight = true;
		isPowerupUsed = false;
		powerupTaker = null;

		body = null;
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.CONTACT_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		// sprout from bottom layer and switch to next layer on finish sprout
		sprite = new PowerStarSprite(agency.getAtlas(), initSpawnPosition.cpy().add(0f, SPROUT_OFFSET));
		drawListener = new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			};
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_BOTTOM, drawListener);
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
		// if taker takes the powerup then this powerup is done
		if(taker.onTakePowerup(new SMB_Pow.PowerStarPow())) {
			isPowerupUsed = true;
			powerupTaker = (Agent) taker;
		}
	}

	private void doUpdate(float delta) {
		processMove(delta);
		processSprite(delta);
	}

	private void processMove(float delta) {
		MoveState nextMoveState = getNextMoveState();
		switch(nextMoveState) {
			case WALK:
				// spawn the body when sprout finishes
				if(moveState == MoveState.SPROUT) {
					// change from bottom to middle sprite draw order
					agency.removeAgentDrawListener(this, drawListener);
					drawListener = new AgentDrawListener() {
							@Override
							public void draw(AgencyDrawBatch batch) { doDraw(batch); }
						};
					agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_MIDDLE, drawListener);
					body = new PowerStarBody(this, agency.getWorld(), initSpawnPosition, new Vector2(0f, 0f));

					// start bounce to the right since this is first walk frame
					body.applyImpulse(MAX_BOUNCE_VEL);
					break;
				}

				// if horizontal move is blocked by solid and not agent then reverse direction
				if(body.getSpine().isHorizontalMoveBlocked(isFacingRight, false))
					isFacingRight = !isFacingRight;

				float xVal = isFacingRight ? MAX_BOUNCE_VEL.x : -MAX_BOUNCE_VEL.x;
				// clamp +y velocity and maintain contstant x velocity
				if(body.getVelocity().y > MAX_BOUNCE_VEL.y)
					body.setVelocity(xVal, MAX_BOUNCE_VEL.y);
				// clamp -y velocity and maintain constant x velocity
				else if(body.getVelocity().y < -MAX_BOUNCE_VEL.y)
					body.setVelocity(xVal, -MAX_BOUNCE_VEL.y);
				// maintain constant x velocity
				else
					body.setVelocity(xVal, body.getVelocity().y);
				break;
			case SPROUT:
				break;
			case END:
				agency.createAgent(FloatingPoints.makeAP(1000, true, body.getPosition(), powerupTaker));
				// powerup used, so dispose this agent
				agency.removeAgent(this);
				break;
		}

		// increment state timer
		moveStateTimer = nextMoveState == moveState ? moveStateTimer+delta : 0f;
		moveState = nextMoveState;
	}

	private MoveState getNextMoveState() {
		if(isPowerupUsed)
			return MoveState.END;
		else if(moveState == MoveState.SPROUT && moveStateTimer > SPROUT_TIME)
			return MoveState.WALK;
		else if(moveState == MoveState.SPROUT)
			return MoveState.SPROUT;
		else
			return MoveState.WALK;
	}

	private void processSprite(float delta) {
		// if sprouting then use sprout offset for sprite position
		if(moveState == MoveState.SPROUT) {
			float yOffset = SPROUT_OFFSET * (SPROUT_TIME - moveStateTimer) / SPROUT_TIME;
			sprite.update(delta, initSpawnPosition.cpy().add(0f, yOffset));
		}
		// otherwise use the regular body position
		else
			sprite.update(delta, body.getPosition());
	}

	private void doDraw(AgencyDrawBatch batch){
		// do not draw sprite if powerup is used 
		if(isPowerupUsed)
			return;

		batch.draw(sprite);
	}

	@Override
	public void onTakeBump(Agent bumpingAgent) {
		// if bump came from left and star is moving left then reverse,
		// if bump came from right and star is moving right then reverse
		if((bumpingAgent.getPosition().x < body.getPosition().x && body.getVelocity().x < 0f) ||
			(bumpingAgent.getPosition().x > body.getPosition().x && body.getVelocity().x > 0f))
			isFacingRight = !isFacingRight;
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
