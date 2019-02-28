package kidridicarus.game.info;

public class GameInfo {
	public static final String GAMEMAP_FILENAME1 = "map/SMB1-1 v6.tmx";
	public static final String GAMEMAP_FILENAME2 = "map/Metroid1-1 v6.tmx";
	public static final String TA_MAIN_FILENAME = "sprite/KidRid12.pack";
	public static final String SMB1_FONT = "font/prstart.fnt";

	// Contact Filter Bit list (for collision detection)
	public class CFBits {
		public static final String AGENT_BIT = "bit_agent";
		public static final String SOLID_BOUND_BIT = "bit_solid_bound";
		public static final String ROOM_BIT = "bit_room";
		public static final String DESPAWN_BIT = "bit_despawn";
		public static final String SPAWNBOX_BIT = "bit_spawnbox";
		public static final String SPAWNTRIGGER_BIT = "bit_spawntrigger";
		public static final String BUMPABLE_BIT = "bit_bumpable";
		public static final String PIPE_BIT = "bit_pipe";
		public static final String ITEM_BIT = "bit_item";
	}
}
