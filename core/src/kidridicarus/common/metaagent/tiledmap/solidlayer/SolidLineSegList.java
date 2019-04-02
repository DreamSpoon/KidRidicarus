package kidridicarus.common.metaagent.tiledmap.solidlayer;

import java.util.Iterator;
import java.util.TreeSet;

public class SolidLineSegList {
	TreeSet<SolidLineSeg> lineSegs;

	public SolidLineSegList() {
		lineSegs = new TreeSet<SolidLineSeg>(new SolidLineSeg.LineSegComparator());
	}

	public boolean add(SolidLineSeg seg) {
		// check for segment overlap/adjacency with segments already in the list
		if(lineSegs.contains(seg))
			throw new IllegalArgumentException("Segment is already in segment list - overlap/adjacency is not allowed.");
		// add
		return lineSegs.add(seg);
	}

	public boolean remove(SolidLineSeg seg) {
		return lineSegs.remove(seg);
	}

	public Iterator<SolidLineSeg> getIterator() {
		return lineSegs.iterator();
	}
}
