package kidridicarus.game.info;

import kidridicarus.agency.AgentClassList;
import kidridicarus.game.agent.KidIcarus.NPC.monoeye.Monoeye;
import kidridicarus.game.agent.KidIcarus.NPC.shemum.Shemum;
import kidridicarus.game.agent.KidIcarus.NPC.specknose.Specknose;
import kidridicarus.game.agent.KidIcarus.item.angelheart.AngelHeart;
import kidridicarus.game.agent.KidIcarus.item.chalicehealth.ChaliceHealth;
import kidridicarus.game.agent.KidIcarus.other.kidicarusdoor.KidIcarusDoor;
import kidridicarus.game.agent.KidIcarus.other.vanishpoof.VanishPoof;
import kidridicarus.game.agent.KidIcarus.player.pit.Pit;
import kidridicarus.game.agent.KidIcarus.player.pitarrow.PitArrow;

public class KidIcarusAgentClassList {
	public static final AgentClassList KIDICARUS_AGENT_CLASSLIST = new AgentClassList( 
			KidIcarusKV.AgentClassAlias.VAL_ANGEL_HEART, AngelHeart.class,
			KidIcarusKV.AgentClassAlias.VAL_CHALICE_HEALTH, ChaliceHealth.class,
			KidIcarusKV.AgentClassAlias.VAL_DOOR, KidIcarusDoor.class,
			KidIcarusKV.AgentClassAlias.VAL_MONOEYE, Monoeye.class,
			KidIcarusKV.AgentClassAlias.VAL_PIT, Pit.class,
			KidIcarusKV.AgentClassAlias.VAL_PIT_ARROW, PitArrow.class,
			KidIcarusKV.AgentClassAlias.VAL_SHEMUM, Shemum.class,
			KidIcarusKV.AgentClassAlias.VAL_SPECKNOSE, Specknose.class,
			KidIcarusKV.AgentClassAlias.VAL_VANISH_POOF, VanishPoof.class);
}
