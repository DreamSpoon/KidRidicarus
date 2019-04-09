package kidridicarus.game.agentspine.SMB1;

import java.util.List;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentsensor.OneWayContactSensor;
import kidridicarus.common.agentspine.SolidContactSpine;
import kidridicarus.game.agent.SMB1.Koopa;

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
		for(Agent agent : agentSensor.getContacts()) {
			if(!(agent instanceof Koopa))
				continue;

			// If wants to move right and other agent is on the right side then move is blocked, or
			// if wants to move left and other agent is on the left side then move is blocked.
			if((moveRight && body.getPosition().x < agent.getPosition().x) ||
					(!moveRight && body.getPosition().x > agent.getPosition().x))
				return true;
		}
		return false;
	}
}
