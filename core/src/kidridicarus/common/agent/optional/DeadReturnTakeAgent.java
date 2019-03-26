package kidridicarus.common.agent.optional;

import kidridicarus.agency.agent.Agent;

/*
 * Like a library book-return box, a spawn box can loan out an NPC (or many, via a HashSet).
 * When the NPC "dies", it returns itself to the spawn box by calling the spawn box's onTakeDeadReturn method
 * with itself as the parameter (i.e. .onTakeDeadReturn(this) ).
 */
public interface DeadReturnTakeAgent {
	public void onTakeDeadReturn(Agent deadAgent);
}
