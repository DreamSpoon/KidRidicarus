package kidridicarus.agency.info;

/*
 * Agency Key Value Info
 * TODO: some of this (all of this?) should be moved to GameKV.
 */
public class AgencyKV {
	public static final String VAL_TRUE = "true";
	public static final String VAL_FALSE = "false";

	public static final String KEY_DIRECTION = "direction";
	public static final String VAL_LEFT = "left";
	public static final String VAL_RIGHT = "right";
	public static final String VAL_UP = "up";
	public static final String VAL_DOWN = "down";

	public class Spawn {
		public static final String KEY_AGENTCLASS = "agentclass";
		public static final String KEY_SPAWNTYPE = "spawntype";
		public static final String KEY_SPAWNMAIN = "spawnmain";
		public static final String KEY_SPAWNAGENTCLASS = "spawnagentclass";

		public static final String VAL_AGENTSPAWNER = "agentspawner";
		public static final String VAL_PLAYERSPAWNER = "playerspawner";
		public static final String VAL_DESPAWN = "despawn";
		public static final String VAL_AGENTSPAWN_TRIGGER = "agentspawn_trigger";
		public static final String VAL_PIPEWARP_SPAWN = "pipewarp";

		// spawnpoint needs a name
		public static final String KEY_NAME = "name";
		// warp point needs a spawnpoint name for exit reasons
		public static final String KEY_EXITNAME = "exitname";

		// passed to something that needs to expire immediately
		public static final String KEY_EXPIRE = "expire";

		public static final String KEY_START_POINT = "start_position";
		public static final String KEY_START_BOUNDS = "start_bounds";
		// used by tile agent constructors (e.g. breakable brick tile blocks)
		public static final String KEY_START_TEXREGION = "start_texregion";
		public static final String KEY_START_VELOCITY = "start_velocity";
		public static final String KEY_START_PARENTAGENT = "start_parentagent";
	}

	public class Room {
		public static final String VAL_ROOM = "room";
		public static final String KEY_ROOMMUSIC = "roommusic";
		public static final String KEY_VIEWOFFSET_Y = "viewoffset_y";
		public static final String KEY_ROOMTYPE = "roomtype";
		public static final String VAL_ROOMTYPE_CENTER = "center";
		public static final String VAL_ROOMTYPE_HSCROLL = "hscroll";
	}

	public class DrawOrder {
		public static final String KEY_DRAWORDER = "draworder";
	}

	public class Layer {
		public static final String KEY_SOLIDLAYER = "solidlayer";
	}
}
