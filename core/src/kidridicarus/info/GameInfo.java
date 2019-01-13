package kidridicarus.info;

public class GameInfo {
	public static final int V_WIDTH = 400;
	public static final int V_HEIGHT = 240;
	// DEBUG: used to quickly change size of screen on desktop without affecting aspect ratio
	public static final int DESKTOP_SCALE = 2;

	public static final String GAMEMAP_FILENAME1 = "map/SMB1-1 v4.tmx";
	public static final String GAMEMAP_FILENAME2 = "map/Metroid1-1 v4.tmx";

	public static final String TA_MAIN_FILENAME = "sprite/KidRid10.pack";

	public static final String SMB1_FONT = "font/prstart.fnt";

	/*
	 * Draw order explained:
	 * 0) Screen is cleared
	 * 1) Tile background (usually one color, e.g. black) is drawn.
	 * 1) BOTTOM sprites are drawn.
	 * 2) Background scenery tiles are drawn.
	 * 3) MIDDLE sprites are drawn.
	 * 4) Foreground scenery tiles are drawn.
	 * 5) TOP sprites are drawn.
	 * 
	 * If order is NONE then layer is not drawn.
	 * Usually, the player sprite is drawn TOP order, turtles and goombas are drawn MIDDLE order.
	 */
	public enum SpriteDrawOrder { NONE, BOTTOM, MIDDLE, TOP };

	public enum LayerDrawOrder { NONE, BOTTOM, MIDDLE, TOP };

	public enum Direction4 {
		RIGHT, UP, LEFT, DOWN;

		public boolean isHorizontal() {
			return this.equals(RIGHT) || this.equals(LEFT);
		}

		public boolean isVertical() {
			return this.equals(UP) || this.equals(DOWN);
		}

		// rotate 90 degrees counterclockwise
		public Direction4 rotate90() {
			switch(this) {
				case RIGHT:
					return Direction4.UP;
				case UP:
					return Direction4.LEFT;
				case LEFT:
					return Direction4.DOWN;
				default:
					return Direction4.RIGHT;
			}
		}
		// rotate 270 degrees counterclockwise (90 degrees clockwise)
		public Direction4 rotate270() {
			switch(this) {
				case RIGHT:
					return Direction4.DOWN;
				case DOWN:
					return Direction4.LEFT;
				case LEFT:
					return Direction4.UP;
				default:
					return Direction4.RIGHT;
			}
		}
	};

	public enum DiagonalDir4 { TOPRIGHT, TOPLEFT, BOTTOMLEFT, BOTTOMRIGHT }

	// deleted code, maybe useful?
	/*
	 * Source of code:
	 * https://stackoverflow.com/questions/6821810/determine-if-two-java-objects-are-of-the-same-class
	 */
//	private <T, U> boolean isObjClasses(Object obj1, Object obj2, Class<T> class1, Class<U> class2) {
//		return (class1.isAssignableFrom(obj1.getClass()) && class2.isAssignableFrom(obj2.getClass())) ||
//				(class1.isAssignableFrom(obj2.getClass()) && class2.isAssignableFrom(obj1.getClass()));
//	}
}
