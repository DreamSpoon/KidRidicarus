package kidridicarus.info;

/*
 * Title: Key Value Info
 * Desc: Used by the map loading and agent creation code.
 */
public class KVInfo {
	public static final String VAL_TRUE = "true";
	public static final String VAL_FALSE = "false";

	public static final String KEY_DIRECTION = "direction";
	public static final String VAL_LEFT = "left";
	public static final String VAL_RIGHT = "right";
	public static final String VAL_UP = "up";
	public static final String VAL_DOWN = "down";

	public class Layer {
		public static final String KEY_SOLIDLAYER = "solidlayer";

		public static final String KEY_DRAWORDER = "draworder";
		public static final String VAL_BOTTOM = "bottom";
		public static final String VAL_MIDDLE = "middle";
		public static final String VAL_TOP = "top";
		public static final String VAL_NONE = "none";
	}

	public class Sprite {
		public static final String KEY_STARTFRAME = "startframe";
	}

	public class Spawn {
		public static final String KEY_AGENTCLASS = "agentclass";
		public static final String KEY_SPAWNTYPE = "spawntype";
		public static final String KEY_SPAWNMAIN = "spawnmain";
		public static final String KEY_SPAWNAGENTCLASS = "spawnagentclass";

		public static final String VAL_AGENTSPAWNER = "agentspawner";
		public static final String VAL_SPAWNGUIDE = "spawnguide";	// "player" spawner
		public static final String VAL_DESPAWN = "despawn";
		public static final String VAL_AGENTSPAWN_TRIGGER = "agentspawn_trigger";

		// spawnpoint needs a name
		public static final String KEY_NAME = "name";
		// warp point needs a spawnpoint name for exit reasons
		public static final String KEY_EXITNAME = "exitname";

		// passed to something that needs to expire immediately
		public static final String KEY_EXPIRE = "expire";
	}

	public class Room {
		public static final String VAL_ROOM = "room";
		public static final String KEY_ROOMMUSIC = "roommusic";
		public static final String KEY_VIEWOFFSET_Y = "viewoffset_y";
		public static final String KEY_ROOMTYPE = "roomtype";
		public static final String VAL_ROOMTYPE_CENTER = "center";
		public static final String VAL_ROOMTYPE_HSCROLL = "hscroll";
	}

	public class Level {
		public static final String VAL_LEVELEND_TRIGGER = "levelend_trigger";
	}

	public class SMB {
		public static final String VAL_BRICKPIECE = "brickpiece";
		public static final String VAL_BUMPTILE = "bumptile";
		public static final String VAL_CASTLEFLAG = "castleflag";
		public static final String VAL_COIN = "coin";
		public static final String VAL_COIN10 = "coin10";
		public static final String VAL_FIREFLOWER = "fireflower";
		public static final String VAL_FLOATINGPOINTS = "floatingpoints";
		public static final String VAL_GOOMBA = "goomba";
		public static final String VAL_FLAGPOLE = "flagpole";
		public static final String VAL_MARIO = "mario";
		public static final String VAL_MARIOFIREBALL = "mariofireball";
		public static final String VAL_MUSHROOM = "mushroom";
		public static final String VAL_MUSH1UP = "mushroom1up";
		public static final String VAL_PIPEWARP = "pipewarp";
		public static final String VAL_POWERSTAR = "powerstar";
		public static final String VAL_SPINCOIN = "spincoin";
		public static final String VAL_TURTLE = "turtle";

		public static final String KEY_QBLOCK = "qblock";
		// what item spawns when the q block is bumped?
		public static final String KEY_SPAWNITEM = "spawnitem";
		public static final String KEY_SECRETBLOCK = "secretblock";

		public static final String KEY_POINTAMOUNT = "pointamount";
		public static final String KEY_RELPOINTAMOUNT = "relative_pointamount";
		public static final String VAL_POINTS0 = "p0";
		public static final String VAL_POINTS100 = "p100";
		public static final String VAL_POINTS200 = "p200";
		public static final String VAL_POINTS400 = "p400";
		public static final String VAL_POINTS500 = "p500";
		public static final String VAL_POINTS800 = "p800";
		public static final String VAL_POINTS1000 = "p1000";
		public static final String VAL_POINTS2000 = "p2000";
		public static final String VAL_POINTS4000 = "p4000";
		public static final String VAL_POINTS5000 = "p5000";
		public static final String VAL_POINTS8000 = "p8000";
		public static final String VAL_POINTS1UP = "p1up";
	}

	public class Metroid {
		public static final String VAL_ZOOMER = "zoomer";
		public static final String VAL_SKREE = "skree";
		public static final String VAL_SKREE_EXP = "skree_exp";
		public static final String VAL_MARUMARI = "marumari";
		public static final String VAL_SAMUS = "samus";
		public static final String VAL_SAMUS_SHOT = "samus_shot";
	}
}
