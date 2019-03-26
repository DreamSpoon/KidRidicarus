package kidridicarus.common.metaagent.tiledmap.collision;

/*
 * Change the "solid" state of a tile in a collision map.
 */
public class TileChange {
	int x;
	int y;
	// solid = true indicates the tile specified by (x, y) must be made solid
	boolean solid;

	public TileChange(int x, int y, boolean solid) {
		this.x = x;
		this.y = y;
		this.solid = solid;
	}
}
