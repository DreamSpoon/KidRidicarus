package kidridicarus.common.agent.optional;

/*
 * More properly, an enable/disable take Agent.
 * Note: An Agent which needs the functionality of enable but not the functionality of disable
 *   should use TriggerTakeAgent, instead of this interface.
 */
public interface EnableTakeAgent {
	public void onTakeEnable(boolean enabled);
}
