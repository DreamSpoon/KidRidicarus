package kidridicarus.tiles;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class BooleanTileMap {
	private int width;
	private int height;
	private boolean cells[][];

	// initializes cells to false
	public BooleanTileMap(int w, int h) {
		width = w;
		height = h;
		cells = new boolean[w][h];
	}

	// initializes cells to true or false depending on the cells of the inMap
	public BooleanTileMap(TiledMapTileLayer inMap) {
		width = inMap.getWidth();
		height = inMap.getHeight();
		cells = new boolean[width][height];
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				if(inMap.getCell(x, y) != null && inMap.getCell(x, y).getTile() != null)
					cells[x][y] = true;
			}
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean getCell(int x, int y) {
		return cells[x][y];
	}

	public void setCell(int x, int y, boolean b) {
		cells[x][y] = b;
	}

	// A "safe" tile getter that will cope gracefully with out of bounds cell requests.
	// Returns false if x or y are out of bounds,
	// Returns false if the cell is null (empty),
	// Otherwise, returns true because there is a tile at (x, y).
	public boolean gracefulGetCell(int x, int y) {
		if(x < 0 || y < 0 || x >= width || y >= height)
			return false;
		return cells[x][y];
	}
}
