package kidridicarus.agency.agentscript;

public interface AgentScript {
	public boolean update(float delta);	// return true to continue running script, return false to stop
	public void setInitAgentStatus(ScriptAgentStatus startAgentStatus);
	public ScriptAgentStatus getAgentStatus();
}
