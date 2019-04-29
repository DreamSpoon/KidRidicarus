package kidridicarus.common.agentspine;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.common.agentsensor.SolidContactSensor;
import kidridicarus.common.tool.Direction4;

public class SolidContactNerve {
	private SolidContactSensor solidSensor = null;

	public SolidContactSensor createSolidContactSensor(AgentBody body) {
		solidSensor = new SolidContactSensor(body);
		return solidSensor;
	}

	public boolean isDirSolid(Direction4 dir, Rectangle bounds) {
		if(solidSensor == null) {
			return false;
		}
		return solidSensor.isDirSolid(dir, bounds);
	}

	// convenience method
	public boolean isOnGround(Rectangle bounds) {
		return isDirSolid(Direction4.DOWN, bounds);
	}

	public boolean isOnCeiling(Rectangle bounds) {
		return isDirSolid(Direction4.UP, bounds);
	}
}
