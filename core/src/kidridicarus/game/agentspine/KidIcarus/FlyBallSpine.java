package kidridicarus.game.agentspine.KidIcarus;

import java.util.Arrays;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agent.agentspawntrigger.AgentSpawnTrigger;
import kidridicarus.common.agentspine.BasicAgentSpine;
import kidridicarus.common.info.UInfo;

public class FlyBallSpine extends BasicAgentSpine {
	private static final float ACCEL_X = UInfo.P2M(180);
	private static final float VEL_X = UInfo.P2M(120);
	private static final float ACCEL_Y = UInfo.P2M(450);
	private static final float VEL_Y = UInfo.P2M(60);

	private static final float CENTER_OFFSET_X = 3f;	// offset from spawn, in tiles
	private static final int ACCEL_OFFSET_RIGHT = 4;
	private static final int ACCEL_OFFSET_LEFT = -5;

	private static final int ACCEL_OFFSET_TOP = -1;
	private static final int ACCEL_OFFSET_BOTTOM = -6;

	public enum AxisGoState {
		ACCEL_PLUS, VEL_PLUS, ACCEL_MINUS, VEL_MINUS;
		public boolean equalsAny(AxisGoState ...otherStates) { return Arrays.asList(otherStates).contains(this); }
		public boolean isPlus() { return equalsAny(ACCEL_PLUS, VEL_PLUS); }
		public boolean isAccel() { return equalsAny(ACCEL_PLUS, ACCEL_MINUS); }
	}

	private float initRightAccelZoneTile;
	private float rightAccelZoneTile;
	private float initLeftAccelZoneTile;
	private float leftAccelZoneTile;

	public FlyBallSpine(AgentBody body, int spawnTileX) {
		super(body);
		// initialize far right and left of normal velocity "window"
		initRightAccelZoneTile = spawnTileX + CENTER_OFFSET_X + ACCEL_OFFSET_RIGHT;
		rightAccelZoneTile = initRightAccelZoneTile;
		initLeftAccelZoneTile = spawnTileX + CENTER_OFFSET_X + ACCEL_OFFSET_LEFT;
		leftAccelZoneTile = initLeftAccelZoneTile;
		// the far top and bottom are relative to the scrolling player's screen, cannot init here
	}

	public void applyAxisMoves(AxisGoState horizGoState, AxisGoState vertGoState) {
		// get x velocity and force
		int dirMult = 1;
		if(!horizGoState.isPlus())
			dirMult = -1;
		Vector2 force = new Vector2(0f, 0f);
		Vector2 velocity = body.getVelocity().cpy();
		if(horizGoState.isAccel())
			force.x = ACCEL_X * dirMult;
		else
			velocity.x = VEL_X * dirMult;
		// get y velocity and force
		dirMult = 1;
		if(!vertGoState.isPlus())
			dirMult = -1;
		if(vertGoState.isAccel())
			force.y = ACCEL_Y * dirMult;
		else
			velocity.y = VEL_Y * dirMult;
		// apply velocity and force to x and y concurrently
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

	public boolean isContinueAcceleration(boolean isHorizontal, boolean isPlus) {
		if(isHorizontal) {
			int myTileX = UInfo.M2Tx(body.getPosition().x);
			// If moving right and current position is within left acceleration zone then continue acceleration, or
			// if moving left and current position is within right acceleration zone then continue acceleration.
			if((isPlus && isTileInHorizontalAccelZone(myTileX, false)) ||
					(!isPlus && isTileInHorizontalAccelZone(myTileX, true)))
				return true;
		}
		// vertical
		else {
			int myTileY = UInfo.M2Tx(body.getPosition().y);
			// If moving up and current position is within bottom acceleration zone then continue acceleration, or
			// if moving down and current position is within top acceleration zone then continue acceleration.
			if((isPlus && isTileInVerticalAccelZone(myTileY, false)) ||
					(!isPlus && isTileInVerticalAccelZone(myTileY, true)))
				return true;
		}
		// discontinue acceleration
		return false;
	}

	public boolean isChangeDirection(boolean isHorizontal, boolean isPlus) {
		if(isHorizontal) {
			int myTileX = UInfo.M2Tx(body.getPosition().x);
			// If moving right and current position is within right acceleration zone then change direction, or
			// if moving left and current position is within left acceleration zone then change direction.
			if((isPlus && isTileInHorizontalAccelZone(myTileX, true)) ||
					(!isPlus && isTileInHorizontalAccelZone(myTileX, false)))
				return true;
		}
		// vertical
		else {
			int myTileY = UInfo.M2Tx(body.getPosition().y);
			// If moving up and current position is within top acceleration zone then change direction, or
			// if moving down and current position is within bottom acceleration zone then change direction.
			if((isPlus && isTileInVerticalAccelZone(myTileY, true)) ||
					(!isPlus && isTileInVerticalAccelZone(myTileY, false)))
				return true;
		}
		// don't change direction
		return false;
	}

	public boolean isTileInHorizontalAccelZone(int tileX, boolean isPlus) {
		// If checking left acceleration zone and X is within zone then return true, or
		// if checking right acceleration zone and X is within zone then return true.
		if((!isPlus && tileX <= leftAccelZoneTile) ||
				(isPlus && tileX >= rightAccelZoneTile))
			return true;
		// return false because tile is not in left accel zone and not in right accel zone
		return false;
	}

	public boolean isTileInVerticalAccelZone(int tileY, boolean isPlus) {
		Integer scrollTopY = getScrollTopY();
		if(scrollTopY == null)
			return false;
		// If checking bottom acceleration zone and Y is within zone then return true, or
		// if checking top acceleration zone and Y is within zone then return true.
		if((!isPlus && tileY <= getScrollTopY() + ACCEL_OFFSET_BOTTOM) ||
				(isPlus && tileY >= getScrollTopY() + ACCEL_OFFSET_TOP))
			return true;
		// return false because tile is not in bottom accel zone and not in top accel zone
		return false;
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
}