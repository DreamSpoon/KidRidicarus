package kidridicarus.common.info;

public class CommonKV {
	public static final String VAL_TRUE = "true";
	public static final String VAL_FALSE = "false";

	public static final String KEY_DIRECTION = "direction";
	public static final String VAL_LEFT = "left";
	public static final String VAL_RIGHT = "right";
	public static final String VAL_UP = "up";
	public static final String VAL_DOWN = "down";

	public class AgentClassAlias {
		public static final String VAL_AGENTSPAWNER = "agentspawner";
		public static final String VAL_AGENTSPAWN_TRIGGER = "agentspawn_trigger";
		public static final String VAL_DESPAWN = "despawn";
		public static final String VAL_DRAWABLE_TILEMAP = "drawable_tilemap";
		public static final String VAL_LEVELEND_TRIGGER = "levelend_trigger";
		public static final String VAL_ORTHOCOLLISION_TILEMAP = "orthocollision_tilemap";
		public static final String VAL_PIPEWARP_SPAWN = "pipewarp";
		public static final String VAL_PLAYERSPAWNER = "playerspawner";
		public static final String VAL_ROOM = "room";
		public static final String VAL_TILEMAP_META = "tilemap_meta";
	}

	public class Spawn {
		public static final String KEY_SPAWNTYPE = "spawntype";
		public static final String KEY_SPAWNMAIN = "spawnmain";
		public static final String KEY_SPAWNAGENTCLASS = "spawnagentclass";
		// passed to something that needs to expire immediately
		public static final String KEY_EXPIRE = "expire";
	}

	public class DrawOrder {
		public static final String KEY_DRAWORDER = "draworder";
	}

	public class Layer {
		public static final String KEY_SOLIDLAYER = "solidlayer";
	}

	public class Script {
		public static final String KEY_FACINGRIGHT = "facingright";
		public static final String KEY_SPRITESTATE = "spritestate";
		public static final String KEY_SPRITESIZE = "bodysize";
		// name of agent, so agent can be targeted
		public static final String KEY_NAME = "name";
		// name of targeted agent
		public static final String KEY_TARGETNAME = "targetname";
	}

	public class Level {
		public static final String VAL_NEXTLEVEL_NAME = "nextlevel_name";
	}

	public class Sprite {
		public static final String KEY_STARTFRAME = "startframe";
	}

	public class AgentMapParams {
		public static final String KEY_TILEDMAP = "tiled_map";
		public static final String KEY_TILEDMAPTILELAYER_LIST = "tiledmaptilelayer_list";
		public static final String KEY_TILEDMAPTILELAYER = "tiledmaptilelayer";
	}

	public class TiledMap {
		public static final String KEY_WIDTH = "width";
		public static final String KEY_HEIGHT = "width";
	}

	public class Room {
		public static final String KEY_ROOMMUSIC = "roommusic";
		public static final String KEY_VIEWOFFSET_Y = "viewoffset_y";
		public static final String KEY_ROOMTYPE = "roomtype";
		public static final String VAL_ROOMTYPE_CENTER = "center";
		public static final String VAL_ROOMTYPE_HSCROLL = "hscroll";
	}
}
