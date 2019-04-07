package kidridicarus.game.agent.KidIcarus.NPC.monoeye;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.agentspawntrigger.AgentSpawnTrigger;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentspine.NPC_Spine;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.agent.KidIcarus.NPC.monoeye.Monoeye.AxisGoState;

public class MonoeyeSpine extends NPC_Spine {
	private static final float ACCEL_X = UInfo.P2M(180);
	private static final float VEL_X = UInfo.P2M(120);
	private static final float ACCEL_Y = UInfo.P2M(450);
	private static final float VEL_Y = UInfo.P2M(60);

	private static final float CENTER_OFFSET_X = 3f;	// offset from spawn, in tiles
	private static final int ACCEL_OFFSET_RIGHT = 4;
	private static final int ACCEL_OFFSET_LEFT = -5;

	private static final int ACCEL_OFFSET_TOP = -1;
	private static final int ACCEL_OFFSET_BOTTOM = -5;

	private AgentContactHoldSensor playerSensor;
	private float initRightAccelZoneTile;
	private float rightAccelZoneTile;
	private float initLeftAccelZoneTile;
	private float leftAccelZoneTile;

	public MonoeyeSpine(MonoeyeBody body, int spawnTileX) {
		super(body);

		// initialize far right and left of normal velocity "window"
		initRightAccelZoneTile = spawnTileX + CENTER_OFFSET_X + ACCEL_OFFSET_RIGHT;
		rightAccelZoneTile = initRightAccelZoneTile;
		initLeftAccelZoneTile = spawnTileX + CENTER_OFFSET_X + ACCEL_OFFSET_LEFT;
		leftAccelZoneTile = initLeftAccelZoneTile;
		// the far top and bottom are relative to the scrolling player's screen, cannot init here
	}

	public AgentContactHoldSensor createPlayerSensor() {
		playerSensor = new AgentContactHoldSensor(null);
		return playerSensor;
	}

	public void applyAxisMoves(AxisGoState horizGoState, AxisGoState vertGoState) {
		Vector2 force = new Vector2(0f, 0f);
		Vector2 velocity = body.getVelocity().cpy();

		int dirMult = 1;
		if(!horizGoState.isPlus())
			dirMult = -1;
		if(horizGoState.isAccel())
			force.x = ACCEL_X * dirMult;
		else
			velocity.x = VEL_X * dirMult;

		dirMult = 1;
		if(!vertGoState.isPlus())
			dirMult = -1;
		if(vertGoState.isAccel())
			force.y = ACCEL_Y * dirMult;
		else
			velocity.y = VEL_Y * dirMult;

		body.setVelocity(velocity);
		body.applyForce(force);
	}

	private Integer getScrollTopY() {
		Integer scrollTopY = null;
		AgentSpawnTrigger trigger = agentSensor.getFirstContactByClass(AgentSpawnTrigger.class);
		if(trigger != null)
			scrollTopY = UInfo.M2Ty(trigger.getBounds().y + trigger.getBounds().height);
		return scrollTopY;
	}

	/*
	 * Returns gawking PlayerAgent if a PlayerAgent is gawking this Monoeye while Monoeye is moving down.
	 * Otherwise return null.
	 */
	public PlayerAgent getGawker(boolean isFacingRight) {
		PlayerAgent playerAgent = playerSensor.getFirstContactByClass(PlayerAgent.class);
		if(isOtherGawking(isFacingRight, playerAgent) &&
				UInfo.M2Tx(playerAgent.getPosition().x) >= initLeftAccelZoneTile &&
				UInfo.M2Tx(playerAgent.getPosition().x) <= initRightAccelZoneTile)
			return playerAgent;
		return null;
	}

	private boolean isOtherGawking(boolean isFacingRight, PlayerAgent otherAgent) {
		if(otherAgent == null)
			return false;

		// if the facing directions are opposite, and the positions are opposite, then gawking occurred
		Vector2 otherPos = otherAgent.getPosition(); 
		Direction4 otherDir = otherAgent.getProperty(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class);
		if(isFacingRight && otherDir == Direction4.LEFT && body.getPosition().x <= otherPos.x)
			return true;
		else if(!isFacingRight && otherDir == Direction4.RIGHT && body.getPosition().x >= otherPos.x)
			return true;

		return false;
	}

