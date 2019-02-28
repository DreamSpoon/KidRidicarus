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
			GameKV.Metroid.VAL_DEATH_POP, DeathPop.class,
			GameKV.Metroid.VAL_DOOR, MetroidDoor.class,
			GameKV.Metroid.VAL_MARUMARI, MaruMari.class,
			GameKV.Metroid.VAL_SAMUS, Samus.class,
			GameKV.Metroid.VAL_SAMUS_SHOT, SamusShot.class,
			GameKV.Metroid.VAL_SKREE, Skree.class,
			GameKV.Metroid.VAL_SKREE_EXP, SkreeExp.class,
			GameKV.Metroid.VAL_ZOOMER, Zoomer.class);
}
