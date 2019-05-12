package kidridicarus.game.KidIcarus;

public class KidIcarusAudio {
	public class Sound {
		private static final String DIR = "KidIcarus/sound/";
		public class General {
			public static final String HEART_PICKUP = DIR+"heart_pickup.wav";
			public static final String SMALL_POOF = DIR+"small_poof.wav";
		}
		public class Pit {
			public static final String HURT = DIR+"pit_hurt.ogg";
			public static final String JUMP = DIR+"pit_jump.wav";
			public static final String SHOOT = DIR+"pit_shoot.wav";
		}
	}

	public class Music {
		private static final String DIR = "KidIcarus/music/";
		public static final String PIT_DIE = DIR+"11_-_Kid_Icarus_-_NES_-_Game_Over.ogg";
	}
}
