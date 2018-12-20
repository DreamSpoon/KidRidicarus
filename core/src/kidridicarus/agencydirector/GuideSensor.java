package kidridicarus.agencydirector;

import kidridicarus.agent.Agent;
import kidridicarus.agent.bodies.AgentBody;
import kidridicarus.agent.bodies.SMB.PipeWarpBody;
import kidridicarus.agent.bodies.SMB.player.MarioBody;
import kidridicarus.agent.bodies.optional.BumpableBody;
import kidridicarus.agent.optional.ItemAgent;
import kidridicarus.collisionmap.LineSeg;

public class GuideSensor {
	public enum SensorType { BODY, FOOT, HEAD, SIDE };

	private MarioBody marioBody;
	private SensorType sensorType;

	public GuideSensor(MarioBody marioBody, SensorType sensorType) {
		this.marioBody = marioBody;
		this.sensorType = sensorType;
	}

	public void onBeginContact(AgentBody agentBody) {
		if(agentBody instanceof PipeWarpBody)
			marioBody.onBeginContactPipe((PipeWarpBody) agentBody);
		else if(agentBody instanceof BumpableBody && sensorType == SensorType.HEAD)
			marioBody.onBeginContactBumpable(agentBody);
		else if(sensorType == SensorType.BODY) {
			Agent agent = agentBody.getParent();
			if(agent instanceof ItemAgent)
				marioBody.onContactItem(agentBody);
			else
				marioBody.onContactAgent(agentBody);
		}
	}

	public void onEndContact(AgentBody agentBody) {
		if(agentBody instanceof PipeWarpBody)
			marioBody.onEndContactPipe((PipeWarpBody) agentBody);
		else if(agentBody instanceof BumpableBody && sensorType == SensorType.HEAD)
			marioBody.onEndContactBumpable(agentBody);
	}

	public void onBeginContact(LineSeg lineSeg) {
		if(sensorType == SensorType.FOOT)
			marioBody.onFootBeginContactBound(lineSeg);
	}

	public void onEndContact(LineSeg lineSeg) {
		if(sensorType == SensorType.FOOT)
			marioBody.onFootEndContactBound(lineSeg);
	}
}
