package kidridicarus.common.info;

import kidridicarus.agency.AgentClassList;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.common.agent.general.AgentSpawnTrigger;
import kidridicarus.common.agent.general.AgentSpawner;
import kidridicarus.common.agent.general.DespawnBox;
import kidridicarus.common.agent.general.PlayerSpawner;
import kidridicarus.common.agent.general.Room;
import kidridicarus.common.agent.general.PipeWarp;

public class CommonInfo {
	public static final AgentClassList CORE_AGENT_CLASS_LIST = new AgentClassList( 
			AgencyKV.Spawn.VAL_AGENTSPAWNER, AgentSpawner.class,
			AgencyKV.Spawn.VAL_AGENTSPAWN_TRIGGER, AgentSpawnTrigger.class,
			AgencyKV.Spawn.VAL_DESPAWN, DespawnBox.class,
			AgencyKV.Spawn.VAL_PIPEWARP_SPAWN, PipeWarp.class,
			AgencyKV.Spawn.VAL_PLAYERSPAWNER, PlayerSpawner.class,
			AgencyKV.Room.VAL_ROOM, Room.class);
}
