package kidridicarus.game.agent.SMB.NPC.turtle;

import java.util.List;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.despawnbox.DespawnBox;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.SolidBoundSensor;
import kidridicarus.common.agentspine.OnGroundSpine;

public class TurtleSpine extends OnGroundSpine {
	private static final float WALK_VEL = 0.4f;
	private static final float BUMP_UP_VEL = 2f;
	private static final float BUMP_SIDE_VEL = 0.4f;

	private TurtleBody body;
	private AgentContactHoldSensor acSensor;
	// horizontal move sensor
	private SolidBoundSensor hmSensor;

	public TurtleSpine(TurtleBody body) {
		this.body = body;
		hmSensor = null;
		acSensor = null;
	}

	public AgentContactHoldSensor createAgentContactSensor() {
		acSensor = new AgentContactHoldSensor(body);
		return acSensor;
	}

	public SolidBoundSensor createHorizontalMoveSensor() {
		hmSensor = new SolidBoundSensor(body);
		return hmSensor;
	}

	public void doWalkMove(boolean isFacingRight) {
		if(isFacingRight)
			body.setVelocity(WALK_VEL, body.getVelocity().y);
		else
			body.setVelocity(-WALK_VEL, body.getVelocity().y);
	}

	public void doBumpAndDisableAllContacts(boolean bumpRight) {
		body.disableAllContacts();
		if(bumpRight)
			body.setVelocity(BUMP_SIDE_VEL, BUMP_UP_VEL);
		else
			body.setVelocity(-BUMP_SIDE_VEL, BUMP_UP_VEL);
	}

	public <T> List<T> getContactAgentsByClass(Class<T> cls) {
		return acSensor.getContactsByClass(cls);
	}

	public boolean checkReverseVelocity(boolean isFacingRight) {
		// if regular move is blocked...
		if(isMoveBlocked(isFacingRight) ||
				isMoveBlockedByAgent(isFacingRight)) {
			// ... and reverse move is not also blocked then reverse 
			if(!isMoveBlocked(!isFacingRight) &&
					!isMoveBlockedByAgent(!isFacingRight)) {
				return true;
			}
		}
		return false;
	}

	private boolean isMoveBlocked(boolean moveRight) {
		return hmSensor.isHMoveBlocked(body.getBounds(), moveRight);
	}

	private boolean isMoveBlockedByAgent(boolean moveRight) {
		return AgentContactHoldSensor.isMoveBlockedByAgent(acSensor, body.getPosition(), moveRight);
	}

	public boolean isDeadBumpRight(Vector2 position) {
		if(position.x < body.getPosition().x)
			return true;
		else
			return false;
	}

	public boolean isContactDespawn() {
		return acSensor.getFirstContactByClass(DespawnBox.class) != null;
	}
}
