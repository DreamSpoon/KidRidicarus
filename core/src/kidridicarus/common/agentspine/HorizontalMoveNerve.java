package kidridicarus.common.agentspine;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentsensor.SolidContactSensor;

public class HorizontalMoveNerve {
	private SolidContactSensor horizontalMoveSensor;

	public HorizontalMoveNerve() {
		horizontalMoveSensor = null;
	}

	public SolidContactSensor createHorizontalMoveSensor(AgentBody body) {
		horizontalMoveSensor = new SolidContactSensor(body);
		return horizontalMoveSensor;
	}

	public boolean isSolidOnThisSide(Rectangle bounds, boolean moveRight) {
		return horizontalMoveSensor.isSolidOnThisSide(bounds, moveRight);
	}
}
