package kidridicarus.agency.agent.body.sensor;

/*
 * Title: On Ground Sensor
 * Desc: Track contacts with solid boundaries that are floors.
 */
public class OnGroundSensor extends SolidBoundSensor {
	public OnGroundSensor(Object parent) {
		super(parent);
	}

	public boolean isOnGround() {
		// return true if the contacts list contains at least 1 floor
		return !getContactsFiltered(true, true, true, true).isEmpty();
	}
}
