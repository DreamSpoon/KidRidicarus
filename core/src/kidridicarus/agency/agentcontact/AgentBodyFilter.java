package kidridicarus.agency.agentcontact;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.info.CommonCF;

public class AgentBodyFilter {
	public CFBitSeq categoryBits;
	public CFBitSeq maskBits;
	public Object userData;

	public AgentBodyFilter(CFBitSeq categoryBits, CFBitSeq maskBits, Object userData) {
		this.categoryBits = categoryBits;
		this.maskBits = maskBits;
		this.userData = userData;
	}

	public static boolean isContact(AgentBodyFilter filterA, AgentBodyFilter filterB) {
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
//		getAgentFromFilter(filterA).getBounds();
//		getAgentFromFilter(filterB).getBounds();
		CFBitSeq catBitsA = filterA.categoryBits;
		CFBitSeq catBitsB = filterB.categoryBits;

		// if both fixtures have the SEMISOLID_BIT then ignore the bit completely - they cannot collide
		if(catBitsA.and(CommonCF.Alias.SEMISOLID_FLOOR_BIT).isNonZero() &&
				catBitsB.and(CommonCF.Alias.SEMISOLID_FLOOR_BIT).isNonZero()) {
			// remove the semi-solid bit from catBitsA and catBitsB
			catBitsA = catBitsA.and(new CFBitSeq(true, CommonCF.Alias.SEMISOLID_FLOOR_BIT));
			catBitsB = catBitsB.and(new CFBitSeq(true, CommonCF.Alias.SEMISOLID_FLOOR_BIT));
		}
		// if fixture A is semi solid and fixture B can contact semi-solid...
		else {
			Agent agentA = getAgentFromFilter(filterA);
			Agent agentB = getAgentFromFilter(filterB);

			// if fixture A is the semi-solid and fixture B maybe contacts it...
			if(catBitsA.and(CommonCF.Alias.SEMISOLID_FLOOR_BIT).isNonZero() &&
				filterB.maskBits.and(CommonCF.Alias.SEMISOLID_FLOOR_BIT).isNonZero()) {
				// If fixture B is above fixture A then do contact test like normal,
				// otherwise remove the semi-solid bit from fixture A.
				// If the top of the semi-solid fixture is above the bottom of the other fixture then remove
				// semi-solid bit and do contact test.
				if(agentA != null && agentB != null &&
						agentA.getBounds().y + agentA.getBounds().height > agentB.getBounds().y) {
					catBitsA = catBitsA.and(new CFBitSeq(true, CommonCF.Alias.SEMISOLID_FLOOR_BIT));
				}
			}
			// if fixture B is the semi-solid and fixture A maybe contacts it...
			else if(catBitsB.and(CommonCF.Alias.SEMISOLID_FLOOR_BIT).isNonZero() &&
				filterA.maskBits.and(CommonCF.Alias.SEMISOLID_FLOOR_BIT).isNonZero()) {
				// If fixture A is above fixture B then do contact test like normal,
				// otherwise remove the semi-solid bit from fixture B.
				// If the top of the semi-solid fixture is above the bottom of the other fixture then
				// remove semi-solid bit and do contact test.
				if(agentA != null && agentB != null &&
						agentB.getBounds().y + agentB.getBounds().height > agentA.getBounds().y) {
					catBitsB = catBitsB.and(new CFBitSeq(true, CommonCF.Alias.SEMISOLID_FLOOR_BIT));
				}
			}
		}

		return catBitsA.and(filterB.maskBits).isNonZero() && catBitsB.and(filterA.maskBits).isNonZero();
	}

	/*
	 * Check the various levels of the chain to find a reference to an agent, and return if found. 
	 */
	public static Agent getAgentFromFilter(AgentBodyFilter obj) {
		if(obj.userData instanceof Agent)
			return (Agent) obj.userData;
		else if(obj.userData instanceof AgentBody)
			return ((AgentBody) obj.userData).getParent();
		else if(obj.userData instanceof AgentContactSensor) {
			Object p = ((AgentContactSensor) obj.userData).getParent();
			if(p instanceof Agent)
				return (Agent) p;
			else if(p instanceof AgentBody)
				return ((AgentBody) p).getParent();
		}
		return null;
	}
}
