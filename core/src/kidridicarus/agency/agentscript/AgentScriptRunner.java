package kidridicarus.agency.agentscript;

/*
 * *Basic* order of operations for game loop and scripts:
 *   1) Get user input and call script update methods
 *   2) Run game world update (scripts may be started during game world update)
 *   4) Draw game world
 * 
 * Scripts may be started during game world update, but these scripts do not receive their first update until
 * next frame update. However, it is possible for the scripts to modify the player agent in the same frame in which
 * they were started, if the player agent checks for script starts during it's update frame.
 * The 'continue running' flag is needed so the player agent can check if the script was running at any point during
 * the current update frame. If the 'continue running' flag isn't used then the script's final agent update state
 * might be ignored by the player agent.
 */
public class AgentScriptRunner {
	private boolean isRunning;
	private boolean continueRunning;
	private AgentScript currentScript;

	public AgentScriptRunner() {
		isRunning = false;
		continueRunning = false;
		currentScript = null;
	}

	/*
	 * Returns true if script was started, otherwise returns false.
	 * Takes the beginning state of the agent.
	 */
	public boolean startScript(AgentScript agentScript, ScriptedAgentState startAgentState) {
		if(isRunning)
			return false;
		isRunning = true;
		continueRunning = true;
		currentScript = agentScript;
		currentScript.startScript(startAgentState);
		return true;
	}

	public void preUpdateAgency(float delta) {
		if(!isRunning)
			return;
		continueRunning = currentScript.update(delta); 
	}

	public void postUpdateAgency() {
		if(!isRunning)
			return;
		isRunning = continueRunning;
	}

	public ScriptedAgentState getScriptAgentState() {
		return currentScript.getScriptAgentState();
	}

	public boolean isRunning() {
		return isRunning;
	}

	public boolean isRunningMoveAdvice() {
		if(!isRunning)
			return false;
		ScriptedAgentState sas = currentScript.getScriptAgentState();
		if(sas == null)
			return false;
		return sas.scriptedMoveAdvice != null;
	}
}
