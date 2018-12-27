package kidridicarus.agent.bodies.sensor;

import kidridicarus.agent.bodies.MobileGroundAgentBody;
import kidridicarus.collisionmap.LineSeg;

public class WalkingSensor implements LineSegContactSensor {
	public enum WalkingSensorType { FOOT };

	private MobileGroundAgentBody body;
	private WalkingSensorType sensorType;

	public WalkingSensor(MobileGroundAgentBody body, WalkingSensorType sensorType) {
		this.body = body;
		this.sensorType = sensorType;
	}
	
	public void onBeginContact(LineSeg lineSeg) {
		if(sensorType == WalkingSensorType.FOOT)
			body.onBeginContactSensor(sensorType, lineSeg);
	}

	public void onEndContact(LineSeg lineSeg) {
		if(sensorType == WalkingSensorType.FOOT)
			body.onEndContactSensor(sensorType, lineSeg);
	}
}
