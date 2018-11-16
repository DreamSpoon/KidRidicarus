package com.ridicarus.kid.collisionmap;

import java.util.Iterator;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.ridicarus.kid.GameInfo;

// Since full / empty tiles are technically not tracked, any destroy cell / add cell operations are fun!
public class TileCollisionMap {
	private World world;
	TiledMapTileLayer layer;

	private int width;
	private int height;
	private int tileWidth;
	private int tileHeight;
	private LineSegList[] hLines;
	private LineSegList[] vLines;

	// 1) width and height are given in number of tiles (not number of pixels).
	// 2) tileWidth and tileHeight are given in pixels.
	// 3) The TiledMapTileLayer 'layer' should not be edited except through this class after instantiating this
	//    class - since any tile adds/deletes in 'layer' must be synchronized with this class. 
	public TileCollisionMap(World world, TiledMapTileLayer layer, int width, int height, int tileWidth, int tileHeight) {
		this.world = world;
		this.layer = layer;
		this.width = width;
		this.height = height;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;

		hLines = new LineSegList[height+1];
		for(int i=0; i<=height; i++)
			hLines[i] = new LineSegList();

		vLines = new LineSegList[width+1];
		for(int i=0; i<=width; i++)
			vLines[i] = new LineSegList();

		createBodiesForTiles();
	}

	private void createBodiesForTiles() {
		calculateLineSegs();
		createBodies();
	}

	// Create a minimal line segment based boundary set based on the map input - for collision geometry
	// creation/tracking/removal.
	// Use one dimensional integer line segments.
	// The goal: Each non-empty (non-null) tile in the map layer will be surrounded by bounding lines (a bounding
	// rectangle).
	// The problems:
	//     1) Irrelevant lines may be created (e.g. where two bricks are side by side, the overlapping/coincident
	//        lines should be erased).
	//     2) Parallel lines that share vertexes need to be fused together, to reduce the total number of line
	//        segments without changing the boundaries.
	//
	// Brute force solution:
	//     Loop through all non-empty tiles and create boxes for each, then remove unnecessary lines and fuse lines
	//     where possible. This may necessitate something like a quadtree to track all the lines, and that's so
	//     annoying to implement.
	//
	// My solution:
	//     Think of the problem as: How to create the minimum amount of of horizontal line segments, and vertical line
	//     segments, to represent the boundaries of the non-empty tiles.
	//     The horizontal lines, although crossing the vertical lines, are not affected by the vertical lines. That is,
	//     the calculation of the horizontal lines is independent of the calculation of the vertical lines.
	//     Since the horizontal lines are evenly spaced on the y-axis, it's simple to add them to an array/tree
	//     structure for the y-dimension, then for each row create an array/tree to store the lines on the x-dimension. 
	//     The lines are non-overlapping and non-adjacent (since overlapping and adjacent lines will be fused whenever
	//     inserted).
	//
	//     So,
	//         1) Loop through the tiles from left-to-right, to create the horizontal lines.
	//         2) Loop through the tiles from bottom-to-top, to create the vertical lines.
	//     Caveat: This algorithm is designed specifically with a regular tileset of same sized rectangles in mind - every
	//         tile must be the same size as every other tile.
	//         Other shapes may not be compatible, and may be added later using a different algorithm (?).  
	private void calculateLineSegs() {
		TiledMapTile me, belowMe, leftOfMe;
		LineSeg currentSeg;

		// Create horizontal lines.
		// Note the less than or equal (<=) compare on the 'y' iterator only.
		// In each iteration of the loop, the bottom of each tile is checked for line creation - but not the top.
		// Therefore an extra iteration is needed to check the top line of the final tile (which is the bottom line
		// of the tile above it).
		// DEBUG: insert a picture here to explain, words are confusing...
		currentSeg = null;
		for(int y = 0; y <= layer.getHeight(); y++) {
			for(int x = 0; x < layer.getWidth(); x++) {
				me = getTile(layer, x, y);
				belowMe = getTile(layer, x, y-1);

				// If the current tile and the tile below it are both empty then no line segment is
				// needed because there is just empty space.
				// If the current tile and the one below it are both full (non-empty) then no line segment is needed
				// since the tiles are adjacent and a line segment would be redundant.
				if((me == null && belowMe == null) || (me != null && belowMe != null)) {
					if(currentSeg != null) {
						// no segment needed for this tile, but a line segment has already been started...
						// finish the current segment and add it to the list (each y value has it's own list)
						currentSeg.end = x-1;
						hLines[y].add(currentSeg);

						currentSeg = null;
					}
				}
				else {	// create/continue line segment
					// start a new segment if none currently exists
					if(currentSeg == null)
						currentSeg = new LineSeg(x, x, true);
					else
						currentSeg.end = x;
				}
			}

			// at the end of the x axis check, if a line segment exists, then add to list and reset current
			if(currentSeg != null) {
				hLines[y].add(currentSeg);

				currentSeg = null;
			}
		}

		// Create vertical lines.
		currentSeg = null;
		for(int x = 0; x <= layer.getWidth(); x++) {
			for(int y = 0; y < layer.getHeight(); y++) {
				me = getTile(layer, x, y);
				leftOfMe = getTile(layer, x-1, y);

				// If the current tile and the tile left of it are both empty then no line segment is
				// needed because there is just empty space.
				// If the current tile and the one left of it are both full (non-empty) then no line segment is needed
				// since the tiles are adjacent and a line segment would be redundant.
				if((me == null && leftOfMe == null) || (me != null && leftOfMe != null)) {
					if(currentSeg != null) {
						// no segment needed for this tile, but a line segment has already been started...
						// finish the current segment and add it to the list (each y value has it's own list)
						currentSeg.end = y-1;
						vLines[x].add(currentSeg);

						currentSeg = null;
					}
				}
				else {	// create/continue line segment
					// start a new segment if none currently exists
					if(currentSeg == null)
						currentSeg = new LineSeg(y, y, false);
					else
						currentSeg.end = y;
				}
			}

			// at the end of the y axis check, if a line segment exists, then add to list and reset current
			if(currentSeg != null) {
				vLines[x].add(currentSeg);

				currentSeg = null;
			}
		}
	}

