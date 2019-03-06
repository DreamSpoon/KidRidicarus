package kidridicarus.agency.agentscript;

public interface AgentScript {
	public interface AgentScriptHooks {
		public void gotoNextLevel(String nextLevelName);
		// also include start music, stop music, etc.
	}

	public void startScript(AgentScriptHooks asHooks, ScriptedAgentState beginScriptAgentState);
	public boolean update(float delta);	// return true to continue running script, return false to stop
	public ScriptedAgentState getScriptAgentState();
	public boolean isOverridable();
}
