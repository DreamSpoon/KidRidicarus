package kidridicarus.game.KidIcarus.agent.NPC.monoeye;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentspine.PlayerContactNerve;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.KidIcarus.agentspine.FlyBallSpine;

public class MonoeyeSpine extends FlyBallSpine {
	private static final float CENTER_OFFSET_X = 3f;	// offset from spawn, in tiles
	private static final int ACCEL_X_LEFT = -5;
	private static final int ACCEL_X_RIGHT = 4;
	private static final int ACCEL_Y_BOTTOM = -6;
	private static final int ACCEL_Y_TOP = -1;

	private PlayerContactNerve pcNerve;

	public MonoeyeSpine(MonoeyeBody body, int spawnTileX) {
		super(body, getFlyWindow(spawnTileX));
		pcNerve = new PlayerContactNerve();
	}

	private static Rectangle getFlyWindow(int spawnTileX) {
		// initialize far left and right of normal velocity "window"
		int left = (int) (spawnTileX + CENTER_OFFSET_X + ACCEL_X_LEFT);
		int right = (int) (spawnTileX + CENTER_OFFSET_X + ACCEL_X_RIGHT);
		// the far top and bottom are relative to the scrolling player's screen, cannot init here
		return new Rectangle(left, ACCEL_Y_BOTTOM, right-left, ACCEL_Y_TOP-ACCEL_Y_BOTTOM);
	}

	public AgentContactHoldSensor createPlayerSensor() {
		return pcNerve.createPlayerSensor(); 
	}

	/*
	 * Returns gawking PlayerAgent if a PlayerAgent is gawking this Monoeye while Monoeye is moving down.
	 * Otherwise return null.
	 */
	public PlayerAgent getGawker(boolean isFacingRight) {
		PlayerAgent playerAgent = pcNerve.getFirstPlayerContact();
		if(playerAgent != null && isOtherGawking(isFacingRight, playerAgent))
			return playerAgent;
		return null;
	}

	private boolean isOtherGawking(boolean isFacingRight, PlayerAgent otherAgent) {
		// if other Agent doesn't have a position then gawking is impossible so return false
		Vector2 otherPos = AP_Tool.getCenter(otherAgent);
		if(otherPos == null)
			return false;

		// if other Agent is outside of gawking zone then return false
		int otherTileX = UInfo.M2Tx(otherPos.x);
		if(isInsideFlyWindowX(otherTileX, true) ||isInsideFlyWindowX(otherTileX, false))
			return false;

		// if the facing directions are opposite, and the positions are opposite, then gawking occurred
		Direction4 otherDir = otherAgent.getProperty(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class);
		return (isFacingRight && otherDir == Direction4.LEFT && body.getPosition().x <= otherPos.x) ||
				(!isFacingRight && otherDir == Direction4.RIGHT && body.getPosition().x >= otherPos.x);
	}
}