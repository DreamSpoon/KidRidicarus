package kidridicarus.game.info;

public class MetroidGfx {
	public class NPC {
		private static final String DIR = "NPC/metroid/";
		public static final String DEATH_POP =	DIR+"explode_big";
		public static final String DOOR_CLOSED =	DIR+"door_closed";
		public static final String DOOR_CLOSING =	DIR+"door_closing";
		public static final String DOOR_OPENED =	DIR+"door_opened";
		public static final String DOOR_OPENING =	DIR+"door_opening";
		public static final String RIO =		DIR+"rio";
		public static final String RIO_HIT =	DIR+"rio_hit";
		public static final String SKREE =		DIR+"skree";
		public static final String SKREE_EXP =	DIR+"skree_exp";
		public static final String SKREE_HIT =	DIR+"skree_hit";
		public static final String ZOOMER =		DIR+"zoomer_orange";
		public static final String ZOOMER_HIT =	DIR+"zoomer_hit";
	}

	public class Item {
		private static final String DIR = "item/metroid/";
		public static final String ENERGY = DIR+"energy";
		public static final String MARUMARI = DIR+"marumari";
	}

	public class Player {
		private static final String DIR = "player/metroid/";
		public static final String AIMRIGHT = DIR+"samus_aim_right";
		public static final String AIMUP = DIR+"samus_aim_up";
		public static final String BALL = DIR+"samus_ball";
		public static final String CLIMB = DIR+"samus_climb";
		public static final String JUMP = DIR+"samus_jump";
		public static final String JUMP_AIMRIGHT = DIR+"samus_jump_aimright";
		public static final String JUMP_AIMUP = DIR+"samus_jump_aimup";
		public static final String JUMPSPIN = DIR+"samus_jumpspin";
		public static final String RUN = DIR+"samus_run";
		public static final String RUN_AIMRIGHT = DIR+"samus_run_aimright";
		public static final String RUN_AIMUP = DIR+"samus_run_aimup";
		public static final String SHOT = DIR+"shot_regular";
		public static final String SHOT_EXP = DIR+"shot_explode";

		public class Dead {
			private static final String SUBDIR = DIR + "dead/";

			public static final String BOT_LEFT = SUBDIR + "bot_left";
			public static final String MID_LEFT = SUBDIR + "mid_left";
			public static final String TOP_LEFT = SUBDIR + "top_left";
			public static final String BOT_RIGHT = SUBDIR + "bot_right";
			public static final String MID_RIGHT = SUBDIR + "mid_right";
			public static final String TOP_RIGHT = SUBDIR + "top_right";
		}
	}

	public class HUD {
		private static final String DIR = "player/metroid/HUD/";

		public static final String ENERGY_TEXT = DIR+"energy_text";
		public static final String ENERGY_TANK_FULL = DIR+"energy_tank_full";
		public static final String ENERGY_TANK_EMPTY = DIR+"energy_tank_empty";
	}
}
