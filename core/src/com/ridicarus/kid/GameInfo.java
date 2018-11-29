package com.ridicarus.kid;

public class GameInfo {
	public static final float PPM = 100f;
	public static final int V_WIDTH = 400;
	public static final int V_HEIGHT = 240;
	// DEBUG: used to quickly change size of screen on desktop without affecting aspect ratio
	public static final int DESKTOP_SCALE = 2;

	public static final int TILEPIX_X = 16;
	public static final int TILEPIX_Y = 16;

	public static final String GAMEMAP_NAME = "level xyz v11.tmx";

	public static final String TILESET_GUTTER = "tileset_gutter";
	public static final String TILEMAP_BACKGROUND = "background";
	public static final String TILEMAP_SCENERY = "scenery";
	public static final String TILEMAP_COLLISION = "collision";
	public static final String TILEMAP_BUMPABLE = "bumpable";
	public static final String TILEMAP_GOOMBA = "goomba";
	public static final String TILEMAP_TURTLE = "turtle";
	public static final String TILEMAP_SPAWNPOINT = "spawnpoint";
	public static final String TILEMAP_FLAGPOLE = "flagpole";
	public static final String TILEMAP_LEVELEND = "levelend";
	public static final String TILEMAP_PIPEWARP = "pipewarp";
	public static final String TILEMAP_ROOMS = "rooms";

	public static final String OBJKEY_ANIM_QMARK = "qblock";
	public static final String OBJKEY_COIN = "coin";
	public static final String OBJKEY_COIN10 = "coin10";
	public static final String OBJKEY_MUSHROOM = "mushroom";
	public static final String OBJKEY_STAR = "powerstar";
	public static final String OBJKEY_SPAWNMAIN = "spawnmain";
	public static final String OBJKEY_SPAWNTYPE = "spawntype";
	public static final String OBJVAL_PIPESPAWN = "pipewarp";
	// spawnpoint needs a name
	public static final String OBJKEY_NAME = "name";
	// warp point needs a spawnpoint name for exit reasons
	public static final String OBJKEY_EXITNAME = "exitname";

	public static final String OBJKEY_DIRECTION = "direction";
	public static final String OBJVAL_LEFT = "left";
	public static final String OBJVAL_RIGHT = "right";
	public static final String OBJVAL_UP = "up";
	public static final String OBJVAL_DOWN = "down";
	
	public static final String OBJKEY_ROOMTYPE = "roomtype";
	public static final String OBJVAL_ROOMTYPE_CENTER = "center";
	public static final String OBJVAL_ROOMTYPE_HSCROLL = "hscroll";

	public static final String TEXATLAS_FILENAME = "Mario_and_Enemies8.pack";
	public static final String TEXATLAS_GOOMBA = "goomba";
	public static final String TEXATLAS_MUSHROOM = "mushroom";
	public static final String TEXATLAS_TURTLE = "turtle";
	public static final String TEXATLAS_COIN_SPIN = "coin_bounce";
	public static final String TEXATLAS_BRICKPIECES = "brick_pieces";
	public static final String TEXATLAS_FIREFLOWER = "fireflower";
	public static final String TEXATLAS_FIREBALL = "fireball";
	public static final String TEXATLAS_FIREBALL_EXP = "fireball_explode";
	public static final String TEXATLAS_POWERSTAR = "powerstar";
	public static final String TEXATLAS_FLAG = "flag";

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

	public static final String MUSIC_MARIO = "audio/music/mario_music.ogg";
	public static final String MUSIC_STARPOWER = "audio/music/04_-_Super_Mario_Bros._-_NES_-_Invincible_BGM.ogg";
	public static final String MUSIC_LEVELEND = "audio/music/02_-_Super_Mario_Bros._-_NES_-_Course_Clear_Fanfare.ogg";
	public static final String SOUND_BREAK = "audio/sounds/SMB/Break.wav";
	public static final String SOUND_BUMP = "audio/sounds/SMB/Bump.wav";
	public static final String SOUND_COIN = "audio/sounds/SMB/Coin.wav";
	public static final String SOUND_POWERUP_SPAWN = "audio/sounds/SMB/Item.wav";
	public static final String SOUND_POWERUP_USE = "audio/sounds/SMB/Powerup.wav";
	public static final String SOUND_POWERDOWN = "audio/sounds/SMB/Warp.wav";
	public static final String SOUND_STOMP = "audio/sounds/SMB/Squish.wav";
	public static final String SOUND_MARIODIE = "audio/sounds/SMB/Die.wav";
	public static final String SOUND_MARIOSMLJUMP = "audio/sounds/SMB/Jump.wav";
	public static final String SOUND_MARIOBIGJUMP = "audio/sounds/SMB/Big Jump2.ogg";
	public static final String SOUND_KICK = "audio/sounds/SMB/Kick.wav";
	public static final String SOUND_FIREBALL = "audio/sounds/SMB/Fire Ball.wav";
	public static final String SOUND_FLAGPOLE = "audio/sounds/SMB/Flagpole.wav";

	public static final float MUSIC_VOLUME = 0.1f;
	public static final float SOUND_VOLUME = 0.25f;

	public static final float MAX_FLOAT_HACK = 1e38f;

	public static final short NOTHING_BIT		= 0;
	public static final short BOUNDARY_BIT		= 2 << 0;
	public static final short MARIO_BIT			= 2 << 1;
	public static final short MARIOHEAD_BIT		= 2 << 2;
	public static final short MARIOFOOT_BIT		= 2 << 3;
	public static final short BANGABLE_BIT		= 2 << 4;
	public static final short ROBOT_BIT			= 2 << 5;
	public static final short ROBOTFOOT_BIT		= 2 << 6;
	public static final short ITEM_BIT			= 2 << 7;
	public static final short MARIO_ROBOSENSOR_BIT	= 2 << 8;
	public static final short PIPE_BIT			= 2 << 9;
	public static final short MARIOSIDE_BIT		= 2 << 10;

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

	public enum Direction4 {
		RIGHT, UP, LEFT, DOWN;

		public boolean isHorizontal() {
			return this.equals(RIGHT) || this.equals(LEFT);
		}

		public boolean isVertical() {
			return this.equals(UP) || this.equals(DOWN);
		}
	};

	public static float P2M(float p) {
		return p / PPM;
	}

	public static float M2P(float x) {
		return x * PPM;
	}
}
