package kidridicarus.common.agentspine;

import kidridicarus.common.agentsensor.SolidContactSensor;

public class OnGroundSpine {
	private SolidContactSensor ogSensor;

	public OnGroundSpine() {
		ogSensor = null;
	}

	public SolidContactSensor createOnGroundSensor() {
		ogSensor = new SolidContactSensor(null);
		return ogSensor;
	}

	public boolean isOnGround() {
		return ogSensor.isContactFloor();
	}
}
