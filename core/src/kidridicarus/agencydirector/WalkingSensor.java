package kidridicarus.agencydirector;

import kidridicarus.agent.bodies.MobileGroundAgentBody;
import kidridicarus.collisionmap.LineSeg;

public class WalkingSensor implements AgentSensor {

	private MobileGroundAgentBody maBody;
	public enum WalkingSensorType { FOOT };
	private WalkingSensorType sensorType;
	

	public WalkingSensor(MobileGroundAgentBody maBody, WalkingSensorType sensorType) {
		this.maBody = maBody;
		this.sensorType = sensorType;
	}
	
	public void onBeginContact(LineSeg lineSeg) {
		if(sensorType == WalkingSensorType.FOOT)
			maBody.onBeginContactSensor(sensorType, lineSeg);
	}		

	public void onEndContact(LineSeg lineSeg) {
		if(sensorType == WalkingSensorType.FOOT)
			maBody.onEndContactSensor(sensorType, lineSeg);
	}
}
