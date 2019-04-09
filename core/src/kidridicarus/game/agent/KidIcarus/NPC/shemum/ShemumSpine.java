package kidridicarus.game.agent.KidIcarus.NPC.shemum;

import kidridicarus.common.agentspine.SolidContactSpine;

public class ShemumSpine extends SolidContactSpine {
	private static final float WALK_VEL = 0.3f;

	public ShemumSpine(ShemumBody body) {
		super(body);
	}

	public void doWalkMove(boolean isFacingRight) {
		if(isFacingRight)
			body.setVelocity(WALK_VEL, body.getVelocity().y);
		else
			body.setVelocity(-WALK_VEL, body.getVelocity().y);
	}
}
