package kidridicarus.agency.collisionmap;

import java.util.Collection;
import java.util.Iterator;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class BooleanTileMap {
	private int width;
	private int height;
	private boolean[][] cells;

	/*
	 * Initializes cells to true or false depending on the cells of the inMap(s).
	 * Process:
	 *   -init all cells to false
	 *   -change a cell to true when solid tile is found in any of the input layers
	 */
	public BooleanTileMap(Collection<TiledMapTileLayer> layers) {
		if(layers == null || layers.isEmpty())
			throw new IllegalArgumentException("Layers array was null or it referenced a null layer in the zeroth position.");
		// get width and height from the first layer in the collection 
		TiledMapTileLayer a = layers.iterator().next();
		width = a.getWidth();
		height = a.getHeight();
		cells = new boolean[width][height];	//init all cells to false
		// check all layers and switch cells to true as needed
		Iterator<TiledMapTileLayer> layersIter = layers.iterator();
		while(layersIter.hasNext()) {
			TiledMapTileLayer layer = layersIter.next();
			for(int y=0; y<height; y++)
				for(int x=0; x<width; x++)
					if(layer.getCell(x, y) != null && layer.getCell(x, y).getTile() != null)
						cells[x][y] = true;
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

	/*
	 * A "safe" tile getter that will cope gracefully with out of bounds cell requests.
	 * Returns false if x or y are out of bounds,
	 * Returns false if the cell is null (empty),
	 * Otherwise, returns true because there is a tile at (x, y).
	 */
	public boolean gracefulGetCell(int x, int y) {
		if(x < 0 || y < 0 || x >= width || y >= height)
			return false;
		return cells[x][y];
	}
}
