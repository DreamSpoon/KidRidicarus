package kidridicarus.common.info;

public class CommonKV {
	public class AgentClassAlias {
		public static final String VAL_AGENTSPAWNER = "agentspawner";
		public static final String VAL_AGENTSPAWN_TRIGGER = "agentspawn_trigger";
		public static final String VAL_DESPAWN = "despawn";
		public static final String VAL_LEVELEND_TRIGGER = "levelend_trigger";
		public static final String VAL_PIPEWARP_SPAWN = "pipewarp";
		public static final String VAL_PLAYERSPAWNER = "playerspawner";
		public static final String VAL_ROOM = "room";

		public static final String VAL_TILEMAP_META = "tilemap_meta";
		public static final String VAL_ORTHOCOLLISION_TILEMAP = "orthocollision_tilemap";
		public static final String VAL_DRAWABLE_TILEMAP = "drawable_tilemap";
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
}
