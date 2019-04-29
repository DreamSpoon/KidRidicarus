package kidridicarus.agency.agentbody;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentContactListener.PreSolver;

public class AgentBodyFilter {
	public CFBitSeq categoryBits;
	public CFBitSeq maskBits;
	public Object userData;
	public PreSolver preSolver;

	public AgentBodyFilter(CFBitSeq categoryBits, CFBitSeq maskBits, Object userData) {
		this.categoryBits = categoryBits;
		this.maskBits = maskBits;
		this.userData = userData;
		// null indicates default preSolver, non-null for custom preSolver
		this.preSolver = null;
	}

	public static boolean isContact(AgentBodyFilter filterA, AgentBodyFilter filterB) {
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
