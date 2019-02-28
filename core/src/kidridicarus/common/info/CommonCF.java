package kidridicarus.common.info;

import kidridicarus.agency.contact.CFBitSeq;

public class CommonCF {
	// Contact Filter Bit list (for collision detection)
	public class Alias {
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

	public static final CFBitSeq NO_CONTACT_CFCAT = new CFBitSeq();
	public static final CFBitSeq NO_CONTACT_CFMASK = new CFBitSeq();

	public static final CFBitSeq AGENT_SENSOR_CFCAT = new CFBitSeq(Alias.AGENT_BIT);
	public static final CFBitSeq AGENT_SENSOR_CFMASK = new CFBitSeq(Alias.AGENT_BIT);

	public static final CFBitSeq SOLID_BODY_CFCAT = new CFBitSeq(Alias.AGENT_BIT);
	public static final CFBitSeq SOLID_BODY_CFMASK = new CFBitSeq(Alias.SOLID_BOUND_BIT);

	public static final CFBitSeq GROUND_SENSOR_CFCAT = new CFBitSeq(Alias.AGENT_BIT);
	public static final CFBitSeq GROUND_SENSOR_CFMASK = new CFBitSeq(Alias.SOLID_BOUND_BIT);

	public static final CFBitSeq SOLID_ITEM_CFCAT = new CFBitSeq(CommonCF.Alias.ITEM_BIT);
	public static final CFBitSeq SOLID_ITEM_CFMASK = new CFBitSeq(CommonCF.Alias.SOLID_BOUND_BIT,
			CommonCF.Alias.AGENT_BIT);

	public static final CFBitSeq BUMPTILE_CFCAT = new CFBitSeq(CommonCF.Alias.BUMPABLE_BIT);
	public static final CFBitSeq BUMPTILE_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT);

	public static final CFBitSeq WARPPIPE_CFCAT = new CFBitSeq(CommonCF.Alias.PIPE_BIT);
	public static final CFBitSeq WARPPIPE_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
}
