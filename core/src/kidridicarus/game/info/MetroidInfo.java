package kidridicarus.game.info;

import kidridicarus.agency.AgentClassList;
import kidridicarus.game.agent.Metroid.NPC.DeathPop;
import kidridicarus.game.agent.Metroid.NPC.MetroidDoor;
import kidridicarus.game.agent.Metroid.NPC.Skree;
import kidridicarus.game.agent.Metroid.NPC.SkreeExp;
import kidridicarus.game.agent.Metroid.NPC.Zoomer;
import kidridicarus.game.agent.Metroid.item.MaruMari;
import kidridicarus.game.agent.Metroid.player.Samus;
import kidridicarus.game.agent.Metroid.player.SamusShot;

public class MetroidInfo {
	public static final AgentClassList METROID_AGENT_CLASSLIST = new AgentClassList( 
			GameKV.Metroid.VAL_DEATH_POP, DeathPop.class,
			GameKV.Metroid.VAL_DOOR, MetroidDoor.class,
			GameKV.Metroid.VAL_MARUMARI, MaruMari.class,
			GameKV.Metroid.VAL_SAMUS, Samus.class,
			GameKV.Metroid.VAL_SAMUS_SHOT, SamusShot.class,
			GameKV.Metroid.VAL_SKREE, Skree.class,
			GameKV.Metroid.VAL_SKREE_EXP, SkreeExp.class,
			GameKV.Metroid.VAL_ZOOMER, Zoomer.class);
}
