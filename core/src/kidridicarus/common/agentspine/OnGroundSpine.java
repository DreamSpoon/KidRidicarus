package kidridicarus.common.agentspine;

import kidridicarus.common.agentsensor.OnGroundSensor;

public class OnGroundSpine {
	private OnGroundSensor ogSensor;

	public OnGroundSpine() {
		ogSensor = null;
	}

	public OnGroundSensor createOnGroundSensor() {
		ogSensor = new OnGroundSensor(null);
		return ogSensor;
	}

//	public void chainToOGSensor(AgentContactSensor sensor) {
//		ogSensor.chainTo(sensor);
//	}

	public boolean isOnGround() {
		// return true if the on ground contacts list contains at least 1 floor
		return ogSensor.isOnGround();
	}
}
