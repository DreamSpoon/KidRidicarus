package kidridicarus.agent.body.sensor;

import java.util.LinkedList;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.contact.AgentBodyFilter;
import kidridicarus.agent.Agent;
import kidridicarus.collisionmap.LineSeg;

/*
 * Title: Horizontal Move Sensor 
 * Desc: Keeps track of contacts with vertical walls and can check move against contacts.
 */
public class HMoveSensor extends ContactSensor {
	// vertical walls only
	private LinkedList<LineSeg> contactWalls;
	private Agent parent;

	public HMoveSensor(Agent parent) {
		this.parent = parent;
		contactWalls = new LinkedList<LineSeg>();
	}

	@Override
	public void onBeginSense(AgentBodyFilter obj) {
		// add to contacts list if: obj is a vertical solid boundary wall and obj IS NOT in the list of contacts 
		if(obj.userData instanceof LineSeg && !((LineSeg) obj.userData).isHorizontal &&
				!contactWalls.contains(obj.userData))
			contactWalls.add((LineSeg) obj.userData);
	}

	@Override
	public void onEndSense(AgentBodyFilter obj) {
		// remove from contacts list if: obj is a vertical solid boundary wall and obj IS in the list of contacts 
		if(obj.userData instanceof LineSeg && !((LineSeg) obj.userData).isHorizontal &&
				contactWalls.contains(obj.userData))
			contactWalls.remove((LineSeg) obj.userData);
	}

	public boolean isMoveBlocked(Rectangle bounds, boolean moveRight) {
		Vector2 center = bounds.getCenter(new Vector2());
		for(LineSeg line : contactWalls) {
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

	@Override
	public Object getParent() {
		return parent;
	}
}
