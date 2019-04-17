package kidridicarus.game.agent.SMB1.NPC.turtle;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.agentspine.SMB1.KoopaSpine;

public class TurtleSpine extends KoopaSpine {
	private static final float WALK_VEL = 0.4f;
	private static final float BUMP_UP_VEL = 2f;
	private static final float BUMP_SIDE_VEL = 0.4f;
	private static final float SLIDE_VEL = 2f;

	public TurtleSpine(TurtleBody body) {
		super(body);
	}

	public void doWalkMove(boolean isFacingRight) {
		if(isFacingRight)
			body.setVelocity(WALK_VEL, body.getVelocity().y);
		else
			body.setVelocity(-WALK_VEL, body.getVelocity().y);
	}

	public void doDeadBumpContactsAndMove(boolean bumpRight) {
		((TurtleBody) body).allowOnlyDeadBumpContacts();
		if(bumpRight)
			body.setVelocity(BUMP_SIDE_VEL, BUMP_UP_VEL);
		else
			body.setVelocity(-BUMP_SIDE_VEL, BUMP_UP_VEL);
	}

	public void doSlideMove(boolean isFacingRight) {
		if(isFacingRight)
			body.setVelocity(SLIDE_VEL, body.getVelocity().y);
		else
			body.setVelocity(-SLIDE_VEL, body.getVelocity().y);
	}

	public boolean isOtherAgentOnRight(Agent other) {
		if(other == null)
			return false;
		// if other Agent doesn't have position then return false
		Vector2 otherPos = AP_Tool.getCenter(other);
		if(otherPos == null)
			return false;

		return otherPos.x > body.getPosition().x;
	}
}
