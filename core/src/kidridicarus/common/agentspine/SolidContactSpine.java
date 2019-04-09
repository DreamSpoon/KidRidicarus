package kidridicarus.common.agentspine;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentsensor.SolidContactSensor;
import kidridicarus.common.tool.Direction4;

public class SolidContactSpine extends BasicAgentSpine {
	private SolidContactNerve scNerve;

	public SolidContactSpine(AgentBody body) {
		super(body);
		scNerve = new SolidContactNerve();
	}

	public SolidContactSensor createSolidContactSensor() {
		return scNerve.createSolidContactSensor(body);
	}

	public boolean isOnGround() {
		return scNerve.isOnGround(body.getBounds());
	}

	public boolean isOnCeiling() {
		return scNerve.isOnCeiling(body.getBounds());
	}

	public boolean isSideMoveBlocked(boolean isRight) {
		if(isRight && scNerve.isDirSolid(Direction4.RIGHT, body.getBounds()))
			return true;
		else if(!isRight && scNerve.isDirSolid(Direction4.LEFT, body.getBounds()))
			return true;
		return false;
	}
}
