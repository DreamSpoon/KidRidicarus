package kidridicarus.agency.agentscript;

import kidridicarus.game.tool.QQ;

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
QQ.pr("start script begin");
		if(isRunning) {
QQ.pr("start script return false");
			return false;
		}
		isRunning = true;

		currentScript = agentScript;
		currentScript.startScript(startAgentStatus);
QQ.pr("start script return true");
		return true;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void update(float delta) {
		if(!isRunning)
			return;
QQ.pr("update script, delta = " + delta);
		isRunning = currentScript.update(delta);
QQ.pr("update script, isRunning = " + isRunning);
	}

	public ScriptAgentStatus getAgentStatus() {
		return currentScript.getScriptAgentStatus();
	}
}
