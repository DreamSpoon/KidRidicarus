package kidridicarus.info;

public class AudioInfo {
	public static final float MUSIC_VOLUME = 0.2f;
	public static final float SOUND_VOLUME = 0.6f;

	public class Music {
		public class SMB {
			private static final String DIR = "audio/music/SMB/";
			public static final String STARPOWER = DIR+"04_-_Super_Mario_Bros._-_NES_-_Invincible_BGM.ogg";
			public static final String LEVELEND = DIR+"02_-_Super_Mario_Bros._-_NES_-_Course_Clear_Fanfare.ogg";
		}

		public class Metroid {
			private static final String DIR = "audio/music/Metroid/";
			public static final String METROIDITEM = DIR+"08_-_Metroid_-_NES_-_Get_Item_Jingle.ogg";
		}
	}

	public class Sound {
		public class SMB {
			private static final String DIR = "audio/sound/SMB/";
			public static final String BREAK = DIR+"Break.wav";
			public static final String BUMP = DIR+"Bump.wav";
			public static final String COIN = DIR+"Coin.wav";
			public static final String POWERUP_SPAWN = DIR+"Item.wav";
			public static final String POWERUP_USE = DIR+"Powerup.wav";
			public static final String POWERDOWN = DIR+"Warp.wav";
			public static final String STOMP = DIR+"Squish.wav";
			public static final String MARIO_DIE = DIR+"Die.wav";
			public static final String MARIO_SMLJUMP = DIR+"Jump.wav";
			public static final String MARIO_BIGJUMP = DIR+"Big Jump.ogg";
			public static final String KICK = DIR+"Kick.wav";
			public static final String FIREBALL = DIR+"Fire Ball.wav";
			public static final String FLAGPOLE = DIR+"Flagpole.wav";
			public static final String UP1 = DIR+"1up.wav";
		}
		public class Metroid {
			private static final String DIR = "audio/sound/Metroid/";
			public static final String STEP = DIR+"step.wav";
			public static final String JUMP = DIR+"jump.wav";
			public static final String HURT = DIR+"hurt.wav";
			public static final String SHOOT = DIR+"shoot.wav";
		}
	}
}