	public boolean isContinueAcceleration(boolean isHorizontal, boolean isPlus) {
		if(isHorizontal) {
			int myTileX = UInfo.M2Tx(body.getPosition().x);
			// if moving right and current position is within left acceleration zone then continue acceleration
			if(isPlus && isTileInOneAccelZone(true, myTileX, false))
				return true;
			// if moving left and current position is within right acceleration zone then continue acceleration
			else if(!isPlus && isTileInOneAccelZone(true, myTileX, true))
				return true;
		}
		// vertical
		else {
			int myTileY = UInfo.M2Tx(body.getPosition().y);
			// if moving up and current position is within bottom acceleration zone then continue acceleration
			if(isPlus && isTileInOneAccelZone(false, myTileY, false))
				return true;
			// if moving down and current position is within top acceleration zone then continue acceleration
			else if(!isPlus && isTileInOneAccelZone(false, myTileY, true))
				return true;
		}
		// discontinue acceleration
		return false;
	}

	public boolean isChangeDirection(boolean isHorizontal, boolean isPlus) {
		if(isHorizontal) {
			int myTileX = UInfo.M2Tx(body.getPosition().x);
			// if moving right and current position is within right acceleration zone then change direction
			if(isPlus && isTileInOneAccelZone(true, myTileX, true))
				return true;
			// if moving left and current position is within left acceleration zone then change direction
			else if(!isPlus && isTileInOneAccelZone(true, myTileX, false))
				return true;
		}
		// vertical
		else {
			int myTileY = UInfo.M2Tx(body.getPosition().y);
			// if moving up and current position is within top acceleration zone then change direction
			if(isPlus && isTileInOneAccelZone(false, myTileY, true))
				return true;
			// if moving down and current position is within bottom acceleration zone then change direction
			else if(!isPlus && isTileInOneAccelZone(false, myTileY, false))
				return true;
		}
		// don't change direction
		return false;
	}

	private boolean isTileInOneAccelZone(boolean isHorizontal, int tileOffset, boolean isPlus) {
		if(isHorizontal) {
			// if checking left acceleration zone and offset is within zone then return true
			if(!isPlus && tileOffset <= leftAccelZoneTile)
				return true;
			// if checking right acceleration zone and offset is within zone then return true
			else if(isPlus && tileOffset >= rightAccelZoneTile)
				return true;
		}
		// vertical
		else {
			Integer scrollTopY = getScrollTopY();
			if(scrollTopY == null)
				return false;

			// if checking bottom acceleration zone and offset is within zone then return true
			if(!isPlus && tileOffset <= getScrollTopY() + ACCEL_OFFSET_BOTTOM)
				return true;
			// if checking top acceleration zone and offset is within zone then return true
			else if(isPlus && tileOffset >= getScrollTopY() + ACCEL_OFFSET_TOP)
				return true;
		}
		// return false because tile is not in either one of the acceleration zones
		return false;
	}

	public boolean isTargetOnLeft(float posX) {
		return UInfo.M2Tx(posX) < UInfo.M2Tx(body.getPosition().x);
	}

	public boolean isTargetOnRight(float posX) {
		return UInfo.M2Tx(posX) > UInfo.M2Tx(body.getPosition().x);
	}

	public void setRightAccelZoneToCurrentPos() {
		rightAccelZoneTile = (float) UInfo.M2Tx(body.getPosition().x);
		if(rightAccelZoneTile > initRightAccelZoneTile)
			rightAccelZoneTile = initRightAccelZoneTile;
	}

	public void resetRightAccelZone() {
		rightAccelZoneTile = initRightAccelZoneTile;
	}

	public void setLeftAccelZoneToCurrentPos() {
		leftAccelZoneTile = (float) UInfo.M2Tx(body.getPosition().x);
		if(leftAccelZoneTile < initLeftAccelZoneTile)
			leftAccelZoneTile = initLeftAccelZoneTile;
	}

