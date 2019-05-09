package kidridicarus.agency.tool;

/*
 * A wrapper for a boolean and a comparable (in this case 'float' order is used).
 * During comparison, if allow == false then order is ignored.
 * order is important only when allow == true.
 */
public class AllowOrder implements Comparable<AllowOrder> {
	public static final AllowOrder NOT_ALLOWED = new AllowOrder(false, 0f);
	public final boolean allow;
	final float order;

	public AllowOrder(boolean allow, float order) {
		this.allow = allow;
		this.order = order;
	}

	@Override
	public boolean equals(Object other) {
		if(other == null)
			return false;
		// if both are not allowed then order does not matter
		else if(!this.allow && !((AllowOrder) other).allow)
			return true;
		// check allow state and order state
		return (other instanceof AllowOrder && this.allow == ((AllowOrder) other).allow &&
				((AllowOrder) other).order == this.order);
	}

	/*
	 * Objects that are not allowed (i.e. allow = false) will be at the end of a list, therefore the compareTo
	 * operator must return "greater than" if "this" is not allowed and "other" is allowed.
	 */
	@Override
	public int compareTo(AllowOrder other) {
		if(equals(other))
			return 0;
		// if this is allowed and other is not allowed then return "less than"
		else if(this.allow && !other.allow)
			return -1;
		// if this is not allowed and other is allowed then return "greater than"
		else if(!this.allow && other.allow)
			return 1;
		// either both are allowed or both are not allowed, so compare their order to determine higher
		else if(this.order > other.order)
			return 1;
		else
			return -1;
	}
}
