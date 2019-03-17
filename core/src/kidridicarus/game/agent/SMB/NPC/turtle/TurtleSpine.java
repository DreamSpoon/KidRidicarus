package kidridicarus.game.agent.SMB.NPC.turtle;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agentspine.GoombaTurtleShareSpine;

public class TurtleSpine extends GoombaTurtleShareSpine {
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

	public boolean isDeadBumpOnRight(Vector2 position) {
		if(position.x > body.getPosition().x)
			return true;
		else
			return false;
	}

	public boolean isOtherAgentOnRight(Agent other) {
		if(other == null)
			return false;
		return other.getPosition().x > body.getPosition().x;
	}
}
