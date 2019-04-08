package kidridicarus.common.agentspine;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentsensor.SolidContactSensor;

/*
 * Floor (on-ground) and Wall (right/left boundary) Spine
 */
public class CeilingWallContactSpine extends WallContactSpine {
	private OnCeilingNerve ocNerve;

	public CeilingWallContactSpine(AgentBody body) {
		super(body);
		ocNerve = new OnCeilingNerve();
	}

	public SolidContactSensor createOnCeilingSensor() {
		return ocNerve.createOnCeilingSensor();
	}

	public boolean isOnCeiling() {
		return ocNerve.isOnCeiling();
	}
}
