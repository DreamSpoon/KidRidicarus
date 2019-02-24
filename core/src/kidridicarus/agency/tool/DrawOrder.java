package kidridicarus.agency.tool;

/*
 * To draw or not to draw...
 * And when?
 */
public class DrawOrder implements Comparable<DrawOrder> {
	public boolean draw;	// if true then draw
	public float order;		// if least then draw first

	public DrawOrder(boolean draw, float order) {
		this.draw = draw;
		this.order = order;
	}

	@Override
	public boolean equals(Object other) {
		return (other instanceof DrawOrder && this.draw == ((DrawOrder) other).draw &&
				((DrawOrder) other).order == this.order);
	}

	/*
	 * Objects that are not drawn (i.e. draw = false) should appear at the end of a list, therefore the compareTo
	 * operator must return "greater than" if "this" is not drawn and "other" is drawn.
	 */
	@Override
	public int compareTo(DrawOrder other) {
		if(equals(other))
			return 0;
		// if this is drawn and other is not drawn then return "less than"
		else if(this.draw && !other.draw)
			return -1;
		// if this is not drawn and other is drawn then return "greater than"
		else if(!this.draw && other.draw)
			return 1;
		// both are either drawn or both are not drawn, so compare their draw order to determine higher
		else if(this.order > other.order)
			return 1;
		else
			return -1;
	}

	@Override
	public String toString() {
		return "[this="+this.hashCode()+", draw="+this.draw+", order="+this.order+"]";
	}
}
