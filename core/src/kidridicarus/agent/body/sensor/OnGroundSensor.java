package kidridicarus.agent.body.sensor;

import java.util.LinkedList;

import kidridicarus.agency.contact.AgentBodyFilter;
import kidridicarus.collisionmap.LineSeg;

/*
 * Title: On Ground Sensor
 * Desc: Track contacts with solid boundaries that are floors.
 */
public class OnGroundSensor extends ContactSensor {
	private LinkedList<LineSeg> contacts;

	public OnGroundSensor() {
		contacts = new LinkedList<LineSeg>();
	}
	
	@Override
	public void onBeginSense(AgentBodyFilter obj) {
		 if(obj.userData instanceof LineSeg) {
			LineSeg ls = (LineSeg) obj.userData;
			if(ls.isHorizontal && ls.upNormal && !contacts.contains(ls))
				contacts.add(ls);
		}
	}

	@Override
	public void onEndSense(AgentBodyFilter obj) {
		 if(obj.userData instanceof LineSeg) {
			LineSeg ls = (LineSeg) obj.userData;
			if(ls.isHorizontal && ls.upNormal && contacts.contains(ls))
				contacts.remove(ls);
		}
	}

	public boolean isOnGround() {
		return contacts.size() > 0;
	}

	@Override
	public Object getParent() {
		// TODO: implement this method
		return null;
	}
}
