package kidridicarus.game.info;

import kidridicarus.agency.AgentClassList;
import kidridicarus.game.agent.KidIcarus.NPC.shemum.Shemum;
import kidridicarus.game.agent.KidIcarus.NPC.smallpoof.SmallPoof;

public class KidIcarusAgentClassList {
	public static final AgentClassList KIDICARUS_AGENT_CLASSLIST = new AgentClassList( 
			KidIcarusKV.AgentClassAlias.VAL_SHEMUM, Shemum.class,
			KidIcarusKV.AgentClassAlias.VAL_SMALL_POOF, SmallPoof.class);
}
