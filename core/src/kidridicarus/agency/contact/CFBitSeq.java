package kidridicarus.agency.contact;

import java.util.Iterator;
import java.util.TreeSet;

/*
 * Title: Contact Filter (CF) Bit Sequence
 * Desc:
 * A collection of contact filter bits, no duplicates. "Bitwise operations" supported:
 *   AND
 *   OR
 *   Set all bits to 0 (full off)
 *   Set all bits to 1 (full on)
 * 
 * A special flag bit, THE_ONE_BIT, is used to indicate when all "bits" are "on". There can be no more than one
 * THE_ONE_BIT in a CF bit sequence.
 *
 * TODO: Refactor to use Strings instead of enums, to increase flexibility.
 */
public class CFBitSeq {
	/*
	 * Title: Contact Filter Bit
	 */
	public enum CFBit { THE_ONE_BIT, AGENT_BIT, SOLID_BOUND_BIT, ROOM_BIT, DESPAWN_BIT, SPAWNBOX_BIT,
		SPAWNTRIGGER_BIT, BUMPABLE_BIT, PIPE_BIT, ITEM_BIT;
	}

	private TreeSet<CFBit> bits;

	public CFBitSeq(CFBit ...bitsInput) {
		boolean allBits = false;
		bits = new TreeSet<CFBit>();
		for(CFBit b : bitsInput) {
			bits.add(b);
			// If any of the bits are 'ALL_BITS', then keep only the 'ALL_BITS' and discard any other bits since
			// the other bits are redundant
			if(b == CFBit.THE_ONE_BIT) {
				allBits = true;
				break;
			}
		}
		if(allBits) {
			bits = new TreeSet<CFBit>();
			// one bit to rule them all, one bit to find them
			bits.add(CFBit.THE_ONE_BIT);
		}
	}

	public CFBitSeq(CFBitSeq seqInput) {
		this(seqInput.bits);
	}

	private CFBitSeq(TreeSet<CFBit> bitsInput) {
		if(bitsInput.contains(CFBit.THE_ONE_BIT)) {
			bits = new TreeSet<CFBit>();
			bits.add(CFBit.THE_ONE_BIT);
		}
		else
			bits = new TreeSet<CFBit>(bitsInput);
	}

	public CFBitSeq or(CFBitSeq seqInput) {
		if(seqInput.bits.contains(CFBit.THE_ONE_BIT))
			return new CFBitSeq(CFBit.THE_ONE_BIT);

		// copy all of our bits into a new set
		TreeSet<CFBit> orBits = new TreeSet<CFBit>(bits);
		// add bitsInput bits that are not already in the set
		for(CFBit b : seqInput.bits) {
			if(!orBits.contains(b))
				orBits.add(b);
		}
		// return the new set of bits, no duplicates
		return new CFBitSeq(orBits);
	}

	public CFBitSeq and(CFBitSeq seqInput) {
		if(seqInput.bits.contains(CFBit.THE_ONE_BIT))
			return new CFBitSeq(this);

		// start a new empty set of bits
		TreeSet<CFBit> andBits = new TreeSet<CFBit>();
		// check each of our bits against the bitsInput for matching bits, keep track of matching bits 
		for(CFBit b : seqInput.bits) {
			if(bits.contains(b))
				andBits.add(b);
		}
		// return the new set of matching bits
		return new CFBitSeq(andBits);
	}

	public void setZero() {
		bits.clear();
	}

	public void setOne() {
		bits.clear();
		bits.add(CFBit.THE_ONE_BIT);
	}

	// are zero bits present in the set?
	public boolean isZero() {
		return bits.isEmpty();
	}

	// are any bits present in the set?
	public boolean isNonZero() {
		return !bits.isEmpty();
	}

	public boolean isOne() {
		return bits.contains(CFBit.THE_ONE_BIT);
	}

	@Override
	public boolean equals(Object other) {
		if(!(other instanceof CFBitSeq))
			return super.equals(other);

		// if the other bit sequence has a different number of bits than this sequence then they are not equal
		if(bits.size() != ((CFBitSeq) other).bits.size())
			return false;
		for(CFBit b : ((CFBitSeq) other).bits)
			if(!bits.contains(b))
				return false;
		return true;
	}

	public String toString() {
		Iterator<CFBit> d = bits.iterator();
		StringBuilder strb = new StringBuilder();
		while(d.hasNext()) {
			strb.append(d.next().toString());
			if(d.hasNext())
				strb.append(", ");
		}
		return strb.toString();
	}
}
