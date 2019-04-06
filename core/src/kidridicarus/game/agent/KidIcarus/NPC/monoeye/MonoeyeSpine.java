package kidridicarus.game.agent.KidIcarus.NPC.monoeye;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.agentspawntrigger.AgentSpawnTrigger;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentspine.NPC_Spine;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;

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

	// This eye can do some pretty fly moves...
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
		if((isMoveRight && isFlyFarLeft()) || (!isMoveRight && isFlyFarRight()))
			body.applyForce(new Vector2(ACCEL_X * dirMult, 0f));
		// else set high velocity
		else
			body.setVelocity(HIGHVEL_X * dirMult, body.getVelocity().y);
	}

	// ... but get it mad, and you better hope the pirate look is in this season - you might lose an eye...
	public void applyOgleMoveUpdate(float otherPosX) {
		// check horizontal move direction against accel zones
		if(isMovingRight && isOgleFarRight(otherPosX))
			isMovingRight = false;
		else if(!isMovingRight && isOgleFarLeft(otherPosX))
			isMovingRight = true;
		// if out of acceleration zone, check for horizontal move direction change 
		if(!isInAccelZone()) {
			if(isMovingRight && isOgleFarRight(otherPosX)) {
				isMovingRight = false;
				accelEndTileLeft = UInfo.FloatM2Tx(otherPosX) + OGLE_ACCEL_OFFSET_RIGHT;
			}
			else if(!isMovingRight && isFlyFarLeft()) {
				isMovingRight = true;
				accelEndTileRight = UInfo.FloatM2Tx(otherPosX) + OGLE_ACCEL_OFFSET_LEFT;
			}
		}

		float dirMult = isMovingRight ? 1f : -1f;
		// if in acceleration zone then apply acceleration
		if((isMovingRight && isOgleFarLeft(otherPosX)) || (!isMovingRight && isOgleFarRight(otherPosX)))
			body.applyForce(new Vector2(OGLE_ACCEL_X * dirMult, 0f));
		// else set high velocity
		else
			body.setVelocity(OGLE_VEL_X * dirMult, -HIGHVEL_Y);
	}

	public AgentContactHoldSensor createPlayerSensor() {
		playerSensor = new AgentContactHoldSensor(null);
		return playerSensor;
	}

	/*
	 * Returns gawking PlayerAgent if a PlayerAgent is gawking this Monoeye while Monoeye is moving down.
	 * Otherwise return null.
	 */
	public PlayerAgent getGawker(boolean isFacingRight) {
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
