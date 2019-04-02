package kidridicarus.common.info;

public class CommonKV {
	public static final String VAL_TRUE = "true";
	public static final String VAL_FALSE = "false";

	public static final String KEY_DIRECTION = "direction";
	public static final String VAL_RIGHT = "right";
	public static final String VAL_UP_RIGHT = "up_right";
	public static final String VAL_UP = "up";
	public static final String VAL_UP_LEFT = "up_left";
	public static final String VAL_LEFT = "left";
	public static final String VAL_DOWN_LEFT = "down_left";
	public static final String VAL_DOWN = "down";
	public static final String VAL_DOWN_RIGHT = "down_right";

	public class AgentClassAlias {
		public static final String VAL_AGENTSPAWNER = "agentspawner";
		public static final String VAL_AGENTSPAWN_TRIGGER = "agentspawn_trigger";
		public static final String VAL_DESPAWN = "despawn";
		public static final String VAL_DRAWABLE_TILEMAP = "drawable_tilemap";
		public static final String VAL_LEVELEND_TRIGGER = "levelend_trigger";
		public static final String VAL_ORTHO_SOLID_TILEMAP = "solid_tilemap";
		public static final String VAL_PIPEWARP_SPAWN = "pipewarp";
		public static final String VAL_PLAYERSPAWNER = "playerspawner";
		public static final String VAL_ROOM = "room";
		public static final String VAL_TILEMAP_META = "tilemap_meta";
		public static final String VAL_KEEP_ALIVE_BOX = "keep_alive_box";
		public static final String VAL_SCROLL_PUSH_BOX = "scroll_push_box";
		public static final String VAL_SCROLL_KILL_BOX = "scroll_kill_box";
		public static final String VAL_SEMI_SOLID_FLOOR = "semi_solid_floor";
	}

	public class Spawn {
		public static final String KEY_SPAWNTYPE = "spawntype";
		public static final String KEY_SPAWNMAIN = "spawnmain";
		public static final String KEY_SPAWNAGENTCLASS = "spawnagentclass";
		public static final String KEY_PLAYERAGENTCLASS = "playeragentclass";
		// passed to something that needs to expire immediately
		public static final String KEY_EXPIRE = "expire";
		public static final String KEY_RESPAWN_DEAD = "respawn_dead";
	}

	public class DrawOrder {
		public static final String KEY_DRAWORDER = "draworder";
	}

	public class Layer {
		public static final String KEY_SOLIDLAYER = "solidlayer";
	}

	public class Script {
		public static final String KEY_SPRITESTATE = "spritestate";
		public static final String KEY_SPRITESIZE = "bodysize";
		// name of agent, so agent can be targeted
		public static final String KEY_NAME = "name";
		// name of targeted agent
		public static final String KEY_TARGETNAME = "target_name";
		public static final String KEY_TARGET_LEFT = "target_left";
		public static final String KEY_TARGET_RIGHT = "target_right";
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
		public static final String KEY_HEIGHT = "height";
	}

	public class Room {
		public static final String KEY_ROOMMUSIC = "roommusic";
		public static final String KEY_ROOM_SCROLL_DIR = "scroll_direction";
		public static final String KEY_VIEWOFFSET_Y = "viewoffset_y";
		public static final String KEY_SPACE_WRAP_H = "space_wrap_h";
		public static final String KEY_ROOM_SCROLL_VELOCITY = "scroll_velocity";
		public static final String KEY_SCROLL_BOUND_H = "scroll_bound_h";

		public static final String KEY_ROOMTYPE = "roomtype";
		public static final String VAL_ROOMTYPE_CENTER = "center";
		public static final String VAL_ROOMTYPE_HSCROLL = "hscroll";
		public static final String VAL_ROOMTYPE_VSCROLL = "vscroll";

		public static final String VAL_SCROLL_PUSH_BOX = "scroll_push_box";
		public static final String VAL_SCROLL_KILL_BOX = "scroll_kill_box";
	}

	public class Powerup {
		public static final String KEY_POWERUP_LIST = "powerup_list";
	}
}
