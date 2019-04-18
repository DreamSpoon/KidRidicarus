package kidridicarus.common.agent.semisolidfloor;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.agentcontact.AgentContactListener.PreSolver;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.AP_Tool;

public class SemiSolidPreSolver implements PreSolver {
	private AgentBodyFilter myFilter;

	public SemiSolidPreSolver(AgentBodyFilter myFilter) {
		this.myFilter = myFilter;
	}

	/*
	 * If neither fixture has the SEMISOLID_BIT then do the test like normal.
	 * If both fixtures have the SEMISOLID_BIT then ignore the bit completely - they cannot collide
	 * because the one will always be below the other! (or visa versa, so always true)
	 * If A has the semi-solid category bit set and B has the semi-solid mask bit set,
	 * and if B is above A, then return true because B struck the the semi-solid floor A from above.
	 * If A has the semi-solid category bit set and B has the semi-solid mask bit set,
	 * and if B is below A, then B cannot strike the semi-solid floor A from below - so remove the
	 * SEMISOLID_BIT and do the test as if the SEMISOLID_BIT was not present.
	 */
	@Override
	public boolean preSolve(Object otherUserData) {
		// if unable to use body filter data from other Agent then assume contact is enabled and return true
		if(!(otherUserData instanceof AgentBodyFilter))
			return true;
		AgentBodyFilter otherFilter = (AgentBodyFilter) otherUserData;
		CFBitSeq otherCatBits = otherFilter.categoryBits;
		CFBitSeq myCatBits = myFilter.categoryBits;

		// if both fixtures have the SEMISOLID_BIT then they cannot collide
		if(myCatBits.and(CommonCF.Alias.SEMISOLID_FLOOR_BIT).isNonZero() &&
				otherCatBits.and(CommonCF.Alias.SEMISOLID_FLOOR_BIT).isNonZero()) {
			return false;
		}

		// If the other Agent would not contact my Agent due to the semi-solid bit being present, then a
		// filter check must still be performed with the other contact filter bits - while ignoring the
		// semi-solid contact bit entirely. 

		// If the top of my semi-solid fixture is above the bottom of the other fixture then remove
		// semi-solid bit before performing contact test.
		Agent myAgent = AgentBodyFilter.getAgentFromFilter(myFilter);
		Agent otherAgent = AgentBodyFilter.getAgentFromFilter(otherFilter);
		if(myAgent != null && otherAgent != null) {
			// If both Agents have bounds, and Agent's top bound is below other Agent's bottom bound,
			// then remove semi-solid bit.
			Rectangle myAgentBounds = AP_Tool.getBounds(myAgent);
			Rectangle otherAgentBounds = AP_Tool.getBounds(otherAgent);
			if(myAgentBounds != null && otherAgentBounds != null &&
					myAgentBounds.y + myAgentBounds.height > otherAgentBounds.y) {
				myCatBits = myCatBits.and(new CFBitSeq(true, CommonCF.Alias.SEMISOLID_FLOOR_BIT));
			}
		}
		// return results of filter test with the (possibly modified) filter categories
		return myCatBits.and(otherFilter.maskBits).isNonZero() && otherCatBits.and(myFilter.maskBits).isNonZero();
	}
}
