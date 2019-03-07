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
import kidridicarus.common.tool.AllowOrder;
import kidridicarus.game.SMB.agent.other.LevelEndTrigger;
import kidridicarus.common.agent.general.PipeWarp;

public class CommonInfo {
	public static final AgentClassList CORE_AGENT_CLASS_LIST = new AgentClassList( 
			AgencyKV.Spawn.VAL_AGENTSPAWNER, AgentSpawner.class,
			AgencyKV.Spawn.VAL_AGENTSPAWN_TRIGGER, AgentSpawnTrigger.class,
			AgencyKV.Spawn.VAL_DESPAWN, DespawnBox.class,
			AgencyKV.Spawn.VAL_PIPEWARP_SPAWN, PipeWarp.class,
			AgencyKV.Spawn.VAL_PLAYERSPAWNER, PlayerSpawner.class,
			AgencyKV.Room.VAL_ROOM, Room.class,
			CommonKV.Level.VAL_LEVELEND_TRIGGER, LevelEndTrigger.class);

	public static class AgentUpdateOrder {
		public static final AllowOrder NONE = new AllowOrder(false, 0f);
		// pre-update is earlier than update
		public static final AllowOrder PRE_UPDATE = new AllowOrder(true, -1f);
		// update is earlier than post update
		public static final AllowOrder UPDATE = new AllowOrder(true, 0f);
		// post update is last
		public static final AllowOrder POST_UPDATE = new AllowOrder(true, 1f);
	}
	/*
	 * Returns null if target is not found.
	 */
	public static Agent getTargetAgent(Agency agency, String targetName) {
		if(targetName == null || targetName.equals(""))
			return null;
		return agency.getFirstAgentByProperties(
				new String[] { CommonKV.Script.KEY_NAME }, new String[] { targetName });
	}

	/*
	 * Returns 0 or a positive value.
	 * Used to check that the time passed to animation's getKeyFrame is positive, even when the time is
	 * running backwards.
	 */
	public static float ensurePositive(float original, float delta) {
		if(original >= 0f)
			return original;

		if(delta == 0f)
			return 0f;
		return (float) (original + (-Math.floor(original / delta))*delta);
	}
}
