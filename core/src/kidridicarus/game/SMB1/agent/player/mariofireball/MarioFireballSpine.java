package kidridicarus.game.SMB1.agent.player.mariofireball;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentspine.SolidContactSpine;
import kidridicarus.common.info.UInfo;

class MarioFireballSpine extends SolidContactSpine {
	protected static final Vector2 MOVE_VEL = new Vector2(2.4f, 2.25f);

	MarioFireballSpine(MarioFireballBody body) {
		super(body);
	}

	void doVelocityCheck() {
		// if body is currently at zero Y velocity, and if previously at zero or negative velocity, then bounce up
		if(UInfo.epsCheck(body.getVelocity().y, 0f, UInfo.VEL_EPSILON) &&
				((MarioFireballBody) body).getPrevVelocity().y < UInfo.VEL_EPSILON) {
			body.setVelocity(body.getVelocity().x, MOVE_VEL.y);
		}
		// cap up velocity
		else if(body.getVelocity().y > MOVE_VEL.y)
			body.setVelocity(body.getVelocity().x, MOVE_VEL.y);
		// cap down velocity
		else if(body.getVelocity().y < -MOVE_VEL.y)
			body.setVelocity(body.getVelocity().x, -MOVE_VEL.y);
	}

	boolean isHitBoundary(boolean facingRight) {
		return isSideMoveBlocked(facingRight) ||
				(body.getVelocity().x <= 0f && facingRight) || (body.getVelocity().x >= 0f && !facingRight);
	}

	void startExplode() {
		body.disableAllContacts();
		body.setVelocity(0f, 0f);
		((MarioFireballBody) body).setGravityScale(0f);
	}
}
