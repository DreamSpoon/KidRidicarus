package kidridicarus.agency.info;

/*
 * Agency Key Value Info
 */
public class AgencyKV {
	public class Spawn {
		public static final String KEY_AGENTCLASS = "agentclass";

		public static final String KEY_START_POINT = "start_position";
		public static final String KEY_START_BOUNDS = "start_bounds";
		// used by tile agent constructors (e.g. breakable brick tile blocks)
		public static final String KEY_START_TEXREGION = "start_texregion";
		public static final String KEY_START_VELOCITY = "start_velocity";
		public static final String KEY_START_PARENTAGENT = "start_parentagent";
	}
}
