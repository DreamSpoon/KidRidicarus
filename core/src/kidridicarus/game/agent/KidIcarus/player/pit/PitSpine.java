package kidridicarus.game.agent.KidIcarus.player.pit;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.playeragent.PlayerSpine;
import kidridicarus.common.agentsensor.SolidContactSensor;
import kidridicarus.common.info.UInfo;

public class PitSpine extends PlayerSpine {
	private static final float MIN_WALK_VEL = 0.1f;

	private static final float GROUNDMOVE_XIMP = 0.2f;
	private static final float MAX_GROUNDMOVE_VEL = 0.65f;
	private static final float STOPMOVE_XIMP = 0.08f;
	private static final float AIRMOVE_XIMP = GROUNDMOVE_XIMP * 0.7f;
	private static final float MAX_AIRMOVE_VEL = MAX_GROUNDMOVE_VEL;
	private static final float JUMPUP_FORCE = 6.15f;
	private static final float JUMPUP_CONSTVEL = 1.6f;
	private static final float HEADBOUNCE_VEL = 1.4f;	// up velocity

	private SolidContactSensor sbSensor;

	public PitSpine(PitBody body) {
		super(body);
		sbSensor = null;
	}

	public SolidContactSensor createSolidBodySensor() {
		sbSensor = new SolidContactSensor(body);
		return sbSensor;
	}

	// apply walk impulse and cap horizontal velocity.
	public void applyWalkMove(boolean moveRight) {
		applyHorizImpulseAndCapVel(moveRight, GROUNDMOVE_XIMP, MAX_GROUNDMOVE_VEL);
	}

	// apply air impulse and cap horizontal velocity.
	public void applyAirMove(boolean moveRight) {
		applyHorizImpulseAndCapVel(moveRight, AIRMOVE_XIMP, MAX_AIRMOVE_VEL);
	}

	public void applyStopMove() {
		// if moving right...
		if(body.getVelocity().x > MIN_WALK_VEL)
			applyHorizontalImpulse(true, -STOPMOVE_XIMP);
		// if moving left...
		else if(body.getVelocity().x < -MIN_WALK_VEL)
			applyHorizontalImpulse(false, -STOPMOVE_XIMP);
		// not moving right or left fast enough, set horizontal velocity to zero to avoid wobbling
		else
			body.setVelocity(0f, body.getVelocity().y);
	}

	public void applyJumpForce(float forceTimer, float jumpForceDuration) {
		if(forceTimer < jumpForceDuration)
			body.applyForce(new Vector2(0f, JUMPUP_FORCE * forceTimer / jumpForceDuration));
	}

	public void applyHeadBounce() {
		applyPlayerHeadBounce(HEADBOUNCE_VEL);
	}

	public void applyJumpVelocity() {
		body.setVelocity(body.getVelocity().x, JUMPUP_CONSTVEL);
	}

	public boolean isStandingStill() {
		return isStandingStill(MIN_WALK_VEL);
	}

	public void checkDoBodySizeChange(boolean isNextDucking) {
		boolean isCurrentlyDucking = ((PitBody) body).isDuckingForm();
		boolean isSolidTileAbove = isMapTileSolid(UInfo.getM2PTileForPos(body.getPosition()).add(0, 1));
		if(isCurrentlyDucking && !isNextDucking && !isSolidTileAbove)
			((PitBody) body).setDuckingForm(false);
		else if(!isCurrentlyDucking && isNextDucking)
			((PitBody) body).setDuckingForm(true);
	}

	public boolean isHeadInTile() {
		return isMapTileSolid(UInfo.getM2PTileForPos(body.getPosition()).add(0, 1));
	}

	public boolean isWalkingRight() {
		return body.getVelocity().x > MIN_WALK_VEL;
	}

	public boolean isWalkingLeft() {
		return body.getVelocity().x < -MIN_WALK_VEL;
	}
}
