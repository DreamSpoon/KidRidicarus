package kidridicarus.game.agent.KidIcarus.NPC.monoeye;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentspine.NPC_Spine;
import kidridicarus.common.info.UInfo;

public class MonoeyeSpine extends NPC_Spine {
	private static final float ACCEL_X = UInfo.P2M(190);
	private static final float HIGHVEL_X = UInfo.P2M(120);

	private static final float CENTER_OFFSET_X = 3f;	// offset from spawn, in tiles
	private static final int ACCEL_OFFSET_LEFT = -4;
	private static final int ACCEL_OFFSET_RIGHT = 4;

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

	public void applyHorizontalMove(boolean isMoveRight) {
		float dirMult = isMoveRight ? 1f : -1f;
		int curTileOffsetX = (int) (UInfo.FloatM2Tx(body.getPosition().x) - (spawnTileX + CENTER_OFFSET_X));
		curTileOffsetX *= dirMult;
		// if in acceleration zone then apply acceleration
		if(curTileOffsetX <= ACCEL_OFFSET_LEFT)
			body.applyForce(new Vector2(ACCEL_X * dirMult, 0f));
		// else set high velocity
		else {
			float velocityX = HIGHVEL_X * dirMult;
			body.setVelocity(velocityX, body.getVelocity().y);
		}
	}
}
