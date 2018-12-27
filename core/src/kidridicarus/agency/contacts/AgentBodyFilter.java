package kidridicarus.agency.contacts;

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
		return filterA.categoryBits.and(filterB.maskBits).isNonZero() &
				filterB.categoryBits.and(filterA.maskBits).isNonZero();
	}
}
