package kidridicarus.common.info;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentClassList;
import kidridicarus.agency.agent.Agent;
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

	/*
	 * Returns null if target is not found.
	 */
	public static Agent getTargetAgent(Agency agency, String targetName) {
		if(targetName == null || targetName.equals(""))
			return null;
		return agency.getFirstAgentByProperties(new String[] { AgencyKV.Spawn.KEY_NAME }, new String[] { targetName });
	}
}
