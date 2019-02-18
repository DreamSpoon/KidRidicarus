package kidridicarus.agency.helper;

/*
 * An element in a queue for updating the "solid" property of a tile in a collision map.
 */
public class SolidTile {
	int x;
	int y;
	// solid = true indicates the tile specified by (x, y) must be made solid
	boolean solid;

	public SolidTile(int x, int y, boolean solid) {
		this.x = x;
		this.y = y;
		this.solid = solid;
	}
}
