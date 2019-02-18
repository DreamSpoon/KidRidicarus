package kidridicarus.agency.helper;

import java.util.concurrent.LinkedBlockingQueue;

import kidridicarus.collisionmap.TileCollisionMap;

/*
 * Store a queue of collision map tiles that need to be switched (e.g. switching a solid tile to non-solid).
 */
public class SolidTileChangeQueue {
	private LinkedBlockingQueue<SolidTile> changeQueue;

	public SolidTileChangeQueue() {
		changeQueue = new LinkedBlockingQueue<SolidTile>();
	}

	public void add(int x, int y, boolean solid) {
		changeQueue.add(new SolidTile(x, y, solid));
	}

	public void processUpdates(TileCollisionMap collisionMap) {
		// iterate through the change queue and make changes where needed
		while(!changeQueue.isEmpty()) {
			SolidTile pTile = changeQueue.poll();
			// change from non-solid to solid?
			if(pTile.solid == true) {
				if(collisionMap.isTileExist(pTile.x, pTile.y)) {
					throw new IllegalStateException(
							"Cannot add solid tile where solid tile already exists in collision map.");
				}
				collisionMap.addTile(pTile.x, pTile.y);
			}
			// change from to solid non-solid
			else {
				if(!collisionMap.isTileExist(pTile.x, pTile.y)) {
					throw new IllegalStateException(
							"Cannot remove solid tile where solid tile does not already exist in collision map.");
				}
				collisionMap.removeTile(pTile.x, pTile.y);
			}
		}
	}
}
