package kidridicarus.common.agentscript;

import kidridicarus.agency.agentscript.AgentScript;
import kidridicarus.agency.agentscript.ScriptAgentStatus;
import kidridicarus.common.agent.general.WarpPipe;

public class PipeWarpScript implements AgentScript {
	private ScriptAgentStatus startAgentStatus;

	public PipeWarpScript(WarpPipe warpPipe) {
		startAgentStatus = null;
	}

	@Override
	public boolean update(float delta) {
		return false;
	}

	@Override
	public void setInitAgentStatus(ScriptAgentStatus startAgentStatus) {
		this.startAgentStatus = startAgentStatus;
	}

	@Override
	public ScriptAgentStatus getAgentStatus() {
		return startAgentStatus;
	}
}
