package kidridicarus.game.agentspine.SMB;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentspine.OnGroundSpine;
import kidridicarus.common.info.UInfo;

public class PlayerSpine extends OnGroundSpine {
	protected AgentBody body;

	public PlayerSpine(AgentBody body) {
		this.body = body;
	}

	protected void applyHorizontalImpulse(boolean moveRight, float amt) {
		if(moveRight)
			body.applyImpulse(new Vector2(amt, 0f));
		else
			body.applyImpulse(new Vector2(-amt, 0f));
	}

	/*
	 * Ensure horizontal velocity is within -max to +max.
	 */
	protected void capHorizontalVelocity(float max) {
		if(body.getVelocity().x > max)
			body.setVelocity(max, body.getVelocity().y);
		else if(body.getVelocity().x < -max)
			body.setVelocity(-max, body.getVelocity().y);
	}

	protected void applyHorizImpulseAndCapVel(boolean moveRight, float xImpulse, float maxXvel) {
		applyHorizontalImpulse(moveRight, xImpulse);
		capHorizontalVelocity(maxXvel);
	}

	// maxVelocity must be positive because it is multiplied by -1 in the logic
	protected void capFallVelocity(float maxVelocity) {
		if(body.getVelocity().y < -maxVelocity)
			body.setVelocity(body.getVelocity().x, -maxVelocity);
	}

	/*
	 * The argument is named minWalkVelocity, and not maxStandVelocity, because minWalkVelocity is used
	 * by the player classes already.
	 */
	public boolean isStandingStill(float minWalkVelocity) {
		return (body.getVelocity().x > -minWalkVelocity && body.getVelocity().x < minWalkVelocity);
	}

	public boolean isMovingUp() {
		return body.getVelocity().y > UInfo.VEL_EPSILON;
	}

	public boolean isMovingDown() {
		return body.getVelocity().y < -UInfo.VEL_EPSILON;
	}

	public boolean isMovingRight() {
		return body.getVelocity().x > UInfo.VEL_EPSILON;
	}

	public boolean isMovingLeft() {
		return body.getVelocity().x < -UInfo.VEL_EPSILON;
	}
}
