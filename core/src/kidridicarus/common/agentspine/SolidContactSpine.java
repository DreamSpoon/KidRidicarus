package kidridicarus.common.agentspine;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentsensor.SolidContactSensor;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;

public class SolidContactSpine extends BasicAgentSpine {
	private SolidContactNerve scNerve;

	public SolidContactSpine(AgentBody body) {
		super(body);
		scNerve = new SolidContactNerve();
	}

	public SolidContactSensor createSolidContactSensor() {
		return scNerve.createSolidContactSensor(body);
	}

	public boolean isOnGround() {
		return scNerve.isOnGround(body.getBounds());
	}

	public boolean isOnCeiling() {
		return scNerve.isOnCeiling(body.getBounds());
	}

	public boolean isSideMoveBlocked(boolean isRight) {
		if((isRight && scNerve.isDirSolid(Direction4.RIGHT, body.getBounds())) ||
				(!isRight && scNerve.isDirSolid(Direction4.LEFT, body.getBounds())))
			return true;
		return false;
	}

	/*
	 * Use current contacts and given velocity (which may be different from body velocity!) to determine
	 * if movement in direction given by velocity is blocked.
	 * Returns true if blocked. Otherwise returns false.
	 */
	public boolean isMoveBlocked(Vector2 velocity) {
		Direction4 horizontalMove = Direction4.NONE;
		Direction4 verticalMove = Direction4.NONE;
		if(velocity.x > UInfo.VEL_EPSILON)
			horizontalMove = Direction4.RIGHT;
		else if(velocity.x < -UInfo.VEL_EPSILON)
			horizontalMove = Direction4.LEFT;
		if(velocity.y > UInfo.VEL_EPSILON)
			verticalMove = Direction4.UP;
		else if(velocity.y < -UInfo.VEL_EPSILON)
			verticalMove = Direction4.DOWN;
		// if move is blocked in either direction then return true
		if(horizontalMove != Direction4.NONE && scNerve.isDirSolid(horizontalMove, body.getBounds()))
			return true;
		if(verticalMove != Direction4.NONE && scNerve.isDirSolid(verticalMove, body.getBounds()))
			return true;
		// not blocked in either direction so return false
		return false;
	}
}