	public void resetLeftAccelZone() {
		leftAccelZoneTile = initLeftAccelZoneTile;
	}

/*	private boolean isTileInEitherAccelZone(boolean isHorizontal, int tileOffset) {
		if(isHorizontal) {
			if(tileOffset <= leftAccelZoneTile)
				return true;
			else if(tileOffset >= rightAccelZoneTile)
				return true;
		}
		// vertical
		else {
			Integer scrollTopY = getScrollTopY();
			if(scrollTopY == null)
				return false;
	
			if(tileOffset <= getScrollTopY() + ACCEL_OFFSET_BOTTOM)
				return true;
			else if(tileOffset >= getScrollTopY() + ACCEL_OFFSET_TOP)
				return true;
		}
		// return false because tile is not in either zone
		return false;
	}
*/
/*	// track ogle target, horizontal tracking only - only called when out of acceleration zone
	public void doTrackTarget(float targetX, boolean isPlus) {
		int myTileX = UInfo.M2Tx(body.getPosition().x);
		int targetTileX = UInfo.M2Tx(targetX);
		// if moving right and target is on left...
		if(isPlus && targetTileX < myTileX) {
QQ.pr("moving right, target is on left");
			// set right acceleration zone based on current position
			rightAccelZoneTile = (float) (myTileX + OGLE_ACCEL_OFFSET_RIGHT);
			// reset left acceleration zone to default
			leftAccelZoneTile = spawnTileX + CENTER_OFFSET_X + ACCEL_OFFSET_LEFT;
		}
		// if moving left and target is on right...
		else if(!isPlus && targetTileX > myTileX) {
QQ.pr("moving left, target is on right");
			// reset right acceleration zone to default
			rightAccelZoneTile = spawnTileX + CENTER_OFFSET_X + ACCEL_OFFSET_RIGHT;
			// set left acceleration zone based on current position
			leftAccelZoneTile = (float) (myTileX + OGLE_ACCEL_OFFSET_LEFT);
		}
	}
*/
}
/*
public class MonoeyeSpine extends NPC_Spine {
	private static final float ACCEL_X = UInfo.P2M(180);
	private static final float HIGHVEL_X = UInfo.P2M(120);
	private static final float ACCEL_Y = UInfo.P2M(450);
	private static final float HIGHVEL_Y = UInfo.P2M(60);

	private static final float OGLE_ACCEL_X = UInfo.P2M(240);
	private static final float OGLE_VEL_X = UInfo.P2M(120);

	private static final float CENTER_OFFSET_X = 3f;	// offset from spawn, in tiles
	private static final int ACCEL_OFFSET_RIGHT = 4;
	private static final int ACCEL_OFFSET_LEFT = -5;

	private static final float CENTER_OFFSET_Y = -3f;	// offset from spawn, in tiles
	private static final int ACCEL_OFFSET_TOP = 2;
	private static final int ACCEL_OFFSET_BOTTOM = -1;

	private static final int OGLE_ACCEL_OFFSET_RIGHT = 2;
	private static final int OGLE_ACCEL_OFFSET_LEFT = -2;

	private AgentContactHoldSensor playerSensor;
	private int spawnTileX;
	private boolean isMovingRight;
	private boolean isMovingUp;
	private Float accelEndTileRight;
	private Float accelEndTileLeft;

	public MonoeyeSpine(MonoeyeBody body, int spawnTileX) {
		super(body);

		this.spawnTileX = spawnTileX;
		// default to move right and down
		isMovingRight = true;
		isMovingUp = false;
		accelEndTileRight = null;
		accelEndTileLeft = null;
	}

	public boolean isFlyFarRight() {
		return UInfo.M2Tx(body.getPosition().x) - (spawnTileX + CENTER_OFFSET_X) >= ACCEL_OFFSET_RIGHT;
	}

	public boolean isFlyFarLeft() {
		return UInfo.M2Tx(body.getPosition().x) - (spawnTileX + CENTER_OFFSET_X) <= ACCEL_OFFSET_LEFT;
	}

	public boolean isFlyFarTop() {
		return UInfo.M2Ty(body.getPosition().y) - (getScrollTopY() + CENTER_OFFSET_Y) >= ACCEL_OFFSET_TOP;
	}

	public boolean isFlyFarBottom() {
		return UInfo.M2Ty(body.getPosition().y) - (getScrollTopY() + CENTER_OFFSET_Y) <= ACCEL_OFFSET_BOTTOM;
	}

	public boolean isOgleFarRight(float otherPosX) {
		return UInfo.M2Tx(body.getPosition().x) - UInfo.M2Tx(otherPosX) >= OGLE_ACCEL_OFFSET_RIGHT;
	}

	public boolean isOgleFarLeft(float otherPosX) {
		return UInfo.M2Tx(body.getPosition().x) - UInfo.M2Tx(otherPosX) <= OGLE_ACCEL_OFFSET_LEFT;
	}

	public boolean isInAccelZone() {
		if(accelEndTileRight == null && accelEndTileLeft == null)
			return false;
		else if(accelEndTileRight != null && UInfo.FloatM2Tx(body.getPosition().x) >= accelEndTileRight)
			return true;
		else if(accelEndTileLeft != null && UInfo.FloatM2Tx(body.getPosition().x) <= accelEndTileLeft)
			return true;
		return false;
	}

	private Float getScrollTopY() {
		Float scrollTopY = null;
		AgentSpawnTrigger trigger = agentSensor.getFirstContactByClass(AgentSpawnTrigger.class);
		if(trigger != null)
			scrollTopY = UInfo.FloatM2Ty(trigger.getBounds().y + trigger.getBounds().height);
		return scrollTopY;
	}

	public void applyFlyMoveUpdate() {
		if(isMovingUp && isFlyFarTop())
			isMovingUp = false;
		else if(!isMovingUp && isFlyFarBottom())
			isMovingUp = true;
		applyVerticalMove(isMovingUp);

		if(!isInAccelZone()) {
			accelEndTileRight = null;
			accelEndTileLeft = null;
			if(isMovingRight && isFlyFarRight()) {
				isMovingRight = false;
				accelEndTileRight = spawnTileX + CENTER_OFFSET_X + ACCEL_OFFSET_RIGHT;
			}
			else if(!isMovingRight && isFlyFarLeft()) {
				isMovingRight = true;
				accelEndTileLeft = spawnTileX + CENTER_OFFSET_X + ACCEL_OFFSET_LEFT;
			}
		}
		applyHorizontalMove(isMovingRight);
	}

	private void applyVerticalMove(boolean isMoveUp) {
		float dirMult = isMoveUp ? 1f : -1f;
		// if in acceleration zone then apply acceleration
		if((isMoveUp && isFlyFarBottom()) || (!isMoveUp && isFlyFarTop()))
			body.applyForce(new Vector2(0f, ACCEL_Y * dirMult));
		// else set high velocity
		else
			body.setVelocity(body.getVelocity().x, HIGHVEL_Y * dirMult);
	}

	private void applyHorizontalMove(boolean isMoveRight) {
		float dirMult = isMoveRight ? 1f : -1f;
		// if in acceleration zone then apply acceleration
		if(isInAccelZone())
			body.applyForce(new Vector2(ACCEL_X * dirMult, 0f));
		// else set high velocity
		else
			body.setVelocity(HIGHVEL_X * dirMult, body.getVelocity().y);
	}

	public void applyOgleMoveUpdate(float otherPosX) {
QQ.pr("ogleMove, myTilePos="+UInfo.FloatM2Tx(body.getPosition().x)+", otherPosX="+UInfo.FloatM2Tx(otherPosX)+", endTileRight="+accelEndTileRight+", endTileLeft="+accelEndTileLeft+", inAccelZone="+isInAccelZone());
		if(!isInAccelZone()) {
			accelEndTileRight = null;
			accelEndTileLeft = null;
			// check horizontal move direction against accel zones
			if(isMovingRight && isOgleFarRight(otherPosX)) {
				isMovingRight = false;
				accelEndTileRight = UInfo.FloatM2Tx(otherPosX) + OGLE_ACCEL_OFFSET_RIGHT;
			}
			else if(!isMovingRight && isOgleFarLeft(otherPosX)) {
				isMovingRight = true;
				accelEndTileLeft = UInfo.FloatM2Tx(otherPosX) + OGLE_ACCEL_OFFSET_LEFT;
			}
		}

		float dirMult = isMovingRight ? 1f : -1f;
		// if in acceleration zone then apply acceleration
		if(isInAccelZone()) {
QQ.pr("ogle accel " + dirMult);
			body.applyForce(new Vector2(OGLE_ACCEL_X * dirMult, 0f));
		}
		// else set high velocity
		else {
QQ.pr("ogle vel " + dirMult + "");
			body.setVelocity(OGLE_VEL_X * dirMult, -HIGHVEL_Y);
		}
	}

	public AgentContactHoldSensor createPlayerSensor() {
		playerSensor = new AgentContactHoldSensor(null);
		return playerSensor;
	}
*/
	/*
	 * Returns gawking PlayerAgent if a PlayerAgent is gawking this Monoeye while Monoeye is moving down.
	 * Otherwise return null.
	 */
