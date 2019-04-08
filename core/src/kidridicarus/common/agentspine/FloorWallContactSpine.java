package kidridicarus.common.agentspine;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentsensor.SolidContactSensor;

/*
 * Floor (on-ground) and Wall (right/left boundary) Spine
 */
public class FloorWallContactSpine extends WallContactSpine {
	private OnGroundNerve ogNerve;

	public FloorWallContactSpine(AgentBody body) {
		super(body);
		ogNerve = new OnGroundNerve();
	}

	public SolidContactSensor createOnGroundSensor() {
		return ogNerve.createOnGroundSensor();
	}

	public boolean isOnGround() {
		return ogNerve.isOnGround();
	}
}
