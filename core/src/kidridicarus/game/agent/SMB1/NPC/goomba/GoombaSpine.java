package kidridicarus.game.agent.SMB1.NPC.goomba;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentspine.SMB_NPC_Spine;

public class GoombaSpine extends SMB_NPC_Spine {
	private static final float WALK_VEL = 0.4f;
	private static final float BUMP_UP_VEL = 2f;
	private static final float BUMP_SIDE_VEL = 0.4f;

	public GoombaSpine(GoombaBody body) {
		super(body);
	}

	public void doWalkMove(boolean isFacingRight) {
		if(isFacingRight)
			body.setVelocity(WALK_VEL, body.getVelocity().y);
		else
			body.setVelocity(-WALK_VEL, body.getVelocity().y);
	}

	public void doDeadSquishContactsAndMove() {
		((GoombaBody) body).allowOnlyDeadSquishContacts();
		body.zeroVelocity(true, true);
	}

	public void doDeadBumpContactsAndMove(boolean bumpRight) {
		((GoombaBody) body).allowOnlyDeadBumpContacts();
		if(bumpRight)
			body.setVelocity(BUMP_SIDE_VEL, BUMP_UP_VEL);
		else
			body.setVelocity(-BUMP_SIDE_VEL, BUMP_UP_VEL);
	}

	public boolean isDeadBumpRight(Vector2 position) {
		return position.x < body.getPosition().x;
	}
}
