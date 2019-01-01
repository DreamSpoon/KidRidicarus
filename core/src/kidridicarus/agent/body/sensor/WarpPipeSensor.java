package kidridicarus.agent.body.sensor;

import java.util.LinkedList;

import kidridicarus.agency.contact.AgentBodyFilter;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.PipeWarp;

public class WarpPipeSensor extends ContactSensor {
	private LinkedList<PipeWarp> pipes;

	public WarpPipeSensor() {
		pipes = new LinkedList<PipeWarp>();
	}

	@Override
	public void onBeginSense(AgentBodyFilter abf) {
		Agent agent = AgentBodyFilter.getAgentFromFilter(abf);
		if(agent != null && agent instanceof PipeWarp && !pipes.contains(agent))
			pipes.add((PipeWarp) agent);
	}

	@Override
	public void onEndSense(AgentBodyFilter abf) {
		Agent agent = AgentBodyFilter.getAgentFromFilter(abf);
		if(agent != null && agent instanceof PipeWarp && pipes.contains(agent))
			pipes.remove(agent);
	}
	
	public LinkedList<PipeWarp> getPipes() {
		return pipes;
	}

	// TODO: implement this method
	@Override
	public Object getParent() {
		return null;
	}
}
