package kidridicarus.common.info;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/*
 * Title: Unit Info
 * Desc: Unit conversion constants and methods. e.g. pixels to meters and meters to pixels.
 * TODO: Find a better place for TILE_PIX_X and _Y.
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
	public static Vector2 P2MVector(float x, float y) {
		return new Vector2(P2M(x), P2M(y));
	}

	/*
	 * Input unit: Pixels
	 * Output unit: Meters
	 */
	public static Rectangle P2MRect(Rectangle rectangle) {
		return new Rectangle(P2M(rectangle.x), P2M(rectangle.y), P2M(rectangle.width), P2M(rectangle.height));
	}

	/*
	 * Input unit: Meters
	 * Output unit: Pixels
	 */
	public static Vector2 getM2PTileForPos(Vector2 position) {
		return new Vector2((int) (M2P(position.x) / TILEPIX_X),
				(int) (M2P(position.y) / TILEPIX_Y));
	}

	/*
	 * Returns the sub-position within the tile for each axis, as a ratio of the tile size on the axis.
	 * Input unit: Meters
	 * Output unit: Fraction of tile in horizontal and vertical axes, i.e. 0 to 1 on both axes.
	 * e.g. if TILEPIX_X = 16 and positionX = 19, then returnsX 3/16=0.1875
	 * e.g. if TILEPIX_Y = 16 and positionY = 42, then returnsY 10/16=0.625
	 */
	public static Vector2 getSubTileCoordsForMPos(Vector2 position) {
		float tileW = P2M(TILEPIX_X);
		float tileH = P2M(TILEPIX_Y);
		// mod position coordinate with tile size
		float x = (int) (position.x / tileW);
		float y = (int) (position.y / tileH);
		// subtract modded position and divide by tile size
		return new Vector2((position.x - x * tileW) / tileW, (position.y - y * tileH) / tileH);
	}

	/*
	 * Get rectangle bounds of tile at (x, y), in meters.
	 * Input unit: Pixels
	 * Output unit: Meters
	 */
	public static Rectangle getP2MTileRect(int x, int y) {
		return P2MRect(new Rectangle(x*TILEPIX_X, y*TILEPIX_Y, TILEPIX_X, TILEPIX_Y));
	}

	public static boolean epsCheck(float input, float target, float epsilon) {
		return input >= target-epsilon && input <= target+epsilon;
	}
}
