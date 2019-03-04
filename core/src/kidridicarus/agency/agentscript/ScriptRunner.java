package kidridicarus.agency.agentscript;

public class ScriptRunner {
	private boolean isRunning;
	private AgentScript currentScript;

	public ScriptRunner() {
		isRunning = false;
		currentScript = null;
	}

	/*
	 * Returns true if script was started, otherwise returns false.
	 * Takes the beginning status of the agent.
	 */
	public boolean startScript(AgentScript agentScript, ScriptAgentStatus startAgentStatus) {
		if(isRunning)
			return false;
		isRunning = true;
		currentScript = agentScript;
		currentScript.startScript(startAgentStatus);
		return true;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void update(float delta) {
		if(!isRunning)
			return;
		isRunning = currentScript.update(delta);
	}

	public ScriptAgentStatus getAgentStatus() {
		return currentScript.getScriptAgentStatus();
	}
}
