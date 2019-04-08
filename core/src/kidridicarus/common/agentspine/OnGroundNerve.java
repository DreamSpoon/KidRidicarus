package kidridicarus.common.agentspine;

import kidridicarus.common.agentsensor.SolidContactSensor;

public class OnGroundNerve {
	private SolidContactSensor ogSensor = null;

	public SolidContactSensor createOnGroundSensor() {
		ogSensor = new SolidContactSensor(null);
		return ogSensor;
	}

	public boolean isOnGround() {
		if(ogSensor == null)
			return false;
		return ogSensor.isContactFloor() || ogSensor.isContactAgent();
	}
}
