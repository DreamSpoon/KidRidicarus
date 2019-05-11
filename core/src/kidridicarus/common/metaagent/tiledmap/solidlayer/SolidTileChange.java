package kidridicarus.common.metaagent.tiledmap.solidlayer;

/*
 * Change the "solid" state of a tile in a solid map.
 */
class SolidTileChange {
	int x;
	int y;
	// solid = true indicates the tile specified by (x, y) must be made solid
	boolean solid;

	SolidTileChange(int x, int y, boolean solid) {
		this.x = x;
		this.y = y;
		this.solid = solid;
	}
}
