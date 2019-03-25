package kidridicarus.game.agent.SMB.player.mariofireball;

import java.util.List;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.SolidContactSensor;
import kidridicarus.common.info.UInfo;

public class MarioFireballSpine {
	protected static final Vector2 MOVE_VEL = new Vector2(2.4f, 2.25f);

	private MarioFireballBody body;
	private SolidContactSensor hmSensor;
	private AgentContactHoldSensor acSensor;

	public MarioFireballSpine(MarioFireballBody body) {
		this.body = body;
	}

	public SolidContactSensor createHMSensor() {
		hmSensor = new SolidContactSensor(body);
		return hmSensor;
	}

	public AgentContactHoldSensor createAgentContactSensor() {
		acSensor = new AgentContactHoldSensor(this);
		return acSensor;
	}

	public void doVelocityCheck() {
		// if body is currently at zero Y velocity, and if previously at zero or negative velocity, then bounce up
		if(UInfo.epsCheck(body.getVelocity().y, 0f, UInfo.VEL_EPSILON) &&
				body.getPrevVelocity().y < UInfo.VEL_EPSILON) {
			body.setVelocity(body.getVelocity().x, MOVE_VEL.y);
		}
		// cap up velocity
		else if(body.getVelocity().y > MOVE_VEL.y)
			body.setVelocity(body.getVelocity().x, MOVE_VEL.y);
		// cap down velocity
		else if(body.getVelocity().y < -MOVE_VEL.y)
			body.setVelocity(body.getVelocity().x, -MOVE_VEL.y);
	}

	public List<Agent> getContactAgents() {
		return acSensor.getContacts();
	}

	public List<ContactDmgTakeAgent> getContactDmgTakeAgents() {
		return acSensor.getContactsByClass(ContactDmgTakeAgent.class);
	}

	public boolean isHitBoundary(boolean facingRight) {
		return hmSensor.isSolidOnThisSide(body.getBounds(), facingRight) ||
				(body.getVelocity().x <= 0f && facingRight) ||
				(body.getVelocity().x >= 0f && !facingRight);
	}

	public void startExplode() {
		body.disableAllContacts();
		body.setVelocity(0f, 0f);
		body.setGravityScale(0f);
	}
}