	// A "safe" tile getter that will cope gracefully with out of bounds cell requests.
	// Returns null if x or y are out of bounds,
	// Returns null if the cell is null (empty),
	// Otherwise, returns the tile at (x, y).
	private TiledMapTile getTile(TiledMapTileLayer layer, int x, int y) {
		Cell c;
		if(x < 0 || y < 0 || x >= layer.getWidth() || y >= layer.getHeight())
			return null;

		c = layer.getCell(x, y);
		if(c == null)
			return null;

		return c.getTile();
	}

	// create Box2d bodies for the horizontal and vertical line segments
	private void createBodies() {
		// loop through rows
		for(int y=0; y<=height; y++) {
			// loop through row's line segments
			Iterator<LineSeg> segIter = hLines[y].getIterator();
			while(segIter.hasNext()) {
				LineSeg seg = segIter.next();
				seg.body = defineHLine(y, seg);
			}
		}

		// loop through columns
		for(int x=0; x<=width; x++) {
			// loop through row's line segments
			Iterator<LineSeg> segIter = vLines[x].getIterator();
			while(segIter.hasNext()) {
				LineSeg seg = segIter.next();
				seg.body = defineVLine(x, seg);
			}
		}
	}

	private Body defineHLine(int yRow, LineSeg seg) {
		// Add +1 to end, because the line segment ends on the right side of end.
		// Consider the case where the segment is one wide. Then seg.begin would equal seg.end.
		// So we need to add one.
		return defineLine(seg.begin, yRow, seg.end+1, yRow, seg);
	}

	private Body defineVLine(int xCol, LineSeg seg) {
		return defineLine(xCol, seg.begin, xCol, seg.end+1, seg);
	}

	private Body defineLine(int startX, int startY, int endX, int endY, LineSeg seg) {
		BodyDef bdef;
		FixtureDef fdef;
		EdgeShape edgeShape;
		Body body;

		bdef = new BodyDef();
		bdef.position.set(GameInfo.P2M(startX * tileWidth), GameInfo.P2M(startY * tileHeight));
		bdef.type = BodyDef.BodyType.StaticBody;
		body = world.createBody(bdef);

		fdef = new FixtureDef();
		edgeShape = new EdgeShape();
		edgeShape.set(0f, 0f, GameInfo.P2M((endX - startX) * tileWidth), GameInfo.P2M((endY - startY) * tileHeight));
		fdef.filter.categoryBits = GameInfo.BOUNDARY_BIT;
//		fdef.filter.maskBits = ...

		fdef.shape = edgeShape;
		body.createFixture(fdef).setUserData(seg);

		return body;
	}

	// Adding or removing a tile in the collision map's line segment list requires the same algorithm:
	//     Treat each of the 4 sides of the tile as a line segment, and toggle them on/off
	//     (i.e. on becomes off, or off becomes on).
	// Note: This makes no assumption about whether the tile is being added or removed, it's simply toggling
	//       from existant to non-existant - like this:
	//       tileExists = !tileExists;
	public void toggleTile(int x, int y) {
		// bottom horizontal segment of tile
		toggleSegment(hLines, true, x, y);
		// top horizontal segment of tile
		toggleSegment(hLines, true, x, y+1);
		// left vertical segment of tile
		toggleSegment(vLines, false, y, x);
		// right vertical segment of tile
		toggleSegment(vLines, false, y, x+1);
	}

