package kidridicarus.common.agentspine;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentbody.MobileAgentBody;
import kidridicarus.common.agentsensor.SolidContactSensor;
import kidridicarus.common.info.CommonKV;

public class MobileAgentSpine extends BasicAgentSpine {
	private SolidContactSensor ogSensor;

	public MobileAgentSpine(MobileAgentBody body) {
		super(body);
		ogSensor = null;
	}

	public SolidContactSensor createOnGroundSensor() {
		ogSensor = new SolidContactSensor(null);
		return ogSensor;
	}

	public void checkDoSpaceWrap(RoomBox curRoom) {
		if(curRoom == null)
			return;
		if(!curRoom.getProperty(CommonKV.Room.KEY_SPACE_WRAP_H, false, Boolean.class))
			return;
		
		// if body position is outside room on left...
		if(body.getPosition().x < curRoom.getBounds().x) {
			// true because I want keep velocity=true
			((MobileAgentBody) body).setPosition(
					new Vector2(curRoom.getBounds().x+curRoom.getBounds().width, body.getPosition().y), true);
		}
		// if body position is outside room on right...
		else if(body.getPosition().x > curRoom.getBounds().x+curRoom.getBounds().width) {
			// true because I want keep velocity=true
			((MobileAgentBody) body).setPosition(
					new Vector2(curRoom.getBounds().x, body.getPosition().y), true);
		}
	}

	public boolean isOnGround() {
		return ogSensor.isContactFloor() || ogSensor.isContactAgent();
	}
}
