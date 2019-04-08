package kidridicarus.game.agent.KidIcarus.NPC.shemum;

import kidridicarus.common.agentspine.SMB_NPC_Spine;

public class ShemumSpine extends SMB_NPC_Spine {
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
