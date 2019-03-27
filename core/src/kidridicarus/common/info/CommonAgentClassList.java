package kidridicarus.common.info;

import kidridicarus.agency.AgentClassList;
import kidridicarus.common.agent.agentspawner.AgentSpawner;
import kidridicarus.common.agent.agentspawntrigger.AgentSpawnTrigger;
import kidridicarus.common.agent.despawnbox.DespawnBox;
import kidridicarus.common.agent.keepalivebox.KeepAliveBox;
import kidridicarus.common.agent.levelendtrigger.LevelEndTrigger;
import kidridicarus.common.agent.playerspawner.PlayerSpawner;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agent.scrollpushbox.ScrollPushBox;
import kidridicarus.common.metaagent.tiledmap.TiledMapMetaAgent;
import kidridicarus.common.metaagent.tiledmap.collision.CollisionTiledMapAgent;
import kidridicarus.common.metaagent.tiledmap.drawlayer.DrawLayerAgent;
import kidridicarus.game.agent.SMB.other.pipewarp.PipeWarp;

public class CommonAgentClassList {
	public static final AgentClassList CORE_AGENT_CLASS_LIST = new AgentClassList( 
			CommonKV.AgentClassAlias.VAL_AGENTSPAWNER, AgentSpawner.class,
			CommonKV.AgentClassAlias.VAL_AGENTSPAWN_TRIGGER, AgentSpawnTrigger.class,
			CommonKV.AgentClassAlias.VAL_DESPAWN, DespawnBox.class,
			CommonKV.AgentClassAlias.VAL_DRAWABLE_TILEMAP, DrawLayerAgent.class,
			CommonKV.AgentClassAlias.VAL_KEEP_ALIVE_BOX, KeepAliveBox.class,
			CommonKV.AgentClassAlias.VAL_LEVELEND_TRIGGER, LevelEndTrigger.class,
			CommonKV.AgentClassAlias.VAL_ORTHOCOLLISION_TILEMAP, CollisionTiledMapAgent.class,
			CommonKV.AgentClassAlias.VAL_PIPEWARP_SPAWN, PipeWarp.class,
			CommonKV.AgentClassAlias.VAL_PLAYERSPAWNER, PlayerSpawner.class,
			CommonKV.AgentClassAlias.VAL_ROOM, RoomBox.class,
			CommonKV.AgentClassAlias.VAL_SCROLL_PUSH_BOX, ScrollPushBox.class,
			CommonKV.AgentClassAlias.VAL_TILEMAP_META, TiledMapMetaAgent.class);
}
