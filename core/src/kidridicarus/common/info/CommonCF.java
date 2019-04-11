package kidridicarus.common.info;

import kidridicarus.agency.agentcontact.CFBitSeq;

public class CommonCF {
	// Contact Filter Bit list (for contact detection)
	public class Alias {
		public static final String AGENT_BIT = "bit_agent";
		public static final String BUMPABLE_BIT = "bit_bumpable";
		public static final String SOLID_MAP_BIT = "bit_solid_map";
		public static final String DESPAWN_BIT = "bit_despawn";
		public static final String KEEP_ALIVE_BIT = "bit_keep_alive";
		public static final String PIPEWARP_BIT = "bit_pipewarp";
		public static final String POWERUP_BIT = "bit_powerup";
		public static final String ROOM_BIT = "bit_room";
		public static final String SOLID_BOUND_BIT = "bit_solid_bound";
		public static final String SPAWNTRIGGER_BIT = "bit_spawntrigger";
		public static final String SCROLL_PUSH_BIT = "bit_scroll_push";
		public static final String SCROLL_KILL_BIT = "bit_scroll_kill";
		public static final String SEMISOLID_FLOOR_BIT = "bit_semisolid_floor";
		// testing, for use by OnGround sensor
		public static final String SEMISOLID_FLOOR_FOOT_BIT = "bit_semisolid_floor_foot";
	}

	public static final CFBitSeq NO_CONTACT_CFCAT = new CFBitSeq(false);
	public static final CFBitSeq NO_CONTACT_CFMASK = new CFBitSeq(false);

	public static final CFBitSeq AGENT_SENSOR_CFCAT = new CFBitSeq(Alias.AGENT_BIT);
	public static final CFBitSeq AGENT_SENSOR_CFMASK = new CFBitSeq(Alias.AGENT_BIT);

	public static final CFBitSeq SOLID_BODY_CFCAT = new CFBitSeq(Alias.AGENT_BIT);
	public static final CFBitSeq SOLID_BODY_CFMASK = new CFBitSeq(Alias.SOLID_BOUND_BIT);

	public static final CFBitSeq POWERUP_CFCAT = new CFBitSeq(CommonCF.Alias.POWERUP_BIT);
	public static final CFBitSeq POWERUP_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT, CommonCF.Alias.ROOM_BIT);
}
