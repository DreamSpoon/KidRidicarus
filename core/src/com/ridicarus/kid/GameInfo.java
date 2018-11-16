package com.ridicarus.kid;

public class GameInfo {
	public static final float PlAYER_STARTX = 32;
	public static final float PlAYER_STARTY = 64;

	public static final int V_WIDTH = 400;
//	public static final int V_HEIGHT = 208;
	public static final int V_HEIGHT = 240;
	public static final int DESKTOP_SCALE = 2;

	public static final float PPM = 100f;

//	public static final int TURTLE_KICK_LEFT_SPEED = -2;
//	public static final int TURTLE_KICK_RIGHT_SPEED = 2;
//	public static final float TURTLE_FLYDIE_TIME = 5f;

//	public static final String GAMEMAP_NAME = "level1-x v1.tmx";
//	public static final String GAMEMAP_NAME = "testCollisionLayers v1.tmx";
	public static final String GAMEMAP_NAME = "level xyz v6.tmx";

	public static final String TILESET_GUTTER = "tileset_gutter";
	public static final String TILEMAP_FOREGROUND = "foreground";
	public static final String TILEMAP_COLLISION = "collision";
	public static final String TILEMAP_BRICK = "brick";
	public static final String TILEMAP_GOOMBA = "goomba";
	public static final String TILEMAP_COINBLOCK = "coinblock";
	public static final String TILEMAP_TURTLE = "turtle";

	public static final String MUSHROOM_TILEKEY = "mushroom";
	public static final String COIN_TILEKEY = "coin";
	public static final String ANIM_QMARK_TILEKEY = "anim_qmark";

	public static final int TILEPIX_X = 16;
	public static final int TILEPIX_Y = 16;

	public static final short NOTHING_BIT		= 0;
	public static final short BOUNDARY_BIT		= 2 << 0;
	public static final short MARIO_BIT			= 2 << 1;
	public static final short MARIOHEAD_BIT		= 2 << 2;
	public static final short MARIOFOOT_BIT		= 2 << 3;
	public static final short DESTROYED_BIT		= 2 << 4;
	public static final short BANGABLE_BIT		= 2 << 6;
	public static final short ROBOT_BIT			= 2 << 7;
	public static final short ROBOTFOOT_BIT		= 2 << 8;
	public static final short ITEM_BIT			= 2 << 9;
	public static final short MARIO_ROBOT_SENSOR_BIT	= 2 << 10;

	public static final String TEXATLAS_FILENAME = "Mario_and_Enemies6.pack";
	public static final String TEXATLAS_GOOMBA = "goomba";
	public static final String TEXATLAS_MUSHROOM = "mushroom";
	public static final String TEXATLAS_BIGMARIO = "big_mario";
	public static final String TEXATLAS_LITTLEMARIO = "little_mario";
	public static final String TEXATLAS_TURTLE = "turtle";
	public static final String TEXATLAS_COIN_SPIN = "coin_bounce";
	public static final String TEXATLAS_BRICKPIECES = "brick_pieces";
	public static final String TEXATLAS_FIREFLOWER = "fireflower";
	public static final String TEXATLAS_FIREMARIO = "fire_mario";
	public static final String TEXATLAS_FIREBALL = "fireball";
	public static final String TEXATLAS_FIREBALL_EXP = "fireball_explode";

	public static final String MUSIC_MARIO = "audio/music/mario_music.ogg";
	public static final String SOUND_BREAK = "audio/sounds/breakblock.wav";
	public static final String SOUND_BUMP = "audio/sounds/bump.wav";
	public static final String SOUND_COIN = "audio/sounds/coin.wav";
	public static final String SOUND_POWERUP_SPAWN = "audio/sounds/powerup_spawn.wav";
	public static final String SOUND_POWERUP_USE = "audio/sounds/powerup.wav";
	public static final String SOUND_POWERDOWN = "audio/sounds/powerdown.wav";
	public static final String SOUND_STOMP = "audio/sounds/stomp.wav";
	public static final String SOUND_MARIODIE = "audio/sounds/mariodie.wav";

	public static final float MUSIC_VOLUME = 0.1f;
	public static final float SOUND_VOLUME = 0.15f;

	public static final float MARIO_DEAD_TIME = 5f;
	public static final float MAX_FLOAT_HACK = 1e38f;

	public static float P2M(float p) {
		return p / PPM;
	}

	public static float M2P(float x) {
		return x * PPM;
	}
}
