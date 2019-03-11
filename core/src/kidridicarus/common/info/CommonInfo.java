package kidridicarus.common.info;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentClassList;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.tool.AllowOrder;
import kidridicarus.common.agent.collisionmap.DrawLayerAgent;
import kidridicarus.common.agent.collisionmap.OrthoCollisionTiledMapAgent;
import kidridicarus.common.agent.collisionmap.TiledMapMetaAgent;
import kidridicarus.common.agent.general.AgentSpawnTrigger;
import kidridicarus.common.agent.general.AgentSpawner;
import kidridicarus.common.agent.general.DespawnBox;
import kidridicarus.common.agent.general.PlayerSpawner;
import kidridicarus.common.agent.general.Room;
import kidridicarus.common.tool.DrawOrderAlias;
import kidridicarus.game.SMB.agent.other.LevelEndTrigger;
import kidridicarus.game.SMB.agent.other.PipeWarp;

public class CommonInfo {
	public static final int V_WIDTH = 256;
	public static final int V_HEIGHT = 240;
	// DEBUG: used to quickly change size of screen on desktop without affecting aspect ratio
	public static final int DESKTOP_SCALE = 2;

	public static class LayerDrawOrder {
		public static final AllowOrder NONE = AllowOrder.NOT_ALLOWED;
		public static final AllowOrder MAP_BACKGROUND = new AllowOrder(true, 0f);
		public static final AllowOrder MAP_BOTTOM = new AllowOrder(true, 2f);
		public static final AllowOrder MAP_MIDDLE = new AllowOrder(true, 4f);
		public static final AllowOrder MAP_TOP = new AllowOrder(true, 6f);
		public static final AllowOrder SPRITE_BOTTOM = new AllowOrder(true, 1f);
		public static final AllowOrder SPRITE_MIDDLE = new AllowOrder(true, 3f);
		public static final AllowOrder SPRITE_TOP = new AllowOrder(true, 5f);
	}

	public static final DrawOrderAlias[] KIDRID_DRAWORDER_ALIAS = new DrawOrderAlias[] {
			new DrawOrderAlias("none", LayerDrawOrder.NONE),
			new DrawOrderAlias("map_background", LayerDrawOrder.MAP_BACKGROUND),
			new DrawOrderAlias("map_bottom", LayerDrawOrder.MAP_BOTTOM),
			new DrawOrderAlias("map_middle", LayerDrawOrder.MAP_MIDDLE),
			new DrawOrderAlias("map_top", LayerDrawOrder.MAP_TOP),
			new DrawOrderAlias("sprite_bottom", LayerDrawOrder.SPRITE_BOTTOM),
			new DrawOrderAlias("sprite_middle", LayerDrawOrder.SPRITE_MIDDLE),
			new DrawOrderAlias("sprite_top", LayerDrawOrder.SPRITE_TOP)
		};

	public static final AgentClassList CORE_AGENT_CLASS_LIST = new AgentClassList( 
			CommonKV.AgentClassAlias.VAL_AGENTSPAWNER, AgentSpawner.class,
			CommonKV.AgentClassAlias.VAL_AGENTSPAWN_TRIGGER, AgentSpawnTrigger.class,
			CommonKV.AgentClassAlias.VAL_DESPAWN, DespawnBox.class,
			CommonKV.AgentClassAlias.VAL_PIPEWARP_SPAWN, PipeWarp.class,
			CommonKV.AgentClassAlias.VAL_PLAYERSPAWNER, PlayerSpawner.class,
			CommonKV.AgentClassAlias.VAL_ROOM, Room.class,
			CommonKV.AgentClassAlias.VAL_LEVELEND_TRIGGER, LevelEndTrigger.class,
			CommonKV.AgentClassAlias.VAL_TILEMAP_META, TiledMapMetaAgent.class,
			CommonKV.AgentClassAlias.VAL_ORTHOCOLLISION_TILEMAP, OrthoCollisionTiledMapAgent.class,
			CommonKV.AgentClassAlias.VAL_DRAWABLE_TILEMAP, DrawLayerAgent.class);

	public static class AgentUpdateOrder {
		public static final AllowOrder NONE = AllowOrder.NOT_ALLOWED;
		// contact update is earlier than update
		public static final AllowOrder CONTACT_UPDATE = new AllowOrder(true, 0f);
		// update is earlier than post update
		public static final AllowOrder UPDATE = new AllowOrder(true, 1f);
		// post update is last
		public static final AllowOrder POST_UPDATE = new AllowOrder(true, 2f);
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
