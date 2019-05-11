package kidridicarus.game.Metroid.agent.player.samus;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.playeragent.PlayerAgentBody;
import kidridicarus.common.agent.playeragent.PlayerSpine;
import kidridicarus.common.info.UInfo;

class SamusSpine extends PlayerSpine {
	private static final float GROUNDMOVE_XIMP = 0.28f;
	private static final float MAX_GROUNDMOVE_VEL = 0.85f;
	private static final float MIN_WALK_VEL = 0.1f;
	private static final float STOPMOVE_XIMP = 0.15f;
	private static final float AIRMOVE_XIMP = GROUNDMOVE_XIMP * 0.7f;
	private static final float MAX_AIRMOVE_VEL = MAX_GROUNDMOVE_VEL;
	private static final float JUMPUP_FORCE = 6.15f;
	private static final float JUMPUP_CONSTVEL = 1.6f;
	private static final Vector2 DAMAGE_KICK_SIDE_IMP = new Vector2(1.8f, 0f);
	private static final Vector2 DAMAGE_KICK_UP_IMP = new Vector2(0f, 1.3f);
	private static final float MAX_UP_VELOCITY = 1.75f;
	private static final float MAX_DOWN_VELOCITY = 2.5f;
	private static final float HEADBOUNCE_VEL = 1.4f;	// up velocity

	SamusSpine(SamusBody body) {
		super(body);
	}

	// apply walk impulse and cap horizontal velocity.
	void applyWalkMove(boolean moveRight) {
		applyHorizImpulseAndCapVel(moveRight, GROUNDMOVE_XIMP, MAX_GROUNDMOVE_VEL);
	}

	// apply air impulse and cap horizontal velocity.
	void applyAirMove(boolean moveRight) {
		applyHorizImpulseAndCapVel(moveRight, AIRMOVE_XIMP, MAX_AIRMOVE_VEL);
	}

	void applyStopMove() {
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

	void applyJumpForce(float forceTimer, float jumpForceDuration) {
		if(forceTimer < jumpForceDuration)
			body.applyForce(new Vector2(0f, JUMPUP_FORCE * forceTimer / jumpForceDuration));
	}

	void applyDamageKick(Vector2 position) {
		// zero the y velocity
		body.setVelocity(body.getVelocity().x, 0);
		// apply a kick impulse to the left or right depending on other agent's position
		if(body.getPosition().x < position.x)
			body.applyImpulse(DAMAGE_KICK_SIDE_IMP.cpy().scl(-1f));
		else
			body.applyImpulse(DAMAGE_KICK_SIDE_IMP);

		// apply kick up impulse if the player is above the other agent
		if(body.getPosition().y > position.y)
			body.applyImpulse(DAMAGE_KICK_UP_IMP);
	}

	void applyHeadBounce() {
		applyPlayerHeadBounce(HEADBOUNCE_VEL);
	}

	boolean isNoHorizontalVelocity() {
		return isStandingStill(MIN_WALK_VEL);
	}

	void applyJumpVelocity() {
		body.setVelocity(body.getVelocity().x, JUMPUP_CONSTVEL);
	}

	// check for bounce against solid floor
	void doBounceCheck() {
		// Check for bounce up (no left/right bounces, no down bounces).
		// Since body restitution=0, bounce occurs when current velocity=0 and previous velocity > 0.
		// Check against 0 using velocity epsilon.
		if(UInfo.epsCheck(body.getVelocity().y, 0f, UInfo.VEL_EPSILON)) {
			float amount = -((PlayerAgentBody) body).getPrevVelocity().y;
			if(amount > MAX_DOWN_VELOCITY-UInfo.VEL_EPSILON)
				amount = MAX_UP_VELOCITY-UInfo.VEL_EPSILON;
			else
				amount = amount * 0.6f;
			body.setVelocity(body.getVelocity().x, amount);
		}
	}
}
