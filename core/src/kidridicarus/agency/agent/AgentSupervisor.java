package kidridicarus.agency.agent;

import kidridicarus.agency.agentscript.AgentScript;
import kidridicarus.agency.agentscript.ScriptAgentStatus;
import kidridicarus.agency.agentscript.ScriptRunner;
import kidridicarus.agency.tool.BasicAdvice;
import kidridicarus.agency.tool.SuperAdvice;

/*
 * Supervisor is expected to handle stuff for PlayerAgents:
 *   -scripted actions
 *   -relaying advice to the agent
 *   -...
 */
public abstract class AgentSupervisor {
	private ScriptRunner scriptRunner;

	public abstract void setFrameAdvice(SuperAdvice superAdvice);
	public abstract BasicAdvice pollFrameAdvice();

	/*
	 * Convert the Player agent information into a simpler script agent status format, which will be used to
	 * initialize the agent status when a script is started.
	 */
	protected abstract ScriptAgentStatus getCurrentScriptAgentStatus();

	public AgentSupervisor() {
		scriptRunner = new ScriptRunner();
	}

	public void postUpdateAgency(float delta) {
		scriptRunner.update(delta);
	}

	/*
	 * Return false if already running a script.
	 * Otherwise start using the given script and return true. 
	 */
	public boolean startScript(AgentScript agentScript) {
		return scriptRunner.startScript(agentScript, getCurrentScriptAgentStatus());
	}

	public ScriptAgentStatus getScriptAgentStatus() {
		return scriptRunner.getAgentStatus();
	}

	public boolean isScriptRunning() {
		return scriptRunner.isRunning();
	}
}
