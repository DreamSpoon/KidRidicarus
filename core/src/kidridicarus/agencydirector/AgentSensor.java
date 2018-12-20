package kidridicarus.agencydirector;

import kidridicarus.agent.bodies.MobileAgentBody;
import kidridicarus.collisionmap.LineSeg;

public class AgentSensor {
	public enum AgentSensorType { FOOT };

	private MobileAgentBody maBody;
	private AgentSensorType sensorType;

	public AgentSensor(MobileAgentBody maBody, AgentSensorType sensorType) {
		this.maBody = maBody;
		this.sensorType = sensorType;
	}

	public void onBeginContact(LineSeg lineSeg) {
		if(sensorType == AgentSensorType.FOOT)
			maBody.onBeginContactSensor(sensorType, lineSeg);
	}

	public void onEndContact(LineSeg lineSeg) {
		if(sensorType == AgentSensorType.FOOT)
			maBody.onEndContactSensor(sensorType, lineSeg);
	}
}
