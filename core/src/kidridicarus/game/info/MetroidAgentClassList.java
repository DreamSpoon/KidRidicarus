package kidridicarus.game.info;

import kidridicarus.agency.AgentClassList;
import kidridicarus.game.agent.Metroid.NPC.rio.Rio;
import kidridicarus.game.agent.Metroid.NPC.skree.Skree;
import kidridicarus.game.agent.Metroid.NPC.skreeshot.SkreeShot;
import kidridicarus.game.agent.Metroid.NPC.zoomer.Zoomer;
import kidridicarus.game.agent.Metroid.item.energy.Energy;
import kidridicarus.game.agent.Metroid.item.marumari.MaruMari;
import kidridicarus.game.agent.Metroid.other.deathpop.DeathPop;
import kidridicarus.game.agent.Metroid.other.metroiddoor.MetroidDoor;
import kidridicarus.game.agent.Metroid.other.metroiddoornexus.MetroidDoorNexus;
import kidridicarus.game.agent.Metroid.player.samus.Samus;
import kidridicarus.game.agent.Metroid.player.samuschunk.SamusChunk;
import kidridicarus.game.agent.Metroid.player.samusshot.SamusShot;

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
			MetroidKV.AgentClassAlias.VAL_SKREE_EXP, SkreeShot.class,
			MetroidKV.AgentClassAlias.VAL_ZOOMER, Zoomer.class);
}
