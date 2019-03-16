package kidridicarus.game.agent.SMB.player.luigi;

import java.util.List;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.SolidBoundSensor;

public class LuigiFireballSpine {
	private LuigiFireballBody body;
	private SolidBoundSensor hmSensor;
	private AgentContactHoldSensor acSensor;

	public LuigiFireballSpine(LuigiFireballBody body) {
		this.body = body;
	}

	public SolidBoundSensor createHMSensor() {
		hmSensor = new SolidBoundSensor(body);
		return hmSensor;
	}

	public AgentContactHoldSensor createAgentContactSensor() {
		acSensor = new AgentContactHoldSensor(this);
		return acSensor;
	}

	public List<Agent> getContactAgents() {
		return acSensor.getContacts();
	}

	public <T> List<T> getContactAgentsByClass(Class<T> cls) {
		return acSensor.getContactsByClass(cls);
	}

	public boolean isHitBoundary(boolean facingRight) {
		if(hmSensor.isHMoveBlocked(body.getBounds(), facingRight) || (body.getVelocity().x <= 0f && facingRight) ||
				(body.getVelocity().x >= 0f && !facingRight))
			return true;
		return false;
	}

	public void startExplode() {
		body.setMainSolid(false);
		body.setAgentSensorEnabled(false);
		body.setVelocity(0f, 0f);
		body.setGravityScale(0f);
	}
}
