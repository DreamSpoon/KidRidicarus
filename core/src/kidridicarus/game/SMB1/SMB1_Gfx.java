package kidridicarus.game.SMB1;

public class SMB1_Gfx {
	public static final String SMB1_FONT = "font/prstart.fnt";

	private static final String BASEDIR = "SMB1/";

	public class General {
		private static final String DIR = BASEDIR+"general/";
		public static final String HUD_COIN = 		DIR+"hud_coin";
		public static final String COIN_SPIN =	DIR+"spin_coin";
		public static final String BRICKPIECE =	DIR+"brick_piece";
		public static final String QBLOCK =		DIR+"qblock";
		public static final String QBLOCK_EMPTY =	DIR+"qblock_empty";
		public static final String CASTLEFLAG =	DIR+"castle_flag";
		public static final String DIGITS_1UP =	DIR+"digits_1up";
		public static final String POINTDIGIT0 =	DIR+"digit0";
		public static final String POINTDIGIT1 =	DIR+"digit1";
		public static final String POINTDIGIT2 =	DIR+"digit2";
		public static final String POINTDIGIT4 =	DIR+"digit4";
		public static final String POINTDIGIT5 =	DIR+"digit5";
		public static final String POINTDIGIT8 =	DIR+"digit8";
		public static final String POLEFLAG =	DIR+"poleflag";
	}

	public class Item {
		private static final String DIR = BASEDIR+"item/";
		public static final String UP1_MUSHROOM =	DIR+"up1_mushroom";
		public static final String MAGIC_MUSHROOM =	DIR+"magic_mushroom";
		public static final String FIRE_FLOWER =	DIR+"fire_flower";
		public static final String POWER_STAR =	DIR+"power_star";
		public static final String COIN_STATIC =	DIR+"static_coin";
	}

	public class NPC {
		private static final String DIR = BASEDIR+"NPC/";

		public static final String GOOMBA_WALK =	DIR+"goomba_walk";
		public static final String GOOMBA_SQUISH =	DIR+"goomba_squish";
		public static final String TURTLE_WALK =	DIR+"turtle_walk";
		public static final String TURTLE_HIDE =	DIR+"turtle_hide";
		public static final String TURTLE_WAKEUP =	DIR+"turtle_wake";
	}

	public static class Player {
		private static final String DIR = BASEDIR+"player/";

		public static class Mario {
			private static final String SUBDIR = DIR+"Mario/";

			// group name strings
			public static final String[] GRP_STR = new String[] { "_reg", "_inv1", "_inv2", "_inv3", "_fire" };

			public static final String SML_BRAKE = SUBDIR+"sml/mario_brake";
			public static final String SML_CLIMB = SUBDIR+"sml/mario_climb";
			public static final String SML_JUMP = SUBDIR+"sml/mario_jump";
			public static final String SML_RUN = SUBDIR+"sml/mario_run";
			public static final String SML_STAND = SUBDIR+"sml/mario_stand";

			public static final String SML_DEAD = SUBDIR+"sml/mario_dead_reg";

			public static final String BIG_BRAKE = SUBDIR+"big/mario_brake";
			public static final String BIG_CLIMB = SUBDIR+"big/mario_climb";
			public static final String BIG_JUMP = SUBDIR+"big/mario_jump";
			public static final String BIG_RUN = SUBDIR+"big/mario_run";
			public static final String BIG_STAND = SUBDIR+"big/mario_stand";

			public static final String BIG_DUCK = SUBDIR+"big/mario_duck";
			public static final String BIG_THROW = SUBDIR+"big/mario_throw";
			public static final String BIG_GROW = SUBDIR+"big/mario_grow_reg";
			public static final String BIG_SHRINK = SUBDIR+"big/mario_shrink_reg";
		}

		public class MarioFireball {
			private static final String SUBDIR = DIR+"MarioFireball/";
			public static final String FIREBALL =	SUBDIR+"fireball";
			public static final String FIREBALL_EXP =	SUBDIR+"fireball_explode";
		}
	}
}
