package kidridicarus.agency.agentscript;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.tool.FrameTime;

public interface AgentScript {
	/*
	 * A superclass of AgentSupervisor will create and implement the hooks, to allow a running script to give
	 * information to, and/or trigger actions by, the AgentSupervisor. Scripts must be able to do more than
	 * move a sprite/body around on the screen.
	 */
	public interface AgentScriptHooks {
		public void gotoNextLevel(String levelName);
		// also include start music, stop music, etc.
	}

	public void startScript(AgentHooks agentHooks, AgentScriptHooks scriptHooks,
			ScriptedAgentState beginScriptAgentState);
	public boolean update(FrameTime frameTime);	// return true to continue running script, return false to stop
	public ScriptedAgentState getScriptAgentState();
	// The next script (the script which is requesting the override) is passed so current script can
	// check type of next script (or even call methods of the next script!), to verify/prioritize overrides.
	public boolean isOverridable(AgentScript nextScript);
}
