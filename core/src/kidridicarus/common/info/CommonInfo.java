package kidridicarus.common.info;

import kidridicarus.agency.AgentClassList;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.common.agent.general.AgentSpawnTrigger;
import kidridicarus.common.agent.general.AgentSpawner;
import kidridicarus.common.agent.general.DespawnBox;
import kidridicarus.common.agent.general.GuideSpawner;
import kidridicarus.common.agent.general.Room;
import kidridicarus.common.agent.general.WarpPipe;

public class CommonInfo {
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

	public static final AgentClassList CORE_AGENT_CLASS_LIST = new AgentClassList( 
			AgencyKV.Spawn.VAL_AGENTSPAWNER, AgentSpawner.class,
			AgencyKV.Spawn.VAL_AGENTSPAWN_TRIGGER, AgentSpawnTrigger.class,
			AgencyKV.Spawn.VAL_DESPAWN, DespawnBox.class,
			AgencyKV.Spawn.VAL_PIPEWARP, WarpPipe.class,
			AgencyKV.Spawn.VAL_SPAWNGUIDE, GuideSpawner.class,
			AgencyKV.Room.VAL_ROOM, Room.class);
}
