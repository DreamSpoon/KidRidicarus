package kidridicarus.game.agent.SMB1.player.mario;

import java.util.List;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.playeragent.PlayerSpine;
import kidridicarus.common.agentsensor.OneWayContactSensor;
import kidridicarus.common.info.UInfo;

/*
 * A "control center" for the body, to apply move impulses, etc. in an organized manner.
 * This class has multiple functions:
 *   1) Interpret the World through contacts.
 *   2) Relay information to the body from the World, with filtering/organization.
 *   3) Take blocks of information and translate into body impulses, and apply those body impulses
 *     (e.g. move up, move left, apply jump).
 */
public class MarioSpine extends PlayerSpine {
	private static final float WALKMOVE_XIMP = 0.025f;
	private static final float MIN_WALKVEL = WALKMOVE_XIMP * 2f;
	private static final float MAX_WALKVEL = WALKMOVE_XIMP * 42f;
	private static final float DECEL_XIMP = WALKMOVE_XIMP * 1.37f;
	private static final float BRAKE_XIMP = WALKMOVE_XIMP * 2.75f;
	private static final float RUNMOVE_XIMP = WALKMOVE_XIMP * 1.5f;
	private static final float MAX_RUNVEL = MAX_WALKVEL * 1.65f;
	private static final float RUNJUMP_MULT = 0.25f;
	private static final float MAX_RUNJUMPVEL = MAX_RUNVEL;
	private static final float JUMP_IMPULSE = 1.75f;
	private static final float JUMP_FORCE = 25.25f;
	private static final float JUMPFORCE_MAXTIME = 0.5f;
	private static final float MAX_FALL_VELOCITY = UInfo.P2M(5f * 60f);
	private static final float AIRMOVE_XIMP = WALKMOVE_XIMP;
	private static final float MAX_DUCKSLIDE_VEL = MAX_WALKVEL * 0.65f;
	private static final float DUCKSLIDE_XIMP = WALKMOVE_XIMP * 1f;
	private static final float HEADBOUNCE_VEL = 2.8f;	// up velocity

	private OneWayContactSensor damagePushSensor;

	public MarioSpine(MarioBody body) {
		super(body);
		damagePushSensor = null;
	}

	// sensor for detecting damage push begin contacts
	public OneWayContactSensor createDamagePushSensor() {
		damagePushSensor = new OneWayContactSensor(body, true);
		return damagePushSensor;
	}

	/*
	 * Apply walk impulse and cap horizontal velocity.
	 */
	public void applyWalkMove(boolean moveRight, boolean run) {
		// run impulse or walk impulse?
		float impulse = run ? RUNMOVE_XIMP : WALKMOVE_XIMP;
		// max run velocity or max walk velocity?
		float max = run ? MAX_RUNVEL : MAX_WALKVEL;
		applyHorizImpulseAndCapVel(moveRight, impulse, max);
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
			// if decelerated too hard to the left then set x velocity to zero
			if(body.getVelocity().x <= UInfo.VEL_EPSILON)
				body.setVelocity(0f, body.getVelocity().y);
		}
		// if moving left...
		else if(body.getVelocity().x < -MIN_WALKVEL) {
			// ... and facing left or ducking, then decelerate with soft impulse to the right
			if(!facingRight && !ducking)
				applyHorizontalImpulse(false, -DECEL_XIMP);
			// ... and facing right, then decelerate with hard impulse to the right
			else
				applyHorizontalImpulse(false, -BRAKE_XIMP);
			// if decelerated too hard to the right then set x velocity to zero
			if(body.getVelocity().x >= -UInfo.VEL_EPSILON)
				body.setVelocity(0f, body.getVelocity().y);
		}
		// not moving right or left fast enough, set horizontal velocity to zero to avoid wobbling
		else
			body.setVelocity(0f, body.getVelocity().y);
	}

	public void applyJumpImpulse() {
		// the faster mario is moving, the higher he jumps, up to a max
		float mult = Math.abs(body.getVelocity().x) / MAX_RUNJUMPVEL;
		// cap the multiplier
		if(mult > 1f)
			mult = 1f;
		// 
		mult = 1f + mult * RUNJUMP_MULT;

		body.applyImpulse(new Vector2 (0f, JUMP_IMPULSE * mult));
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
		applyHorizImpulseAndCapVel(moveRight, AIRMOVE_XIMP, MAX_RUNVEL);
	}

	public void applyDuckSlideMove(boolean moveRight) {
		applyHorizImpulseAndCapVel(moveRight, DUCKSLIDE_XIMP, MAX_DUCKSLIDE_VEL);
	}

	public void applyHeadBounce() {
		applyPlayerHeadBounce(HEADBOUNCE_VEL);
	}

	public boolean isBraking(boolean facingRight) {
		// if facing right and body is moving left, or if facing left and body is moving right then return true
		return (facingRight && body.getVelocity().x < -MIN_WALKVEL) ||
				(!facingRight && body.getVelocity().x > MIN_WALKVEL);
	}

	public List<Agent> getPushDamageContacts() {
		return damagePushSensor.getAndResetContacts();
	}

	public void capFallVelocity() {
		capFallVelocity(MAX_FALL_VELOCITY);
	}

	public boolean isStandingStill() {
		return isStandingStill(MIN_WALKVEL);
	}
}
