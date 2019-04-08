package kidridicarus.agency.info;

/*
 * Agency Key Value Info
 */
public class AgencyKV {
	public class Spawn {
		public static final String KEY_AGENT_CLASS = "agent_class";

		public static final String KEY_START_POS = "start_position";
		public static final String KEY_START_BOUNDS = "start_bounds";
		// used by tile agent constructors (e.g. breakable brick tile blocks)
		public static final String KEY_START_TEXREGION = "start_tex_region";
		public static final String KEY_START_VEL = "start_velocity";
		public static final String KEY_START_PARENT_AGENT = "start_parent_agent";
	}
}
