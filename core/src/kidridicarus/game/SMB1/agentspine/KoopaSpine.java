package kidridicarus.game.SMB1.agentspine;

import java.util.List;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.common.agentsensor.OneWayContactSensor;
import kidridicarus.common.agentspine.SolidContactSpine;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.SMB1.agent.Koopa;

/*
 * Is "Koopa" correct?
 * Turtles are Koopa, but are Goombas also Koopa?
 */
public class KoopaSpine extends SolidContactSpine {
	private HeadBounceNerve hbNerve;

	public KoopaSpine(AgentBody body) {
		super(body);
		hbNerve = new HeadBounceNerve();
	}

	public OneWayContactSensor createHeadBounceSensor() {
		return hbNerve.createHeadBounceSensor(body);
	}

	public List<Agent> getHeadBounceBeginContacts() {
		return hbNerve.getHeadBounceBeginContacts();
	}

	/*
	 * The use of AND, OR, NOT operations is dense here, so read carefully.
	 * Example 1:
	 *   Given isFacingRight = true, useAgents = true.
	 *   Returns true if
	 *     move right is blocked by solid or if move right is blocked by a Koopa Agent, and
	 *     move left is not blocked by solid and move left is not blocked by a Koopa Agent.
	 *   Otherwise returns false.
	 * Example 2:
	 *   Given isFacingRight = true, useAgents = false.
	 *   Returns true if
	 *     move right is blocked by solid, and
	 *     move left is not blocked by solid.
	 *   Otherwise returns false.
	 * isFacingRight=false examples are trivial.
	 */
	public boolean isKoopaSideMoveBlocked(boolean isFacingRight, boolean useAgents) {
		// If regular move is blocked...
		// ... and reverse move is not also blocked then reverse.
		return  (isSideMoveBlocked(isFacingRight) || (useAgents && isMoveBlockedByKoopa(isFacingRight))) &&
				(!isSideMoveBlocked(!isFacingRight) && (!useAgents || !isMoveBlockedByKoopa(!isFacingRight)));
	}

	private boolean isMoveBlockedByKoopa(boolean moveRight) {
		for(Agent otherAgent : agentSensor.getContacts()) {
			// skip other Agent if not on team Kooopa or if other doesn't have position
			if(!(otherAgent instanceof Koopa))
				continue;
			Vector2 otherPos = AP_Tool.getCenter(otherAgent);
			if(otherPos == null)
				continue;

			// If wants to move right and other agent is on the right side then move is blocked, or
			// if wants to move left and other agent is on the left side then move is blocked.
			if((moveRight && body.getPosition().x < otherPos.x) ||
					(!moveRight && body.getPosition().x > otherPos.x)) {
				return true;
			}
		}
		return false;
	}

	public boolean isDeadBumpRight(Vector2 position) {
		return position.x > body.getPosition().x;
	}
}
