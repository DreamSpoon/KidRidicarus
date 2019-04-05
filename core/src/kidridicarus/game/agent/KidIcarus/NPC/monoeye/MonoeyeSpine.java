package kidridicarus.game.agent.KidIcarus.NPC.monoeye;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.agentspawntrigger.AgentSpawnTrigger;
import kidridicarus.common.agentspine.NPC_Spine;
import kidridicarus.common.info.UInfo;

public class MonoeyeSpine extends NPC_Spine {
	private static final float ACCEL_X = UInfo.P2M(180);
	private static final float HIGHVEL_X = UInfo.P2M(120);
	private static final float ACCEL_Y = UInfo.P2M(450);
	private static final float HIGHVEL_Y = UInfo.P2M(60);

	private static final float CENTER_OFFSET_X = 3f;	// offset from spawn, in tiles
	private static final int ACCEL_OFFSET_LEFT = -4;
	private static final int ACCEL_OFFSET_RIGHT = 4;

	private static final float CENTER_OFFSET_Y = -3f;	// offset from spawn, in tiles
	private static final int ACCEL_OFFSET_BOTTOM = -1;
	private static final int ACCEL_OFFSET_TOP = 2;

	private int spawnTileX;

	public MonoeyeSpine(MonoeyeBody body, int spawnTileX) {
		super(body);
		this.spawnTileX = spawnTileX;
	}

	public boolean isFarRight() {
		int curTileOffsetX = (int) (UInfo.FloatM2Tx(body.getPosition().x) - (spawnTileX + CENTER_OFFSET_X));
		return curTileOffsetX >= ACCEL_OFFSET_RIGHT;
	}

	public boolean isFarLeft() {
		int curTileOffsetX = (int) (UInfo.FloatM2Tx(body.getPosition().x) - (spawnTileX + CENTER_OFFSET_X));
		return curTileOffsetX <= ACCEL_OFFSET_LEFT;
	}

	public boolean isFarTop() {
		int curTileOffsetY = (int) (UInfo.FloatM2Ty(body.getPosition().y) - (getScrollTopY() + CENTER_OFFSET_Y));
		return curTileOffsetY >= ACCEL_OFFSET_TOP;
	}

	public boolean isFarBottom() {
		int curTileOffsetY = (int) (UInfo.FloatM2Ty(body.getPosition().y) - (getScrollTopY() + CENTER_OFFSET_Y));
		return curTileOffsetY <= ACCEL_OFFSET_BOTTOM;
	}

	public void applyMoveBodyUpdate(boolean isMoveRight, boolean isMoveUp) {
		applyVerticalMove(isMoveUp);
		applyHorizontalMove(isMoveRight);
	}
	
	private Float getScrollTopY() {
		Float scrollTopY = null;
		AgentSpawnTrigger trigger = agentSensor.getFirstContactByClass(AgentSpawnTrigger.class);
		if(trigger != null)
			scrollTopY = UInfo.FloatM2Ty(trigger.getBounds().y + trigger.getBounds().height);
		return scrollTopY;
	}

	private void applyVerticalMove(boolean isMoveUp) {
		float dirMult = isMoveUp ? 1f : -1f;
		// if in acceleration zone then apply acceleration
		if((isMoveUp && isFarBottom()) || (!isMoveUp && isFarTop()))
			body.applyForce(new Vector2(0f, ACCEL_Y * dirMult));
		// else set high velocity
		else
			body.setVelocity(body.getVelocity().x, HIGHVEL_Y * dirMult);
	}

	private void applyHorizontalMove(boolean isMoveRight) {
		float dirMult = isMoveRight ? 1f : -1f;
		// if in acceleration zone then apply acceleration
		if((isMoveRight && isFarLeft()) || (!isMoveRight && isFarRight()))
			body.applyForce(new Vector2(ACCEL_X * dirMult, 0f));
		// else set high velocity
		else
			body.setVelocity(HIGHVEL_X * dirMult, body.getVelocity().y);
	}
}
