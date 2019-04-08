package kidridicarus.game.agent.KidIcarus.player.pitarrow;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentsensor.SolidContactSensor;
import kidridicarus.common.agentspine.BasicAgentSpine;
import kidridicarus.common.agentspine.HorizontalMoveNerve;

public class PitArrowSpine extends BasicAgentSpine {
	private HorizontalMoveNerve hmNerve;
	public PitArrowSpine(AgentBody body) {
		super(body);
		hmNerve = new HorizontalMoveNerve();
	}

	public SolidContactSensor createHorizontalMoveSensor() {
		return hmNerve.createHorizontalMoveSensor(body);
	}

	public boolean isSolidOnThisSide(Rectangle bounds, boolean moveRight) {
		return hmNerve.isSolidOnThisSide(bounds, moveRight);
	}

	public boolean isHitBound(boolean isRight) {
		return hmNerve.isSolidOnThisSide(body.getBounds(), isRight);
	}
}
