package kidridicarus.common.agentspine;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentsensor.SolidContactSensor;

public class HorizontalMoveNerve {
	private SolidContactSensor horizontalMoveSensor = null;

	public SolidContactSensor createHorizontalMoveSensor(AgentBody body) {
		horizontalMoveSensor = new SolidContactSensor(body);
		return horizontalMoveSensor;
	}

	public boolean isSolidOnThisSide(Rectangle bounds, boolean moveRight) {
		if(horizontalMoveSensor == null)
			return false;
		return horizontalMoveSensor.isSolidOnThisSide(bounds, moveRight);
	}
}
