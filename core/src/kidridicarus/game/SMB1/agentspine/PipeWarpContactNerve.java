package kidridicarus.game.SMB1.agentspine;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.SMB1.agent.other.pipewarp.PipeWarp;

public class PipeWarpContactNerve {
	private AgentContactHoldSensor pipeWarpSensor = null;

	public AgentContactHoldSensor createPipeWarpSensor(AgentBody body) {
		pipeWarpSensor = new AgentContactHoldSensor(body);
		return pipeWarpSensor;
	}

	public PipeWarp getEnterPipeWarp(AgentBody body, Direction4 moveDir) {
		if(moveDir == null)
			return null;
		for(PipeWarp pw : pipeWarpSensor.getContactsByClass(PipeWarp.class)) {
			if(pw.canBodyEnterPipe(body.getBounds(), moveDir))
				return pw;
		}
		return null;
	}
}
