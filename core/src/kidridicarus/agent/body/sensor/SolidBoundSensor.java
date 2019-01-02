package kidridicarus.agent.body.sensor;

import java.util.LinkedList;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.contact.AgentBodyFilter;
import kidridicarus.collisionmap.LineSeg;

public class SolidBoundSensor extends ContactSensor {
	private LinkedList<LineSeg> contacts;

	public SolidBoundSensor(Object parent) {
		super(parent);
		contacts = new LinkedList<LineSeg>();
	}

	@Override
	public void onBeginSense(AgentBodyFilter obj) {
		 if(obj.userData instanceof LineSeg) {
			LineSeg ls = (LineSeg) obj.userData;
			if(!contacts.contains(ls))
				contacts.add(ls);
		}
	}

	@Override
	public void onEndSense(AgentBodyFilter obj) {
		 if(obj.userData instanceof LineSeg) {
			LineSeg ls = (LineSeg) obj.userData;
			if(contacts.contains(ls))
				contacts.remove(ls);
		}
	}

	public LinkedList<LineSeg> getContacts() {
		return contacts;
	}

	public LinkedList<LineSeg> getContactsFiltered(boolean filterHorV, boolean isHorizontal, boolean filterUpNormal,
			boolean upNormal) {
		LinkedList<LineSeg> list = new LinkedList<LineSeg>();
		for(LineSeg ls : contacts) {
			if(filterHorV && ls.isHorizontal != isHorizontal)
				continue;
			if(filterUpNormal && ls.upNormal != upNormal)
				continue;
			list.add(ls);
		}
		return list;
	}

	public boolean isHMoveBlocked(Rectangle bounds, boolean moveRight) {
		Vector2 center = bounds.getCenter(new Vector2());
		// loop through list of walls contacted
		for(LineSeg line : getContactsFiltered(true, false, false, false)) {
			// Check for actual bound contact, not just close call...
			// to know if this bound is blocking just a teensy bit or a large amount
			if(line.dblCheckContact(bounds)) {
				// if moving right and there is a right wall on the right then return blocked true
				if(moveRight && !line.upNormal && center.x < line.getBounds().x)
					return true;
				// if moving left and there is a left wall on the left then return blocked true
				else if(!moveRight && line.upNormal && center.x > line.getBounds().x)
					return true;
			}
		}
		return false;
	}
}
