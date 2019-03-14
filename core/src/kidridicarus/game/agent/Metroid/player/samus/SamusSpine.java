package kidridicarus.game.agent.Metroid.player.samus;

import java.util.List;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.SolidBoundSensor;
import kidridicarus.common.agentspine.OnGroundSpine;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.agent.SMB.other.pipewarp.PipeWarp;

public class SamusSpine extends OnGroundSpine {
	private static final Vector2 DAMAGE_KICK_SIDE_IMP = new Vector2(1.8f, 0f);
	private static final Vector2 DAMAGE_KICK_UP_IMP = new Vector2(0f, 1.3f);

	private SamusBody body;
	private AgentContactHoldSensor acSensor;
	private SolidBoundSensor sbSensor;
	private AgentContactHoldSensor pwSensor;	// pipe warp sensor

	public SamusSpine(SamusBody body) {
		this.body = body;
		acSensor = null;
		sbSensor = null;
		pwSensor = null;
	}

	public SolidBoundSensor createSolidBodySensor() {
		sbSensor = new SolidBoundSensor(body);
		return sbSensor;
	}

	public AgentContactHoldSensor creatAgentContactSensor() {
		acSensor = new AgentContactHoldSensor(body);
		return acSensor;
	}

	public AgentContactHoldSensor createPipeWarpSensor() {
		pwSensor = new AgentContactHoldSensor(body);
		return pwSensor;
	}

	public void applyDamageKick(Vector2 position) {
		// zero the y velocity
		body.setVelocity(body.getVelocity().x, 0);
		// apply a kick impulse to the left or right depending on other agent's position
		if(body.getPosition().x < position.x)
			body.applyBodyImpulse(DAMAGE_KICK_SIDE_IMP.cpy().scl(-1f));
		else
			body.applyBodyImpulse(DAMAGE_KICK_SIDE_IMP);

		// apply kick up impulse if the player is above the other agent
		if(body.getPosition().y > position.y)
			body.applyBodyImpulse(DAMAGE_KICK_UP_IMP);
	}

	/*
	 * Checks pipe warp sensors for a contacting pipe warp with entrance direction matching adviceDir, and
	 * returns pipe warp if found. Returns null otherwise. 
	 */
	public PipeWarp getPipeWarpForAdvice(Direction4 adviceDir) {
		for(PipeWarp pw : pwSensor.getContactsByClass(PipeWarp.class)) {
			if(((PipeWarp) pw).canBodyEnterPipe(body.getBounds(), adviceDir))
				return (PipeWarp) pw;
		}
		return null;
	}

	public <T> List<T> getContactsByClass(Class<T> cls) {
		return acSensor.getContactsByClass(cls);
	}

	public <T> T getFirstContactByClass(Class<T> cls) {
		return acSensor.getFirstContactByClass(cls);
	}

	public boolean isContactingWall(boolean isRightWall) {
		return sbSensor.isHMoveBlocked(body.getBounds(), isRightWall);
	}

	public RoomBox getCurrentRoom() {
		return (RoomBox) acSensor.getFirstContactByClass(RoomBox.class);
	}
}
