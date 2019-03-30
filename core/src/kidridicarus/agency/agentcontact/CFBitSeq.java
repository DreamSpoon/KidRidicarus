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
 * 
 * If isOneMinus = false then bits are "additive".
 * If isOneMinus = true then bits are "subtractive".
 * In other words:
 *   If isOneMinus = false, bits are interpreted as adding from zero.
 *   If isOneMinus = false, bits are interpreted as subtracting from one.
 *   
 * But what if a bit sequence is initially set to one and then "all" the "bits" are subtracted/removed?
 * The bit sequence is still one, and the bits subtracted are stored in the bits set. If, subsequently,
 * the AND bit-wise operation is applied to the bit sequence then it will "allow" bits that were not
 * subtracted/removed - and it will "block" bits that were subtracted/removed.
 *   
 * To properly remove "all" "bits", set the bit sequence to zero.  
 * 
 * This makes me think of this as a 0 to 1 infinity thing:
 *   "Bits" can be "added" to zero to increase, but an infinite number of "bits" "added" to zero does not
 *   add up to one. And visa versa; bits can be "subtracted" from one, but no matter how many "bits" are
 *   "subtracted" from one, the result never reaches zero.
 * 
 * What to call these two states?
 * 0+
 * 1-
 * zero-up
 * one-down
 */
public class CFBitSeq {
	private boolean isOneMinus;
	private HashSet<String> bits;

	public CFBitSeq(boolean isOneMinus) {
		this.isOneMinus = isOneMinus;
		bits = new HashSet<String>();
	}

	public CFBitSeq(String ...bitsInput) {
		this.isOneMinus = false;
		bits = new HashSet<String>();
		for(String bit : bitsInput)
			bits.add(bit);
	}

	public CFBitSeq(boolean isOneMinus, String ...bitsInput) {
		this.isOneMinus = isOneMinus;
		bits = new HashSet<String>();
		for(String bit : bitsInput)
			bits.add(bit);
	}

	public CFBitSeq(boolean isOneMinus, HashSet<String> bits) {
		this.isOneMinus = isOneMinus;
		this.bits = new HashSet<String>(bits);
	}

	public CFBitSeq and(String ...bitsInput) {
		return and(new CFBitSeq(bitsInput));
	}

	public CFBitSeq and(CFBitSeq otherSeq) {
		HashSet<String> andBits = new HashSet<String>();

		// if this and other sequence are both 1- then the AND operation is just a join of the two bit sets
		if(this.isOneMinus && otherSeq.isOneMinus) {
			andBits.addAll(this.bits);
			andBits.addAll(otherSeq.bits);
			return new CFBitSeq(true, andBits);
		}
		// if this and the other sequence are both 0+ then do a traditional AND operation
		else if(!this.isOneMinus && !otherSeq.isOneMinus) {
			// check each of our bits against the bitsInput for matching bits, keep track of matching bits 
			for(String otherBit : otherSeq.bits) {
				if(this.bits.contains(otherBit))
					andBits.add(otherBit);
			}
			return new CFBitSeq(false, andBits);
		}
		// if this sequence is 1- and the other sequence is 0+
		else if(this.isOneMinus && !otherSeq.isOneMinus) {
			// zero plus: start with zero, add all of other sequence's "additive" bits
			andBits.addAll(otherSeq.bits);
			// one minus: remove all of this sequence's "subtractive" bits
			for(String bit : this.bits) {
				if(andBits.contains(bit))
					andBits.remove(bit);
			}
			return new CFBitSeq(false, andBits);
		}
		// if this sequence is 0+ and the other sequence is 1-
		else {
			// zero plus: start with zero, add all of this sequence's "additive" bits
			andBits.addAll(this.bits);
			// one minus: remove all of other sequence's "subtractive" bits
			for(String otherBit : otherSeq.bits) {
				if(andBits.contains(otherBit))
					andBits.remove(otherBit);
			}
			return new CFBitSeq(false, andBits);
		}
	}

	public void setZero() {
		bits.clear();
		isOneMinus = false;
	}

	public void setOne() {
		bits.clear();
		isOneMinus = true;
	}

	/*
	 * If isOne = false, and no "additive" bits are listed, then this sequence is exactly zero so return true.
	 * Otherwise return false.
	 */
	public boolean isZero() {
		return !isOneMinus && bits.isEmpty();
	}

	/*
	 * If isOne = true and no "subtractive" bits are listed, then this sequence is exactly one so return true.
	 * Otherwise return false.
	 */
	public boolean isOne() {
		return isOneMinus && bits.isEmpty();
	}

	/*
	 * If isOne = true then this sequence cannot be zero so return true,
	 * if isOne = false and any "additive" bits are listed then this sequence cannot be zero so return true,
	 * Otherwise return false.
	 * Note: Is this equivalency true?
	 *   isNonZero = isNonOne
	 */
	public boolean isNonZero() {
		return isOneMinus || !bits.isEmpty(); 
	}

	/*
	 * Returns false if other is not an instance of CFBitSeq, or if any bits differ between other and this.
	 * Returns true otherwise.
	 */
	@Override
	public boolean equals(Object otherSeq) {
		if(!(otherSeq instanceof CFBitSeq))
			return super.equals(otherSeq);

		// if the other bit sequence has a different number of bits than this sequence then they are not equal
		if(bits.size() != ((CFBitSeq) otherSeq).bits.size())
			return false;
		// check each bit against the other for differences, return false if any difference found
		for(String otherBit : ((CFBitSeq) otherSeq).bits)
			if(!bits.contains(otherBit))
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
/*
public class CFBitSeq {
	private HashSet<String> bits;
	private boolean isOne;

	public CFBitSeq(boolean isOne) {
		this.isOne = isOne;
		bits = new HashSet<String>();
	}

	public CFBitSeq(String ...bitsInput) {
		this.isOne = false;
		bits = new HashSet<String>();
		for(String b : bitsInput)
			bits.add(b);
	}

	public CFBitSeq(CFBitSeq seqInput) {
		this.isOne = false;
		bits = new HashSet<String>();
		if(seqInput.isOne)
			this.isOne = true;
		else {
			Iterator<String> bitsIter = seqInput.bits.iterator();
			while(bitsIter.hasNext())
				this.bits.add(bitsIter.next());
		}
	}

	public CFBitSeq(HashSet<String> bits) {
		this.isOne = false;
		this.bits = new HashSet<String>(bits);
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
*/
	/*
	 * Returns false if other is not an instance of CFBitSeq, or if any bits differ between other and this.
	 * Returns true otherwise.
	 */
/*	@Override
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
*/
