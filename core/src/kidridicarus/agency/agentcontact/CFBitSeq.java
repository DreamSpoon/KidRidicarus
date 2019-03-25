package kidridicarus.agency.agentcontact;

import java.util.HashSet;
import java.util.Iterator;

/*
 * Title: Contact Filter (CF) Bit Sequence
 * Desc:
 * A collection of contact filter bits identified by String, no duplicates allowed.
 * "Bitwise operations" supported:
 *   AND
 *   Set all bits to 0 (full off)
 *   Set all bits to 1 (full on)
 * 
 * Implemented with a boolean to indicate full on or full off, and a hashset of Strings where each String is
 * an alias to a bit. Thus, collision filter bits can be added and removed "on the fly" by simply using a
 * new unique String.
 */
public class CFBitSeq {
	private HashSet<String> bits;
	private boolean isOne;

	public CFBitSeq(boolean isOne) {
		this.isOne = isOne;
		bits = new HashSet<String>();
	}

	public CFBitSeq(String ...bitsInput) {
		bits = new HashSet<String>();
		for(String b : bitsInput)
			bits.add(b);
	}

	public CFBitSeq(CFBitSeq seqInput) {
		bits = new HashSet<String>();
		if(seqInput.isOne)
			this.isOne = true;
		else
			setToBits(seqInput.bits);
	}

	public CFBitSeq(HashSet<String> bits) {
		this.isOne = false;
		this.bits = new HashSet<String>(bits);
	}

	private void setToBits(HashSet<String> bits) {
		Iterator<String> bitsIter = bits.iterator();
		while(bitsIter.hasNext())
			this.bits.add(bitsIter.next());
	}

	public CFBitSeq and(CFBitSeq bitsInput) {
		if(bitsInput.isOne)
			return new CFBitSeq(this);
		else if (this.isOne)
			return new CFBitSeq(bitsInput);

		// start a new empty set of bits
		HashSet<String> andBits = new HashSet<String>();
		// check each of our bits against the bitsInput for matching bits, keep track of matching bits 
		for(String b : bitsInput.bits) {
			if(this.bits.contains(b))
				andBits.add(b);
		}
		// return the new set of matching bits
		return new CFBitSeq(andBits);
	}

	public void setZero() {
		bits.clear();
		isOne = false;
	}

	public void setOne() {
		bits.clear();
		isOne = true;
	}

	// are zero bits present in the set?
	public boolean isZero() {
		if(isOne)
			return false;
		else
			return bits.isEmpty();
	}

	// are any bits present in the set?
	public boolean isNonZero() {
		if(isOne)
			return true;
		else
			return !bits.isEmpty();
	}

	public boolean isOne() {
		return isOne;
	}

	/*
	 * Returns false if other is not an instance of CFBitSeq, or if any bits differ between other and this.
	 * Returns true otherwise.
	 */
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof CFBitSeq))
			return super.equals(other);

		// if the other bit sequence has a different number of bits than this sequence then they are not equal
		if(bits.size() != ((CFBitSeq) other).bits.size())
			return false;
		// check each bit against the other for differences, return false if any difference found
		for(String b : ((CFBitSeq) other).bits)
			if(!bits.contains(b))
				return false;
		// no differences found, return true
		return true;
	}

	@Override
	public String toString() {
		Iterator<String> bitsIter = bits.iterator();
		StringBuilder strb = new StringBuilder("[\"");
		while(bitsIter.hasNext()) {
			strb.append(bitsIter.next());
			if(bitsIter.hasNext())
				strb.append("\", \"");
		}
		return strb.append("\"]").toString();
	}
}
