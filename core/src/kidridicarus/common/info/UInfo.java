package kidridicarus.common.info;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/*
 * Title: Unit Info
 * Desc: Unit conversion constants and methods.
 * Supported metrics:
 *   -pixel
 *   -meter
 *   -tile
 *   -sub-tile (fraction of tile along horizontal/vertical axis, i.e. 0 to 1 inclusive)
 */
public class UInfo {
	public static final float PPM = 100f;
	public static final int TILEPIX_X = 16;
	public static final int TILEPIX_Y = 16;
	// TODO verify / formulate explanation for reasonableness of these two epsilon values (e.g. should they
	// vary by game / gametype?)
	public static final float VEL_EPSILON = 0.0001f;
	public static final float POS_EPSILON = 0.001f;

	/*
	 * Input unit: Pixels
	 * Output unit: Meters
	 */
	public static float P2M(float p) {
		return p / PPM;
	}

	/*
	 * Input unit: Meters
	 * Output unit: Pixels
	 */
	public static float M2P(float x) {
		return x * PPM;
	}

	/*
	 * Input unit: Pixels
	 * Output unit: Meters
	 */
	public static Vector2 VectorP2M(float x, float y) {
		return new Vector2(P2M(x), P2M(y));
	}

	/*
	 * Input unit: Pixels
	 * Output unit: Meters
	 */
	public static Rectangle RectangleP2M(Rectangle rectangle) {
		return new Rectangle(P2M(rectangle.x), P2M(rectangle.y), P2M(rectangle.width), P2M(rectangle.height));
	}

	/*
	 * Get rectangle bounds of tile at (x, y), in meters.
	 * Input unit: Tile coordinates
	 * Output unit: Meters
	 */
	public static Rectangle RectangleT2M(int x, int y) {
		return RectangleP2M(new Rectangle(x*TILEPIX_X, y*TILEPIX_Y, TILEPIX_X, TILEPIX_Y));
	}

	/*
	 * Input unit: Meters
	 * Output unit: Tile coordinates
	 */
	public static Vector2 VectorM2T(Vector2 position) {
		return new Vector2((int) (M2P(position.x) / TILEPIX_X), (int) (M2P(position.y) / TILEPIX_Y));
	}

	/*
	 * Get center of tile given by (tileX, tileY) .
	 * Input unit: Tile coordinates
	 * Output unit: Meters
	 */
	public static Vector2 VectorT2M(int tileX, int tileY) {
		return VectorP2M(tileX*TILEPIX_X + TILEPIX_X/2f, tileY*TILEPIX_Y + TILEPIX_Y/2f);
	}

	/*
	 * Returns the sub-tile position within the tile for each axis, as a ratio of the tile size on each axis.
	 * Input unit: Meters
	 * Output unit: Sub-tile
	 * e.g. if TILEPIX_X = 16 and positionX = 19, then returnsX 3/16=0.1875
	 * e.g. if TILEPIX_Y = 16 and positionY = 42, then returnsY 10/16=0.625
	 */
	public static Vector2 VectorM2SubT(Vector2 position) {
		float tileW = P2M(TILEPIX_X);
		float tileH = P2M(TILEPIX_Y);
		// mod position coordinate with tile size
		float x = (int) (position.x / tileW);
		float y = (int) (position.y / tileH);
		// subtract modded position and divide by tile size
		return new Vector2((position.x - x * tileW) / tileW, (position.y - y * tileH) / tileH);
	}

	/*
	 * Input unit: Meters
	 * Output unit: Tile coordinates
	 * I left the code in "long form" because I was confused when I changed it to short form (the cast to int
	 * makes it difficult to combine the +0.5f and -0.5f .
	 * Note: This is a wonky tile bounds conversion - it offsets the bounds inwards to get the tile coordinates.
	 *   But it is good for converting rectangles drawn on a map to tiles for agent spawner purposes.  
	 */
	public static Rectangle RectangleM2T(Rectangle bounds) {
		int bottomX;
		int topX;
		// if the horizontal bounds are less than or equal one tile wide then use center of bounds
		if(M2P(bounds.width) <= TILEPIX_X) {
			bottomX = (int) (M2P(bounds.x+bounds.width/2f) / TILEPIX_X);
			topX = bottomX;
		}
		else {
			bottomX = (int) (M2P(bounds.x) / TILEPIX_X + 0.5f);
			topX = (int) (M2P(bounds.x + bounds.width) / TILEPIX_X - 0.5f);
		}
		int bottomY;
		int topY;
		// if the vertical bounds are less than or equal one tile wide then use center of bounds
		if(M2P(bounds.height) <= TILEPIX_Y) {
			bottomY = (int) (M2P(bounds.y+bounds.height/2f) / TILEPIX_Y);
			topY = bottomY;
		}
		else {
			bottomY = (int) (M2P(bounds.y) / TILEPIX_Y + 0.5f);
			topY = (int) (M2P(bounds.y + bounds.height) / TILEPIX_Y - 0.5f);
		}
		// The width and height get +1 because the min bounds are 1 tile wide and 1 tile high.
		// e.g. A single point occupies exactly one tile.
		return new Rectangle(bottomX, bottomY, topX - bottomX + 1, topY - bottomY + 1);
	}

	public static boolean epsCheck(float input, float target, float epsilon) {
		return input >= target-epsilon && input <= target+epsilon;
	}
}
