package kidridicarus.game.agent.Metroid.player.samus;

import java.util.List;

import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.OnGroundSensor;
import kidridicarus.common.agentsensor.SolidBoundSensor;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.agent.SMB.other.pipewarp.PipeWarp;

public class SamusSpine {
	private AgentContactHoldSensor acSensor;
	private OnGroundSensor ogSensor;
	private SolidBoundSensor sbSensor;
	private AgentContactHoldSensor wpSensor;	// warp pipe sensor
	private SamusBody body;

	public SamusSpine(SamusBody body) {
		this.body = body;
	}

	public SolidBoundSensor createSolidBodySensor() {
		sbSensor = new SolidBoundSensor(body);
		return sbSensor;
	}

	public AgentContactHoldSensor creatAgentContactSensor() {
		acSensor = new AgentContactHoldSensor(body);
		return acSensor;
	}

	public OnGroundSensor createGroundAndPipeSensor() {
		wpSensor = new AgentContactHoldSensor(body);
		ogSensor = new OnGroundSensor(null);
		// the og sensor chains to the wp sensor, because the wp sensor will be attached to other fixtures
		ogSensor.chainTo(wpSensor);

		return ogSensor;
	}

	/*
	 * Returns warp pipe entrance if pipe sensors are contacting a pipe with entrance direction matching adviceDir.
	 * Returns null otherwise. 
	 */
	public PipeWarp getPipeWarpForAdvice(Direction4 adviceDir) {
		for(PipeWarp pw : wpSensor.getContactsByClass(PipeWarp.class)) {
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

	public boolean isOnGround() {
		// return true if the on ground contacts list contains at least 1 floor
		return ogSensor.isOnGround();
	}

	public RoomBox getCurrentRoom() {
		return (RoomBox) acSensor.getFirstContactByClass(RoomBox.class);
	}
}
