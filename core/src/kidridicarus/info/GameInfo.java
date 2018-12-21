package kidridicarus.info;

public class GameInfo {
	public static final int V_WIDTH = 400;
	public static final int V_HEIGHT = 240;
	// DEBUG: used to quickly change size of screen on desktop without affecting aspect ratio
	public static final int DESKTOP_SCALE = 2;

	public static final String GAMEMAP_FILENAME1 = "map/SMB1-1 v1.tmx";
	public static final String GAMEMAP_FILENAME2 = "map/Metroid1-1 v1.tmx";
	public static final String TEXATLAS_FILENAME = "sprite/KidRid1.pack";

	public static final String TILESET_GUTTER = "tileset_gutter";

	public static final String TEXATLAS_MUSHROOM = "mushroom";
	public static final String TEXATLAS_FIREFLOWER = "fireflower";
	public static final String TEXATLAS_POWERSTAR = "powerstar";
	public static final String TEXATLAS_MUSH1UP = "mush1up";
	public static final String TEXATLAS_FIREBALL = "fireball";
	public static final String TEXATLAS_FIREBALL_EXP = "fireball_explode";
	public static final String TEXATLAS_COIN_SPIN = "coin_spin";
	public static final String TEXATLAS_COIN_STATIC = "coin_static";
	public static final String TEXATLAS_COIN_HUD = "coin_hud";
	public static final String TEXATLAS_BRICKPIECES = "brick_pieces";
	public static final String TEXATLAS_FLAG = "flag";
	public static final String TEXATLAS_CASTLEFLAG = "castleflag";
	public static final String TEXATLAS_GOOMBA = "goomba";
	public static final String TEXATLAS_TURTLE = "turtle";
	public static final String TEXATLAS_POINTDIGITS = "pointdigits";
	public static final String TEXATLAS_1UPDIGITS = "1up";

	public static final String TEXATLAS_M_ZOOMER= "zoomer";

	public static final String TEXATLAS_SMLMARIO_REG = "little_mario";
	// TODO: little fire mario separate image?
	public static final String TEXATLAS_SMLMARIO_FIRE = TEXATLAS_SMLMARIO_REG;
	public static final String TEXATLAS_SMLMARIO_INV1 = "little_mario_invinc1";
	public static final String TEXATLAS_SMLMARIO_INV2 = "little_mario_invinc2";
	public static final String TEXATLAS_SMLMARIO_INV3 = "little_mario_invinc3";
	public static final String TEXATLAS_BIGMARIO_REG = "big_mario";
	public static final String TEXATLAS_BIGMARIO_FIRE = "fire_mario";
	public static final String TEXATLAS_BIGMARIO_INV1 = "big_mario_invinc1";
	public static final String TEXATLAS_BIGMARIO_INV2 = "big_mario_invinc2";
	public static final String TEXATLAS_BIGMARIO_INV3 = "big_mario_invinc3";

	public static final String SMB_FONT = "font/prstart v1.fnt";

	public static final short NOTHING_BIT		= 0;
	public static final short BOUNDARY_BIT		= 2 << 0;
	public static final short GUIDE_BIT			= 2 << 1;
	public static final short GUIDE_SENSOR_BIT	= 2 << 2;
	public static final short BANGABLE_BIT		= 2 << 3;
	public static final short AGENT_BIT			= 2 << 4;
	public static final short AGENT_SENSOR_BIT	= 2 << 5;
	public static final short ITEM_BIT			= 2 << 6;
	public static final short PIPE_BIT			= 2 << 7;
	public static final short DESPAWN_BIT		= 2 << 8;
	public static final short SPAWNBOX_BIT		= 2 << 9;
	public static final short SPAWNTRIGGER_BIT	= 2 << 10;
	public static final short ROOMBOX_BIT		= 2 << 11;

	// TODO: this is a guess value (0.001f) - test more to refine - may depend upon Pixels Per Meter and Pixels Per Tile
	public static final float BODY_VS_VERT_BOUND_EPSILON = 0.001f;

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
	 * Usually, the player sprite is drawn TOP order, turtles and goombas are drawn MIDDLE order.
	 */
	public enum SpriteDrawOrder { NONE, BOTTOM, MIDDLE, TOP };	// if layer == NONE then don't draw

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

	public enum DiagonalDir4 { TOPRIGHT, TOPLEFT, BOTTOMLEFT, BOTTOMRIGHT };
}
