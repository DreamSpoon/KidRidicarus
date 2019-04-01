package kidridicarus.game.info;

public class KidIcarusGfx {
	private static final String BASEDIR = "KidIcarus/";

	public class General {
		private static final String DIR = BASEDIR+"general/";
		public static final String SMALL_POOF =	DIR+"small_poof";
	}

	public class Item {
		private static final String DIR = BASEDIR+"item/";
		public static final String HEART1 =	DIR+"heart1";
	}

	public class NPC {
		private static final String DIR = BASEDIR+"NPC/";
		public static final String SHEMUM =	DIR+"shemum";
	}

	public class Player {
		private static final String DIR = BASEDIR+"player/";
		public class Pit {
			private static final String SUBDIR = DIR+"Pit/";
			public static final String AIMUP =			SUBDIR+"pit_aimup";
			public static final String AIMUP_SHOOT =	SUBDIR+"pit_aimup_shoot";
			public static final String CLIMB =			SUBDIR+"pit_climb";
			public static final String DEAD =			SUBDIR+"pit_dead";
			public static final String DUCK =			SUBDIR+"pit_duck";
			public static final String JUMP =			SUBDIR+"pit_jump";
			public static final String JUMP_SHOOT =		SUBDIR+"pit_jump_shoot";
			public static final String STAND =			SUBDIR+"pit_stand";
			public static final String STAND_SHOOT =	SUBDIR+"pit_stand_shoot";
			public static final String WALK =			SUBDIR+"pit_walk";
			public static final String WALK_SHOOT =		SUBDIR+"pit_walk_shoot";
		}
		public class PitArrow {
			private static final String SUBDIR = DIR+"Pit/";
			public static final String ARROW =			SUBDIR+"pit_arrow";
		}
	}
}
