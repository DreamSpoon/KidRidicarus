package kidridicarus.game.agent.KidIcarus.NPC.shemum;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.game.agentspine.SMB.HeadBounceSpine;

public class ShemumSpine extends HeadBounceSpine {
	private static final float GOOMBA_WALK_VEL = 0.4f;
	private static final float BUMP_UP_VEL = 2f;
	private static final float BUMP_SIDE_VEL = 0.4f;

	public ShemumSpine(ShemumBody body) {
		super(body);
	}

	public void doWalkMove(boolean isFacingRight) {
		if(isFacingRight)
			body.setVelocity(GOOMBA_WALK_VEL, body.getVelocity().y);
		else
			body.setVelocity(-GOOMBA_WALK_VEL, body.getVelocity().y);
	}

	public void doDeadSquishContactsAndMove() {
		((ShemumBody) body).allowOnlyDeadSquishContacts();
		body.zeroVelocity(true, true);
	}

	public void doDeadBumpContactsAndMove(boolean bumpRight) {
		((ShemumBody) body).allowOnlyDeadBumpContacts();
		if(bumpRight)
			body.setVelocity(BUMP_SIDE_VEL, BUMP_UP_VEL);
		else
			body.setVelocity(-BUMP_SIDE_VEL, BUMP_UP_VEL);
	}

	public boolean isDeadBumpRight(Vector2 position) {
		return position.x < body.getPosition().x;
	}
}
