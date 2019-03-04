package kidridicarus.agency.agentscript;

public interface AgentScript {
	public void startScript(ScriptedAgentState beginScriptAgentState);
	public boolean update(float delta);	// return true to continue running script, return false to stop
	public ScriptedAgentState getScriptAgentState();
}
