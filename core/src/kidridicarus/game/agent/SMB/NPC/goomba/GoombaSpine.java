package kidridicarus.game.agent.SMB.NPC.goomba;

import java.util.List;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.SolidBoundSensor;
import kidridicarus.common.agentspine.OnGroundSpine;

public class GoombaSpine extends OnGroundSpine {
	private static final float GOOMBA_WALK_VEL = 0.4f;
	private static final float BUMP_UP_VEL = 2f;
	private static final float BUMP_SIDE_VEL = 0.4f;

	private GoombaBody body;
	// horizontal move sensor
	private SolidBoundSensor hmSensor;
	private AgentContactHoldSensor acSensor;

	public GoombaSpine(GoombaBody body) {
		this.body = body;
		hmSensor = null;
		acSensor = null;
	}

	public SolidBoundSensor createHorizontalMoveSensor() {
		hmSensor = new SolidBoundSensor(body);
		return hmSensor;
	}

	public AgentContactHoldSensor createAgentSensor() {
		acSensor = new AgentContactHoldSensor(body);
		return acSensor;
	}

	public void doWalkMove(boolean isFacingRight) {
		if(isFacingRight)
			body.setVelocity(GOOMBA_WALK_VEL, body.getVelocity().y);
		else
			body.setVelocity(-GOOMBA_WALK_VEL, body.getVelocity().y);
	}

/*	public void doStopAndDisableAgentContacts() {
		body.zeroVelocity(true, true);
		body.setAgentSensorEnabled(false);
	}
*/
	public void doBumpAndDisableAllContacts(boolean bumpRight) {
		body.setMainSolid(false);
		body.setAgentSensorEnabled(false);
		if(bumpRight)
			body.setVelocity(BUMP_SIDE_VEL, BUMP_UP_VEL);
		else
			body.setVelocity(-BUMP_SIDE_VEL, BUMP_UP_VEL);
	}

	public boolean isDeadBumpRight(Vector2 position) {
		if(position.x < body.getPosition().x)
			return true;
		else
			return false;
	}

	public boolean isMoveBlocked(boolean moveRight) {
		return hmSensor.isHMoveBlocked(body.getBounds(), moveRight);
	}

	public boolean isMoveBlockedByAgent(boolean moveRight) {
		return AgentContactHoldSensor.isMoveBlockedByAgent(acSensor, body.getPosition(), moveRight);
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

	public <T> List<T> getContactAgentsByClass(Class<T> cls) {
		return acSensor.getContactsByClass(cls);
	}
}
