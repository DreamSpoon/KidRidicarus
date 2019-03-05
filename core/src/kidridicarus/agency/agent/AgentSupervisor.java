package kidridicarus.agency.agent;

import kidridicarus.agency.agentscript.AgentScript;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.AgentScriptRunner;
import kidridicarus.agency.tool.BasicAdvice;
import kidridicarus.agency.tool.SuperAdvice;

/*
 * Supervisor is expected to handle stuff for PlayerAgents:
 *   -scripted actions
 *   -relaying advice to the agent
 *   -...
 */
public abstract class AgentSupervisor {
	private AgentScriptRunner scriptRunner;

	public abstract void setFrameAdvice(SuperAdvice superAdvice);
	public abstract BasicAdvice pollFrameAdvice();

	/*
	 * Convert the Player agent information into a simpler script agent state format, which will be used to
	 * initialize the agent state when a script is started.
	 */
	protected abstract ScriptedAgentState getCurrentScriptAgentState();

	public AgentSupervisor() {
		scriptRunner = new AgentScriptRunner();
	}

	public void preUpdateAgency(float delta) {
		scriptRunner.preUpdateAgency(delta);
	}

	public void postUpdateAgency() {
		scriptRunner.postUpdateAgency();
	}

	/*
	 * Return false if already running a script.
	 * Otherwise start using the given script and return true. 
	 */
	public boolean startScript(AgentScript agentScript) {
		return scriptRunner.startScript(agentScript, getCurrentScriptAgentState());
	}

	public ScriptedAgentState getScriptAgentState() {
		return scriptRunner.getScriptAgentState();
	}

	public boolean isScriptRunning() {
		return scriptRunner.isRunning();
	}
}
