package kidridicarus.game.info;

import kidridicarus.agency.AgentClassList;
import kidridicarus.game.agent.KidIcarus.NPC.monoeye.Monoeye;
import kidridicarus.game.agent.KidIcarus.NPC.shemum.Shemum;
import kidridicarus.game.agent.KidIcarus.item.heart1.Heart1;
import kidridicarus.game.agent.KidIcarus.other.smallpoof.SmallPoof;
import kidridicarus.game.agent.KidIcarus.player.pit.Pit;
import kidridicarus.game.agent.KidIcarus.player.pitarrow.PitArrow;

public class KidIcarusAgentClassList {
	public static final AgentClassList KIDICARUS_AGENT_CLASSLIST = new AgentClassList( 
			KidIcarusKV.AgentClassAlias.VAL_HEART1, Heart1.class,
			KidIcarusKV.AgentClassAlias.VAL_MONOEYE, Monoeye.class,
			KidIcarusKV.AgentClassAlias.VAL_PIT, Pit.class,
			KidIcarusKV.AgentClassAlias.VAL_PIT_ARROW, PitArrow.class,
			KidIcarusKV.AgentClassAlias.VAL_SHEMUM, Shemum.class,
			KidIcarusKV.AgentClassAlias.VAL_SMALL_POOF, SmallPoof.class);
}
