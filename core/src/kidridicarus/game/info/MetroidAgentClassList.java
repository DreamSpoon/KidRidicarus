package kidridicarus.game.info;

import kidridicarus.agency.tool.AgentClassList;
import kidridicarus.game.Metroid.agent.NPC.rio.Rio;
import kidridicarus.game.Metroid.agent.NPC.skree.Skree;
import kidridicarus.game.Metroid.agent.NPC.skreeshot.SkreeShot;
import kidridicarus.game.Metroid.agent.NPC.zoomer.Zoomer;
import kidridicarus.game.Metroid.agent.item.energy.Energy;
import kidridicarus.game.Metroid.agent.item.marumari.MaruMari;
import kidridicarus.game.Metroid.agent.other.deathpop.DeathPop;
import kidridicarus.game.Metroid.agent.other.metroiddoor.MetroidDoor;
import kidridicarus.game.Metroid.agent.other.metroiddoornexus.MetroidDoorNexus;
import kidridicarus.game.Metroid.agent.player.samus.Samus;
import kidridicarus.game.Metroid.agent.player.samuschunk.SamusChunk;
import kidridicarus.game.Metroid.agent.player.samusshot.SamusShot;

public class MetroidAgentClassList {
	public static final AgentClassList METROID_AGENT_CLASSLIST = new AgentClassList( 
			MetroidKV.AgentClassAlias.VAL_DEATH_POP, DeathPop.class,
			MetroidKV.AgentClassAlias.VAL_DOOR, MetroidDoor.class,
			MetroidKV.AgentClassAlias.VAL_DOOR_NEXUS, MetroidDoorNexus.class,
			MetroidKV.AgentClassAlias.VAL_ENERGY, Energy.class,
			MetroidKV.AgentClassAlias.VAL_MARUMARI, MaruMari.class,
			MetroidKV.AgentClassAlias.VAL_SAMUS, Samus.class,
			MetroidKV.AgentClassAlias.VAL_SAMUS_CHUNK, SamusChunk.class,
			MetroidKV.AgentClassAlias.VAL_SAMUS_SHOT, SamusShot.class,
			MetroidKV.AgentClassAlias.VAL_RIO, Rio.class,
			MetroidKV.AgentClassAlias.VAL_SKREE, Skree.class,
			MetroidKV.AgentClassAlias.VAL_SKREE_SHOT, SkreeShot.class,
			MetroidKV.AgentClassAlias.VAL_ZOOMER, Zoomer.class);
}
