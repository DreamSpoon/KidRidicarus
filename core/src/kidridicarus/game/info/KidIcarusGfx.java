package kidridicarus.game.info;

public class KidIcarusGfx {
	private static final String BASEDIR = "KidIcarus/";

	public static class General {
		private static final String DIR = BASEDIR+"general/";
		public static final String SMALL_POOF =	DIR+"small_poof";
		public static final String BIG_POOF =	DIR+"big_poof";
		public static final String DOOR_BROWN_CLOSED =	DIR+"door_brown_closed";
		public static final String DOOR_BROWN_OPENED =	DIR+"door_brown_opened";
	}

	public static class Item {
		private static final String DIR = BASEDIR+"item/";
		public static final String HEART1 =	DIR+"heart1";
		public static final String HEART5 =	DIR+"heart5";
		public static final String HEART10 =	DIR+"heart10";
		public static final String CHALICE =	DIR+"chalice";
	}

	public static class NPC {
		private static final String DIR = BASEDIR+"NPC/";
		public static final String SHEMUM =	DIR+"shemum";
		public static final String MONOEYE = DIR+"monoeye";
	}

	public static class Player {
		private static final String DIR = BASEDIR+"player/";
		public static class Pit {
			private static final String SUBDIR = DIR+"Pit/";
			public static final String[] GRPDIR = { SUBDIR+"reg/", SUBDIR+"inv1/", SUBDIR+"inv2/", SUBDIR+"inv3/" };
			public static final String AIMUP =		"pit_aimup";
			public static final String AIMUP_SHOOT ="pit_aimup_shoot";
			public static final String CLIMB =		"pit_climb";
			public static final String DEAD =		"pit_dead";
			public static final String DUCK =		"pit_duck";
			public static final String JUMP =		"pit_jump";
			public static final String JUMP_SHOOT =	"pit_jump_shoot";
			public static final String STAND =		"pit_stand";
			public static final String STAND_SHOOT ="pit_stand_shoot";
			public static final String WALK =		"pit_walk";
			public static final String WALK_SHOOT =	"pit_walk_shoot";
		}
		public static class PitArrow {
			private static final String SUBDIR = DIR+"Pit/";
			public static final String ARROW = SUBDIR+"pit_arrow";
		}

		public static class HUD {
			private static final String SUBDIR = DIR+"PitHUD/";
			public static final String[] HEALTH_BAR = { SUBDIR+"health_bar0", SUBDIR+"health_bar1",
					SUBDIR+"health_bar2", SUBDIR+"health_bar3", SUBDIR+"health_bar4", SUBDIR+"health_bar5",
					SUBDIR+"health_bar6", SUBDIR+"health_bar7" };
		}
	}
}
