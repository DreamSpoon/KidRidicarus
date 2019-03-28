package kidridicarus.agency.agent;

/*
 * A callback for Agents that need to respawn sub-Agents (e.g. an AgentSpawner that spawns one Agent at a time,
 * but will spawn multiple Agents over a period of time), to de-target a disposed target, etc.
 */
public abstract class AgentRemoveListener {
	public abstract void removedAgent();

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
