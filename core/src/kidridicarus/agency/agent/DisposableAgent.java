package kidridicarus.agency.agent;

/*
 * Agent requiring disposeAgent method to be run upon removal from its Agency.
 *
 * All agents can be "disposed", as in removed, from the list of all agents in the Agency's AgencyIndex - this class
 * does not change that. This class' functionality should ensure proper coordination of resource disposal.
 * This class exists to allow for "meta-Agents", or Agents made up of multiple sub-Agents.
 *   e.g. The level map is a "meta-Agent" made up of sub-Agents:
 *     -solid tile map
 *     -drawable background and scenery
 *     -initial spawn Agents
 *     When the level map is disposed, it must dispose of its sub-Agents.
 * When a MetaAgent is disposed, it will choose to call the dispose methods (if any) of its sub-Agents.
 * Thus, when a MetaAgent is disposed during regular Agency updates, it will properly handle the remove/dispose
 * needs of it's sub-Agents by running dispose methods for each of it's sub-Agents. The Agency should not handle the
 * sub-Agent removal/disposal because the sub-Agents could conceivably be shared by other Agents/meta-Agents.
 * Also, when a MetaAgent is disposed at Agency class dispose time, the Agency class will not call the dispose
 * methods of the MetaAgent's sub-Agents. Instead, the meta-Agent will receive a disposeAgent call and choose
 * how to handle its sub-Agents.
 * Since Agency will remove all Agents from its AgencyIndex at disposal, the only chance an Agent gets to release
 * graphics memory / other resources is when disposeAgent method is run.
 *
 * Q) I'm planning to code a new Agent and I want to know: Should it implement Disposable or DisposableAgent? Or both?
 * A) If the proposed Agent does not need a dispose method, then don't implement either!
 *    But, if a Box2D body needs to be destroyed, or if some graphical/audio resources need releasing, etc., then
 *    implement at least one of the two dispose methods.
 *    Always use DisposableAgent unless all of the following conditions are satisfied:
 *      1) The Agent is a sub-Agent of a parent MetaAgent.
 *      2) The parent MetaAgent calls the sub-Agent's dispose method.
 *      3) The parent MetaAgent implements DisposableAgent.
 *      4) If conditions 1 and 2 are both met, but condition 3 is not met, then recursively apply the above 3
 *         conditions to the parent MetaAgent, i.e.
 *           i) The MetaAgent is a sub-Agent of a parent MetaAgent.
 *           ii) The parent MetaAgent calls the MetaAgent's dispose method.
 *           iii) The parent MetaAgent implements DisposableAgent.
 *           ...
 *    If all of the above conditions are satisfied, then implement Disposable (or functionality like it), and use
 *    interface DisposableAgent if necessary (TODO double-check this last point).
 * Because:
 *    Every Agent that needs disposal (e.g. an Agent that creates a Box2D Body) must handle disposal under
 *    different sets of conditions:
 *      1) Agent is disposed in a frame, and Agency is not disposed after running that frame.
 *      2) Agent is not disposed in a frame, and Agency is disposed after running that frame.
 *      Note: Agency must not be disposed during a frame.
 *    When an MetaAgent (a type of Agent) is disposed in a frame, and Agency is not disposed after running that
 *    frame, then the Agent can enforce the order of disposal of its sub-Agents.
 *    During Agency disposal however, the Agency will iterate through a list of Agents and dispose each Agent in
 *    no particular order (no priority queue). This is where the functionality difference between Disposable and
 *    DisposableAgent becomes important.
 *    Agency WILL run disposeAgent method of every DisposableAgent that is removed when Agency is disposed.
 *    Agency will NOT run dispose method of any Disposable that is removed when Agency is disposed.
 *    e.g. Two Agents are added to Agency and then Agency is disposed, what happens?
 *        class AgentA extends Agent implements DisposableAgent { ... }
 *        class AgentB extends Agent implements Disposable { ... }
 *        Agency.addAgent(new AgentA(...));
 *        Agency.addAgent(new AgentB(...));
 *        ...
 *        Agency.dispose();
 *
 *      When Agency.dispose() is run,
 *        It will run disposeAgent method of class AgentA, and
 *        It will NOT run dispose method of class AgentB.
 */
public interface DisposableAgent {
	public void disposeAgent();
}
