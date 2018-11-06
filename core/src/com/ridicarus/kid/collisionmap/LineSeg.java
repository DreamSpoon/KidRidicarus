package com.ridicarus.kid.collisionmap;

import java.util.Comparator;

import com.badlogic.gdx.physics.box2d.Body;
import com.ridicarus.kid.GameInfo;

public class LineSeg {
	public int begin, end;	// in tile coordinates
	public Body body;
	public boolean isHorizontal;

	public LineSeg(int begin, int end, boolean isHorizontal) {
		if(begin > end)
			throw new IllegalArgumentException("begin > end exception, begin = " + begin + ", end = " + end);
		this.begin = begin;
		this.end = end;
		this.isHorizontal = isHorizontal;
	}

	public static class LineSegComparator implements Comparator<LineSeg> {
		// If segA is completely to the left of segB, with no overlap, then return -1.
		// If segA is completely to the right of segB, with no overlap, then return +1.
		// Line segments are checked for overlap, inclusively. If overlap exists then return 0.
		//
		// Overlap examples:
		// -----------------
		// E.g. 0)
		//     If:
		//         segA begins at 2 and ends at 4, and
		//         segB begins at 4 and ends at 4
		//     Then:
		//         segA and segB overlap
		//
		// E.g. 1)
		//     If:
		//         segA begins at 2 and ends at 4, and
		//         segB begins at 4 and ends at 10
		//     Then:
		//         segA and segB overlap
		//
		// E.g. 2)
		//     If:
		//         segA begins at 2 and ends at 4, and
		//         segB begins at 5 and ends at 10
		//     Then:
		//         segA and segB do not overlap
		//
		// E.g. 3)
		//     If:
		//         segA begins at 2 and ends at 4, and
		//         segB begins at 6 and ends at 10
		//     Then:
		//         segA and segB do not overlap
		// etc.
		// Assumption is made that horizontal lines will be compared only with horizontal lines and vertical lines
		// will be compared only with vertical lines. Horizontal lines should be in separate list(s) from vertical
		// lines. 
		@Override
		public int compare(LineSeg segA, LineSeg segB) {
			if((segA.isHorizontal && !segB.isHorizontal) ||
				(!segA.isHorizontal && segB.isHorizontal)) {
				throw new IllegalArgumentException("Cannot compare horizontal and vertical line segments.");
			}
			if(segA.end < segB.begin)
				return -1;	// no overlap, segment A is left of segment B.
			else if(segA.begin > segB.end)
				return 1;	// no overlap, segment A is right of segment B.
			else
				return 0;	// overlap
		}
	}

	public String toString( ) {
		return (String) "LineSeg: { begin=" + begin + ", end=" + end + ", isHorizontal= " + isHorizontal + "}";
	}

	public float getWorldBegin() {
		if(isHorizontal)
			return GameInfo.P2M(begin * GameInfo.TILEPIX_X);
		else
			return GameInfo.P2M(begin * GameInfo.TILEPIX_Y);
	}

	public float getWorldEnd() {
		if(isHorizontal)
			return GameInfo.P2M((end+1) * GameInfo.TILEPIX_X);
		else
			return GameInfo.P2M((end+1) * GameInfo.TILEPIX_Y);
	}
}
