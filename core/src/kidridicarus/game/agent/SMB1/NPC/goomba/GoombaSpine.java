package kidridicarus.game.agent.SMB1.NPC.goomba;

import kidridicarus.game.agentspine.SMB1.KoopaSpine;

public class GoombaSpine extends KoopaSpine {
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
}
