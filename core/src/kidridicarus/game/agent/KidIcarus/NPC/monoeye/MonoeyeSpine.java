package kidridicarus.game.agent.KidIcarus.NPC.monoeye;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.agentspawntrigger.AgentSpawnTrigger;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentspine.SMB_NPC_Spine;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.agent.KidIcarus.NPC.monoeye.Monoeye.AxisGoState;

public class MonoeyeSpine extends SMB_NPC_Spine {
	private static final float ACCEL_X = UInfo.P2M(180);
	private static final float VEL_X = UInfo.P2M(120);
	private static final float ACCEL_Y = UInfo.P2M(450);
	private static final float VEL_Y = UInfo.P2M(60);

	private static final float CENTER_OFFSET_X = 3f;	// offset from spawn, in tiles
	private static final int ACCEL_OFFSET_RIGHT = 4;
	private static final int ACCEL_OFFSET_LEFT = -5;

	private static final int ACCEL_OFFSET_TOP = -1;
	private static final int ACCEL_OFFSET_BOTTOM = -6;

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
}