/*	public PlayerAgent getGawker(boolean isFacingRight) {
		PlayerAgent playerAgent = playerSensor.getFirstContactByClass(PlayerAgent.class);
		if(!isMovingUp && isOtherGawking(isFacingRight, playerAgent) &&
				UInfo.M2Tx(playerAgent.getPosition().x) >= spawnTileX + CENTER_OFFSET_X + ACCEL_OFFSET_LEFT &&
				UInfo.M2Tx(playerAgent.getPosition().x) <= spawnTileX + CENTER_OFFSET_X + ACCEL_OFFSET_RIGHT)
			return playerAgent;
		return null;
	}

	private boolean isOtherGawking(boolean isFacingRight, PlayerAgent otherAgent) {
		if(otherAgent == null)
			return false;

		// if the facing directions are opposite, and the positions are opposite, then gawking occurred
		Vector2 otherPos = otherAgent.getPosition(); 
		Direction4 otherDir = otherAgent.getProperty(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class);
		if(isFacingRight && otherDir == Direction4.LEFT && body.getPosition().x <= otherPos.x)
			return true;
		else if(!isFacingRight && otherDir == Direction4.RIGHT && body.getPosition().x >= otherPos.x)
			return true;

		return false;
	}

	public boolean canAcquireTarget() {
		return !isInAccelZone();
	}

	public boolean isMovingUp() {
		return isMovingUp;
	}
}
*/