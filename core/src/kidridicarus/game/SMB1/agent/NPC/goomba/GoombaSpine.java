package kidridicarus.game.SMB1.agent.NPC.goomba;

import kidridicarus.game.SMB1.agentspine.KoopaSpine;

class GoombaSpine extends KoopaSpine {
	private static final float WALK_VEL = 0.4f;
	private static final float BUMP_UP_VEL = 2f;
	private static final float BUMP_SIDE_VEL = 0.4f;

	GoombaSpine(GoombaBody body) {
		super(body);
	}

	void doWalkMove(boolean isFacingRight) {
		if(isFacingRight)
			body.setVelocity(WALK_VEL, body.getVelocity().y);
		else
			body.setVelocity(-WALK_VEL, body.getVelocity().y);
	}

	void doDeadSquishContactsAndMove() {
		((GoombaBody) body).allowOnlyDeadSquishContacts();
		body.zeroVelocity(true, true);
	}

	void doDeadBumpContactsAndMove(boolean bumpRight) {
		((GoombaBody) body).allowOnlyDeadBumpContacts();
		if(bumpRight)
			body.setVelocity(BUMP_SIDE_VEL, BUMP_UP_VEL);
		else
			body.setVelocity(-BUMP_SIDE_VEL, BUMP_UP_VEL);
	}
}
