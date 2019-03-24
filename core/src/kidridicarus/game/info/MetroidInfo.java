package kidridicarus.game.info;

import kidridicarus.agency.AgentClassList;
import kidridicarus.game.agent.Metroid.NPC.skree.Skree;
import kidridicarus.game.agent.Metroid.NPC.skreeshot.SkreeShot;
import kidridicarus.game.agent.Metroid.NPC.zoomer.Zoomer;
import kidridicarus.game.agent.Metroid.item.energy.Energy;
import kidridicarus.game.agent.Metroid.item.marumari.MaruMari;
import kidridicarus.game.agent.Metroid.other.deathpop.DeathPop;
import kidridicarus.game.agent.Metroid.other.metroiddoor.MetroidDoor;
import kidridicarus.game.agent.Metroid.player.samus.Samus;
import kidridicarus.game.agent.Metroid.player.samuschunk.SamusChunk;
import kidridicarus.game.agent.Metroid.player.samusshot.SamusShot;

public class MetroidInfo {
	public static final AgentClassList METROID_AGENT_CLASSLIST = new AgentClassList( 
			GameKV.Metroid.AgentClassAlias.VAL_DEATH_POP, DeathPop.class,
			GameKV.Metroid.AgentClassAlias.VAL_DOOR, MetroidDoor.class,
			GameKV.Metroid.AgentClassAlias.VAL_ENERGY, Energy.class,
			GameKV.Metroid.AgentClassAlias.VAL_MARUMARI, MaruMari.class,
			GameKV.Metroid.AgentClassAlias.VAL_SAMUS, Samus.class,
			GameKV.Metroid.AgentClassAlias.VAL_SAMUS_CHUNK, SamusChunk.class,
			GameKV.Metroid.AgentClassAlias.VAL_SAMUS_SHOT, SamusShot.class,
			GameKV.Metroid.AgentClassAlias.VAL_SKREE, Skree.class,
			GameKV.Metroid.AgentClassAlias.VAL_SKREE_EXP, SkreeShot.class,
			GameKV.Metroid.AgentClassAlias.VAL_ZOOMER, Zoomer.class);
}
