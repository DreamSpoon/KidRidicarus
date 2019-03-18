package kidridicarus.agency.agentscript;

public interface AgentScript {
	public interface AgentScriptHooks {
		public void gotoNextLevel(String levelName);
		// also include start music, stop music, etc.
	}

	public void startScript(AgentScriptHooks asHooks, ScriptedAgentState beginScriptAgentState);
	public boolean update(float delta);	// return true to continue running script, return false to stop
	public ScriptedAgentState getScriptAgentState();
	// The next script (the script which is requesting the override) is passed so current script can
	// check type of next script (or even call methods of the next script!), to verify/prioritize overrides.
	public boolean isOverridable(AgentScript nextScript);
}
