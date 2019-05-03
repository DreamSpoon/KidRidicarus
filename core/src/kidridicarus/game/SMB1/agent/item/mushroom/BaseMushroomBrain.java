package kidridicarus.game.SMB1.agent.item.mushroom;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.FrameTime;
import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.PowerupBrainContactFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.SMB1.agent.item.fireflower.SproutSpriteFrameInput;
import kidridicarus.game.SMB1.agent.other.floatingpoints.FloatingPoints;
import kidridicarus.game.info.SMB1_Audio;

public class BaseMushroomBrain {
	private static final float SPROUT_TIME = 1f;
	private static final float SPROUT_OFFSET = UInfo.P2M(-13f);
	private static final float WALK_VEL = 0.6f;
	private static final float BUMP_UPVEL = 1.5f;

	private enum MoveState { SPROUT, WALK, END }

	private BaseMushroom parent;
	private BaseMushroomBody body;
	private float moveStateTimer;
	private MoveState moveState;
	private Vector2 initSpawnPosition;
	private PowerupTakeAgent powerupTaker;
	private boolean isFacingRight;
	private boolean isBumped;
	private Vector2 bumpCenter;
	private RoomBox lastKnownRoom;
	private boolean despawnMe;
	private Powerup myPowerup;

	public BaseMushroomBrain(BaseMushroom parent, BaseMushroomBody body, Vector2 initSpawnPosition,
			Powerup myPowerup) {
		this.parent = parent;
		this.body = body;
		this.myPowerup = myPowerup;
		this.initSpawnPosition = initSpawnPosition;
		moveStateTimer = 0f;
		moveState = MoveState.SPROUT;
		powerupTaker = null;
		isFacingRight = true;
		isBumped = false;
		bumpCenter = new Vector2();
		lastKnownRoom = null;
		despawnMe = false;
	}

	public Vector2 getSproutStartPos() {
		return initSpawnPosition.cpy().add(0f, SPROUT_OFFSET);
	}

	public void processContactFrame(BrainContactFrameInput cFrameInput) {
		// exit if not finished sprouting or if used
		if(moveState == MoveState.SPROUT || powerupTaker != null)
			return;
		// if any agents touching this powerup are able to take it, then push it to them
		PowerupTakeAgent taker = ((PowerupBrainContactFrameInput) cFrameInput).powerupTaker;
		if(taker == null)
			return;
		if(taker.onTakePowerup(myPowerup))
			powerupTaker = taker;

		// if alive and not touching keep alive box, or if touching despawn, then set despawn flag
		if(moveState != MoveState.END && (!cFrameInput.isKeepAlive || cFrameInput.isDespawn))
			despawnMe = true;
		// update last known room if not used
		if(moveState != MoveState.END && cFrameInput.room != null)
			lastKnownRoom = cFrameInput.room;
	}

	public SproutSpriteFrameInput processFrame(FrameTime frameTime) {
		SproutSpriteFrameInput frameOut = new SproutSpriteFrameInput();
		MoveState nextMoveState = getNextMoveState();
		boolean isMoveStateChange = nextMoveState != moveState;
		switch(nextMoveState) {
			case SPROUT:
				frameOut.position.set(initSpawnPosition.cpy().add(0f,
						SPROUT_OFFSET * (SPROUT_TIME - moveStateTimer) / SPROUT_TIME));
				break;
			case WALK:
				if(isMoveStateChange) {
					frameOut.finishSprout = true;
					body.finishSprout(initSpawnPosition);
				}
				else {
					processBumps();
					// if on ground then apply walk velocity
					if(body.getSpine().isOnGround()) {
						if(isFacingRight)
							body.setVelocity(WALK_VEL, body.getVelocity().y);
						else
							body.setVelocity(-WALK_VEL, body.getVelocity().y);
					}
					// do space wrap last so that contacts are maintained
					body.getSpine().checkDoSpaceWrap(lastKnownRoom);
				}
				frameOut.position.set(body.getPosition());
				break;
			case END:
				if(powerupTaker != null) {
					parent.getAgency().getEar().playSound(SMB1_Audio.Sound.POWERUP_USE);
					parent.getAgency().createAgent(FloatingPoints.makeAP(1000, true, body.getPosition(),
							(Agent) powerupTaker));
				}
				parent.getAgency().removeAgent(parent);
				return null;
		}
		moveStateTimer = isMoveStateChange ? 0f : moveStateTimer+frameTime.timeDelta;
		moveState = nextMoveState;
		return frameOut;
	}

	private MoveState getNextMoveState() {
		if(powerupTaker != null || despawnMe)
			return MoveState.END;
		else if(moveState == MoveState.WALK || (moveState == MoveState.SPROUT && moveStateTimer > SPROUT_TIME))
			return MoveState.WALK;
		else
			return MoveState.SPROUT;
	}

	private void processBumps() {
		// process bumpings
		if(isBumped) {
			isBumped = false;
			// If moving right and bumped from the right then reverse velocity,
			// if moving left and bumped from the left then reverse velocity
			if(isFacingRight && bumpCenter.x > body.getPosition().x)
				isFacingRight = false;
			else if(!isFacingRight && bumpCenter.x < body.getPosition().x)
				isFacingRight = true;
			body.applyImpulse(new Vector2(0f, BUMP_UPVEL));
		}
		// bounce off of vertical bounds and not agents
		else if(body.getSpine().isSideMoveBlocked(isFacingRight))
			isFacingRight = !isFacingRight;
	}

	public void onTakeBump(Agent bumpingAgent) {
		// one bump per frame please
		if(isBumped)
			return;
		// if bumping agent doesn't have position then exit
		Vector2 bumpingAgentPos = AP_Tool.getCenter(bumpingAgent);
		if(bumpingAgentPos == null)
			return;

		isBumped = true;
		bumpCenter.set(bumpingAgentPos); 
	}
}
