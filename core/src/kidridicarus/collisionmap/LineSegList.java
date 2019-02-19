package kidridicarus.collisionmap;

import java.util.Iterator;
import java.util.TreeSet;

public class LineSegList {
	TreeSet<LineSeg> lineSegs;

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

	public Iterator<LineSeg> getIterator() {
		return lineSegs.iterator();
	}
}
