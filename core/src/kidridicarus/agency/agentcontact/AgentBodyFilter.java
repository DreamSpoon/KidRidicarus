package kidridicarus.agency.agentcontact;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;

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
		 * If both agents have the SEMISOLID_BIT then ignore the bit completely - they cannot collide
		 * because the one will always be below the other! (or visa versa, so always true)
		 * If A has the category bit set and B has mask bit set, then check B and if B is above A,
		 * then return true because B struck the the semi-solid floor A from above.
		 * If A has the category bit set and B has mask bit set, then check B and if B is below A,
		 * then B cannot strike the semi-solid floor A from below - so remove the SEMISOLID_BIT and do
		 * the test as if the SEMISOLID_BIT was not present.
		 */
//		if(filterA.c)
//		getAgentFromFilter(filterA).getBounds();
//		getAgentFromFilter(filterB).getBounds();

		return filterA.categoryBits.and(filterB.maskBits).isNonZero() &&
				filterB.categoryBits.and(filterA.maskBits).isNonZero();
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
