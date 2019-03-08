package kidridicarus.game.info;

import kidridicarus.agency.AgentClassList;
import kidridicarus.game.Metroid.agent.NPC.DeathPop;
import kidridicarus.game.Metroid.agent.NPC.MetroidDoor;
import kidridicarus.game.Metroid.agent.NPC.Skree;
import kidridicarus.game.Metroid.agent.NPC.SkreeExp;
import kidridicarus.game.Metroid.agent.NPC.Zoomer;
import kidridicarus.game.Metroid.agent.item.MaruMari;
import kidridicarus.game.Metroid.agent.player.Samus;
import kidridicarus.game.Metroid.agent.player.SamusShot;

public class MetroidInfo {
	public static final AgentClassList METROID_AGENT_CLASSLIST = new AgentClassList( 
			GameKV.Metroid.AgentClassAlias.VAL_DEATH_POP, DeathPop.class,
			GameKV.Metroid.AgentClassAlias.VAL_DOOR, MetroidDoor.class,
			GameKV.Metroid.AgentClassAlias.VAL_MARUMARI, MaruMari.class,
			GameKV.Metroid.AgentClassAlias.VAL_SAMUS, Samus.class,
			GameKV.Metroid.AgentClassAlias.VAL_SAMUS_SHOT, SamusShot.class,
			GameKV.Metroid.AgentClassAlias.VAL_SKREE, Skree.class,
			GameKV.Metroid.AgentClassAlias.VAL_SKREE_EXP, SkreeExp.class,
			GameKV.Metroid.AgentClassAlias.VAL_ZOOMER, Zoomer.class);
}
