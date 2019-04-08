package kidridicarus.common.agentspine;

import kidridicarus.common.agentsensor.SolidContactSensor;

public class OnCeilingNerve {
	private SolidContactSensor ocSensor = null;

	public SolidContactSensor createOnCeilingSensor() {
		ocSensor = new SolidContactSensor(null);
		return ocSensor;
	}

	public boolean isOnCeiling() {
		return ocSensor.isContactCeiling();
	}
}