	// Use this function to process both horizontal and vertical lines, since the code is basically the same
	// for both operations.
	// horvLines means Horizontal or Vertical lines, just FYI
	private void toggleSegment(LineSegList[] horvLines, boolean isHorizontal, int x, int y) {
		LineSeg testSeg;
		LineSeg floorSeg;
		LineSeg higherSeg;
		LineSeg newSeg;

		int left, right;

		testSeg = new LineSeg(x, x, isHorizontal);
		floorSeg = horvLines[y].lineSegs.floor(testSeg);
		higherSeg = horvLines[y].lineSegs.higher(testSeg);

		// adjacency on right? 
		if(higherSeg != null && higherSeg.begin == x+1) {
			// If there is adjacency on right, then there cannot be overlap from 'floor', or left, seg.
			// The left seg (the 'floor' seg) might be adjacent to x, however.
			if(floorSeg != null && floorSeg.end == x-1) {
				// If adjacency on left and right, then fuse the two line segments into a new bigger one.

				left = floorSeg.begin;
				right = higherSeg.end;

				// destroy old segments
				world.destroyBody(floorSeg.body);
				world.destroyBody(higherSeg.body);
				horvLines[y].remove(floorSeg);
				horvLines[y].remove(higherSeg);

				// create newer, bigger segment...
			}
			else {
				// if adjacency on right only, then extend segment
				left = higherSeg.begin-1;
				right = higherSeg.end;

				// destroy old segment
				world.destroyBody(higherSeg.body);
				horvLines[y].remove(higherSeg);

				// create newer, bigger segment..
			}
			// ... create newer, bigger segment
			newSeg = new LineSeg(left, right, isHorizontal);
			horvLines[y].add(newSeg);
			if(isHorizontal)
				newSeg.body = defineHLine(y, newSeg);
			else
				newSeg.body = defineVLine(y, newSeg);
		}
		else {
			// given:
			//     1) no segments adjacent on the right
			//
			// 5 possible cases:
			//     0) segment exists only at x. e.g.
			//            seg = (4, 4)
			//            x = 4
			//     1) segment overlaps and extends beyond x. e.g.
			//            seg = (3, 7)
			//            x = 5
			//     2) segment overlaps, but ends at x. e.g.
			//            seg = (3, 7)
			//            x = 7
			//     3) segment is adjacent but not overlapping. e.g.
			//            seg = (3, 7)
			//            x = 8
			//     4) left of x, no adjacency, no overlap (this would produce the same result as floorSeg == null)
			//            seg = (3, 7)
			//            x = 9
			if(floorSeg != null && floorSeg.end >= x-1) {
				if(floorSeg.begin == x && floorSeg.end == x) {
					// one segment, exists only at x

					// destroy the segment, do not create new segment(s)
					world.destroyBody(floorSeg.body);
					horvLines[y].remove(floorSeg);
				}
				else if(floorSeg.end > x) {
					// floorSeg overlaps and extends beyond x

					// removing left beginning segment?
					if(floorSeg.begin == x) {
						// shorten the old segment by 1 tile

						// destroy old segment
						world.destroyBody(floorSeg.body);
						horvLines[y].remove(floorSeg);

						newSeg = new LineSeg(floorSeg.begin+1, floorSeg.end, isHorizontal);
						horvLines[y].add(newSeg);
						if(isHorizontal)
							newSeg.body = defineHLine(y, newSeg);
						else
							newSeg.body = defineVLine(y, newSeg);
					}
					else {
						// break the segment into two pieces with a gap at x
	
						// destroy old segment
						world.destroyBody(floorSeg.body);
						horvLines[y].remove(floorSeg);
	
						// create two new segments
						LineSeg newLeftSeg, newRightSeg;
						newLeftSeg = new LineSeg(floorSeg.begin, x-1, isHorizontal);
						newRightSeg = new LineSeg(x+1, floorSeg.end, isHorizontal);
						horvLines[y].add(newLeftSeg);
						horvLines[y].add(newRightSeg);
						if(isHorizontal) {
							newLeftSeg.body = defineHLine(y, newLeftSeg);
							newRightSeg.body = defineHLine(y, newRightSeg);
						}
						else {
							newLeftSeg.body = defineVLine(y, newLeftSeg);
							newRightSeg.body = defineVLine(y, newRightSeg);
						}
					}
				}
				else if(floorSeg.end == x) {
					// floorSeg overlaps and ends at x

					// shorten floorSeg by one tile

					// destroy old segment
					world.destroyBody(floorSeg.body);
					horvLines[y].remove(floorSeg);

					newSeg = new LineSeg(floorSeg.begin, x-1, isHorizontal);
					horvLines[y].add(newSeg);
					if(isHorizontal)
						newSeg.body = defineHLine(y, newSeg);
					else
						newSeg.body = defineVLine(y, newSeg);
				}
				else {
					// floorSeg is adjacent on the left side and does not overlap x

					// extend floorSeg by one tile

					// destroy old segment
					world.destroyBody(floorSeg.body);
					horvLines[y].remove(floorSeg);

					newSeg = new LineSeg(floorSeg.begin, x, isHorizontal);
					horvLines[y].add(newSeg);
					if(isHorizontal)
						newSeg.body = defineHLine(y, newSeg);
					else
						newSeg.body = defineVLine(y, newSeg);
				}
			}
			else {
				// nothing to connect to on the left or the right, just create a lone segment
				newSeg = new LineSeg(x, x, isHorizontal);
				horvLines[y].add(newSeg);
				if(isHorizontal)
					newSeg.body = defineHLine(y, newSeg);
				else
					newSeg.body = defineVLine(y, newSeg);
			}
		}
	}
}
