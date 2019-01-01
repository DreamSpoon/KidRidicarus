package kidridicarus.collisionmap;

import java.util.Iterator;
import java.util.TreeSet;

public class LineSegList {
	public TreeSet<LineSeg> lineSegs;

	public LineSegList() {
		lineSegs = new TreeSet<LineSeg>(new LineSeg.LineSegComparator());
	}

	public boolean add(LineSeg seg) {
		// check for segment overlap/adjacency with segments already in the list
		if(lineSegs.contains(seg))
			throw new IllegalArgumentException("Segment is already in segment list - overlap/adjacency is not allowed.");
		// add
		return lineSegs.add(seg);
	}

	public boolean remove(LineSeg seg) {
		return lineSegs.remove(seg);
	}

	/*
	 * Return a 2 element LineSeg array:
	 *     { [a 'left' LineSeg reference, or a null], [a 'right' LineSeg reference, or a null] }.
	 * 
	 * If the 'left' LineSeg is not null:
	 *     It is either a LineSeg that is adjacent to the left of offset, or
	 *     a LineSeg that overlaps offset.
	 * If the 'right' LineSeg is not null:
	 *     It is a LineSeg that overlaps offset or is adjacent to the right of offset.
	 * 
	 * Does not return null in any case, but may return an array of nulls.
	 * 
	 * possible TODO: Should this be getAdjacentOrOverlap()?
	 */
	public static final int LEFT_SEG = 0;
	public static final int RIGHT_SEG = 1;
	public LineSeg[] getAdjacentAndOverlap(int offset, boolean isHorizontal) {	// 
		LineSeg testSeg;
		LineSeg floorSeg;
		LineSeg higherSeg;
		LineSeg adjOverlapArray[];

		// init to empty
		adjOverlapArray = new LineSeg[] { null, null };

		// If the set is empty then return nothing found.
		// (don't return null, instead return a ref to nulls)
		if(lineSegs.isEmpty()) return adjOverlapArray;

		// Create a one tile test segment to represent 'offset' (otherOffest and upNormal are set arbitrarily as
		// they are not used in the tests) 
		testSeg = new LineSeg(offset, offset, 0, isHorizontal, true);
		// test for segment <= to 'testSeg' (overlap possible)
		floorSeg = lineSegs.floor(testSeg);
		// test for segment > 'testSeg' (overlap impossible)
		higherSeg = lineSegs.higher(testSeg);

		// Even if floorSeg != null, it may be completely to the left of 'offset' - just the nearest segment on the
		// left - still need to test for adjacency/overlap. 
		if(floorSeg != null) {
			// Regarding floorSeg...
			// Is this an adjacent/overlapping segment?
			// Adjacent if: end = offset-1
			// Overlapping if: end > offset-1
			// Either way, put it in the left return value.
			if(floorSeg.end >= offset-1)
				adjOverlapArray[LEFT_SEG] = floorSeg;	// adjacent on left or overlap
		}

		// Even if higherSeg != null, it may be completely to the right of 'offset' - and just the closest segment on
		// the right - test for adjacency/overlap. 
		if(higherSeg != null) {
			// DEBUG: Since higherSeg cannot overlap, the following if should be begin == offset+1,
			// since begin < offset+1 is impossible.
			if(higherSeg.begin <= offset+1)
				adjOverlapArray[RIGHT_SEG] = higherSeg;	// adjacent on right
		}

		return adjOverlapArray;
	}

	public Iterator<LineSeg> getIterator() {
		return lineSegs.iterator();
	}
}
