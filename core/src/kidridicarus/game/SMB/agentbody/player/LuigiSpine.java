package kidridicarus.game.SMB.agentbody.player;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.general.Room;
import kidridicarus.common.agentbody.sensor.AgentContactHoldSensor;
import kidridicarus.common.agentbody.sensor.OnGroundSensor;
import kidridicarus.common.info.UInfo;

/*
 * A "control center" for the body, to apply move impulses, etc. in an organized manner.
 * This class has multiple functions:
 *   1) Interpret the World through contacts.
 *   2) Relay information to the body from the World, with filtering/organization.
 *   3) Take blocks of information and translate into body impulses, and apply those body impulses
 *     (e.g. move up, move left, apply jump).
 */
public class LuigiSpine {
	private static final float WALKMOVE_XIMP = 0.025f;
	private static final float MAX_STAND_VEL = LuigiSpine.WALKMOVE_XIMP * 0.01f;
	private static final float MIN_WALKVEL = WALKMOVE_XIMP * 2f;
	private static final float MAX_WALKVEL = WALKMOVE_XIMP * 42f;
	private static final float DECEL_XIMP = WALKMOVE_XIMP * 1.37f;
	private static final float BRAKE_XIMP = WALKMOVE_XIMP * 2.75f;
	private static final float RUNMOVE_XIMP = WALKMOVE_XIMP * 1.5f;
	private static final float MAX_RUNVEL = MAX_WALKVEL * 1.65f;

	private static final float JUMP_IMPULSE = 1.75f;
	private static final float JUMP_FORCE = 25.2f;
	private static final float JUMPFORCE_MAXTIME = 0.5f;

	private static final float MAX_FALL_VELOCITY = UInfo.P2M(5f * 60f);
	private static final float AIRMOVE_XIMP = 0.04f;

	private AgentContactHoldSensor acSensor;
	private OnGroundSensor ogSensor;
	private LuigiBody body;

	public LuigiSpine(LuigiBody body) {
		this.body = body;
		acSensor = null;
		ogSensor = null;
	}

	public AgentContactHoldSensor createAgentSensor() {
		acSensor = new AgentContactHoldSensor(body);
		return acSensor;
	}

	public OnGroundSensor createGroundSensor() {
		ogSensor = new OnGroundSensor(null);
		return ogSensor;
	}

	/*
	 * Apply walk impulse and cap horizontal velocity.
	 */
	public void applyWalkMove(boolean moveRight, boolean run) {
		// run impulse or walk impulse?
		float impulse = run ? RUNMOVE_XIMP : WALKMOVE_XIMP;
		applyHorizontalImpulse(moveRight, impulse);
		// max run velocity or max walk velocity?
		float max = run ? MAX_RUNVEL : MAX_WALKVEL;
		capHorizontalVelocity(max);
	}

	/*
	 * Apply decel impulse and check for min velocity.
	 */
	public void applyDecelMove(boolean facingRight, boolean ducking) {
		// if moving right...
		if(body.getVelocity().x > MIN_WALKVEL) {
			// ... and facing right or ducking, then decelerate with soft impulse to the left
			if(facingRight && !ducking)
				applyHorizontalImpulse(true, -DECEL_XIMP);
			// ... and facing left, then decelerate with hard impulse to the left
			else
				applyHorizontalImpulse(true, -BRAKE_XIMP);
		}
		// if moving left...
		else if(body.getVelocity().x < -MIN_WALKVEL) {
			// ... and facing left or ducking, then decelerate with soft impulse to the right
			if(!facingRight && !ducking)
				applyHorizontalImpulse(false, -DECEL_XIMP);
			// ... and facing right, then decelerate with hard impulse to the right
			else
				applyHorizontalImpulse(false, -BRAKE_XIMP);
		}
		// not moving right or left fast enough, set horizontal velocity to zero to avoid wobbling
		else
			body.setVelocity(0f, body.getVelocity().y);
	}

	private void applyHorizontalImpulse(boolean moveRight, float amt) {
		if(moveRight)
			body.applyBodyImpulse(new Vector2(amt, 0f));
		else
			body.applyBodyImpulse(new Vector2(-amt, 0f));
	}

	/*
	 * Ensure horizontal velocity is within -max to +max.
	 */
	private void capHorizontalVelocity(float max) {
		if(body.getVelocity().x > max)
			body.setVelocity(max, body.getVelocity().y);
		else if(body.getVelocity().x < -max)
			body.setVelocity(-max, body.getVelocity().y);
	}

	private static final float MARIO_RUNJUMP_MULT = 0.25f;
	private static final float MARIO_MAX_RUNJUMPVEL = MarioBody.MARIO_MAX_RUNVEL;
	public void applyJumpImpulse() {
		// the faster mario is moving, the higher he jumps, up to a max
		float mult = Math.abs(body.getVelocity().x) / MARIO_MAX_RUNJUMPVEL;
		// cap the multiplier
		if(mult > 1f)
			mult = 1f;
		// 
		mult = 1f + mult * MARIO_RUNJUMP_MULT;

		body.applyBodyImpulse(new Vector2 (0f, JUMP_IMPULSE * mult));
	}

	public void applyJumpForce(float jumpForceTimer) {
		float t = jumpForceTimer;
		if(t < 0)
			t = 0;
		else if(t > JUMPFORCE_MAXTIME)
			t = JUMPFORCE_MAXTIME;
		body.applyForce(new Vector2(0, JUMP_FORCE * (JUMPFORCE_MAXTIME - t) / JUMPFORCE_MAXTIME));
	}

	public void applyAirMove(boolean moveRight) {
		applyHorizontalImpulse(moveRight, AIRMOVE_XIMP);
		capHorizontalVelocity(MAX_RUNVEL);
	}

	public boolean isStandingStill() {
		return (body.getVelocity().x >= -MAX_STAND_VEL && body.getVelocity().x <= MAX_STAND_VEL);
	}

	public boolean isBraking(boolean facingRight) {
		if(facingRight && body.getVelocity().x < -MIN_WALKVEL)
			return true;
		else if(!facingRight && body.getVelocity().x > MIN_WALKVEL)
			return true;
		return false;
	}

	public boolean isMovingUp() {
		return body.getVelocity().y > 0f;
	}

	public Room getCurrentRoom() {
		return (Room) acSensor.getFirstContactByClass(Room.class);
	}

	public boolean isOnGround() {
		return ogSensor.isOnGround();
	}

	public void capFallVelocity() {
		if(body.getVelocity().y < -MAX_FALL_VELOCITY)
			body.setVelocity(body.getVelocity().x, -MAX_FALL_VELOCITY);
	}
}
