package kidridicarus.game.info;

import kidridicarus.agency.AgentClassList;
import kidridicarus.game.agent.Metroid.NPC.DeathPop;
import kidridicarus.game.agent.Metroid.NPC.Skree;
import kidridicarus.game.agent.Metroid.NPC.SkreeExp;
import kidridicarus.game.agent.Metroid.NPC.Zoomer;
import kidridicarus.game.agent.Metroid.item.MaruMari;
import kidridicarus.game.agent.Metroid.player.Samus;
import kidridicarus.game.agent.Metroid.player.SamusShot;

public class MetroidInfo {
	public static final AgentClassList metroidAgentClassList = new AgentClassList( 
			KVInfo.Metroid.VAL_DEATH_POP, DeathPop.class,
			KVInfo.Metroid.VAL_MARUMARI, MaruMari.class,
			KVInfo.Metroid.VAL_SAMUS, Samus.class,
			KVInfo.Metroid.VAL_SAMUS_SHOT, SamusShot.class,
			KVInfo.Metroid.VAL_SKREE, Skree.class,
			KVInfo.Metroid.VAL_SKREE_EXP, SkreeExp.class,
			KVInfo.Metroid.VAL_ZOOMER, Zoomer.class);
}
