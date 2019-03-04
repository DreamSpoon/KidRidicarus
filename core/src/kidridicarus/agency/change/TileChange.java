package kidridicarus.agency.change;

/*
 * Change the "solid" state of a tile in a collision map.
 */
public class TileChange {
	public int x;
	public int y;
	// solid = true indicates the tile specified by (x, y) must be made solid
	public boolean solid;

	public TileChange(int x, int y, boolean solid) {
		this.x = x;
		this.y = y;
		this.solid = solid;
	}
}
