package com.ridicarus.kid;

public class GameInfo {
	public static final float PPM = 100f;
	public static final int V_WIDTH = 400;
	public static final int V_HEIGHT = 240;
	// DEBUG: used to quickly change size of screen on desktop without affecting aspect ratio
	public static final int DESKTOP_SCALE = 2;

	public static final String GAMEMAP_NAME = "level xyz v8.tmx";

	public static final String TILESET_GUTTER = "tileset_gutter";
	public static final String TILEMAP_BACKGROUND = "background";
	public static final String TILEMAP_SCENERY = "scenery";
	public static final String TILEMAP_COLLISION = "collision";
	public static final String TILEMAP_BUMPABLE = "bumpable";
	public static final String TILEMAP_GOOMBA = "goomba";
	public static final String TILEMAP_TURTLE = "turtle";
	public static final String TILEMAP_SPAWNPOINT = "spawnpoint";
	public static final String TILEMAP_FLAGPOLE = "flagpole";

	public static final String ANIM_QMARK_TILEKEY = "qblock";
	public static final String COIN_TILEKEY = "coin";
	public static final String COIN10_TILEKEY = "coin10";
	public static final String MUSHROOM_TILEKEY = "mushroom";
	public static final String STAR_TILEKEY = "powerstar";

	public static final int TILEPIX_X = 16;
	public static final int TILEPIX_Y = 16;

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

	public static final float MARIO_DEAD_TIME = 5f;
	public static final float MAX_FLOAT_HACK = 1e38f;

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

	public static float P2M(float p) {
		return p / PPM;
	}

	public static float M2P(float x) {
		return x * PPM;
	}
}
