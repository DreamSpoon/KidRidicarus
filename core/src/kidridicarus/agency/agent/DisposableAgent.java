package kidridicarus.agency.agent;

/*
 * Agent which requires disposeAgent method be called upon removal from its Agency.
 * Note:
 * All agents can be "disposed", as in removed, from the list of all agents in the Agency's AgencyIndex - this class
 * does not change that.
 * This class exists to allow for "meta-Agents", or Agents made up of multiple sub-Agents.
 *   e.g. The level map is a "meta-Agent" made up of sub-Agents:
 *     -collision map
 *     -drawable background and scenery Agents
 *     -initial spawn Agents
 *     When the level map is disposed, it will dispose of its sub-Agents.
 * When a meta-Agent is disposed, it will choose to call the dispose methods (if any) of its sub-Agents.
 * Thus, when a meta-Agent is disposed during regular Agency updates, it will properly handle the remove/dispose
 * needs of it's sub-Agents by calling removeAgent for each of it's sub-agents. The Agency should not handle the
 * sub-Agent removal/disposal because the sub-Agents could conceivably be shared by other Agents/meta-Agents.
 * Also, when a meta-Agent is disposed at Agency class dispose time, the Agency class will not call the dispose
 * methods of the meta-Agent's sub-Agents. Instead, the meta-Agent will receive a disposeAgent call and choose
 * how to handle its sub-Agents.
 * Since Agency will remove all Agents from its AgencyIndex at disposal, the only chance an Agent gets to release
 * graphics memory / other resources is when its disposeAgent method is called. The meta-Agent concept should allow
 * for proper coordination of resource disposal.
 */
public interface DisposableAgent {
	public void disposeAgent();
}
