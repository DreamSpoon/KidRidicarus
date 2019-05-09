package kidridicarus.agency.agent;

import kidridicarus.agency.Agent;

/*
 * A callback that is triggered when a specified Agent is removed from Agency.
 * listeningAgent receives the callback when otherAgent is removed.
 * e.g.
 *   -an AgentSpawner that spawns one Agent at a time, but will spawn multiple Agents over a period of time)
 *   -de-target a dead target, etc.
 *
 * At Agency disposal time, the order of removal of Agents from Agency is undefined (it's actually well defined
 * within Agency, but for the sake of argument, assume it is undefinable). Disposal of sub-Agents must be handled
 * by a meta-Agent to prevent double disposal of the same sub-Agent. This problem isn't apparent until a meta-Agent
 * needs to implement dispose functionality under two different conditions:
 *   1) Disposal of Agent at Agency dispose time.
 *   2) Disposal of Agent at end of update frame.
 *
 * If the meta-Agent shares resources among its sub-Agents then the meta-Agent should create only one
 * AgentRemoveListener, and handle disposal needs (i.e. releasing resources) by calling sub-Agent dispose methods.
 *
 * TODO is postRemoveAgent a good idea?
 */
public abstract class AgentRemoveListener {
	public abstract void preRemoveAgent();

	private final Agent listeningAgent;
	private final Agent otherAgent;

	public AgentRemoveListener(Agent listeningAgent, Agent otherAgent) {
		this.listeningAgent = listeningAgent;
		this.otherAgent = otherAgent;
	}

	public Agent getListeningAgent() {
		return listeningAgent;
	}

	public Agent getOtherAgent() {
		return otherAgent;
	}
}
