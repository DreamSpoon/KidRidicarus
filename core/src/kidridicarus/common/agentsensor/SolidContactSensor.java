package kidridicarus.common.agentsensor;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.agentcontact.AgentContactSensor;
import kidridicarus.common.metaagent.tiledmap.collision.LineSeg;

public class SolidContactSensor extends AgentContactSensor {
	private LinkedList<LineSeg> lineSegContacts;
	private LinkedList<Agent> agentContacts;

	public SolidContactSensor(Object parent) {
		super(parent);
		lineSegContacts = new LinkedList<LineSeg>();
		agentContacts = new LinkedList<Agent>();
	}

	@Override
	public void onBeginSense(AgentBodyFilter obj) {
		if(obj.userData instanceof LineSeg) {
			LineSeg ls = (LineSeg) obj.userData;
			if(!lineSegContacts.contains(ls))
				lineSegContacts.add(ls);
		}
		else if(obj.userData instanceof Agent) {
			Agent agent = (Agent) obj.userData;
			if(!agentContacts.contains(agent))
				agentContacts.add(agent);
		}
	}

	@Override
	public void onEndSense(AgentBodyFilter obj) {
		if(obj.userData instanceof LineSeg) {
			LineSeg ls = (LineSeg) obj.userData;
			if(lineSegContacts.contains(ls))
				lineSegContacts.remove(ls);
		}
		else if(obj.userData instanceof Agent) {
			Agent agent = (Agent) obj.userData;
			if(agentContacts.contains(agent))
				agentContacts.remove(agent);
		}
	}

	private List<LineSeg> getLineSegsFiltered(boolean filterHorV, boolean isHorizontal, boolean filterUpNormal,
			boolean upNormal) {
		List<LineSeg> list = new LinkedList<LineSeg>();
		for(LineSeg ls : lineSegContacts) {
			if(filterHorV && ls.isHorizontal != isHorizontal)
				continue;
			if(filterUpNormal && ls.upNormal != upNormal)
				continue;
			list.add(ls);
		}
		return list;
	}

	public boolean isContactFloor() {
		// return true if the contacts list contains at least 1 floor
		return !getLineSegsFiltered(true, true, true, true).isEmpty();
	}

	public boolean isContactWall(Rectangle testBounds, boolean rightWall) {
		Vector2 center = testBounds.getCenter(new Vector2());
		// loop through list of walls contacted
		for(LineSeg line : getLineSegsFiltered(true, false, false, false)) {
			// Check for actual bound contact, not just close call...
			// to know if this bound is blocking just a teensy bit or a large amount
			if(line.dblCheckContact(testBounds)) {
				// if moving right and there is a right wall on the right then return blocked true
				if(rightWall && !line.upNormal && center.x <= line.getBounds().x)
					return true;
				// if moving left and there is a left wall on the left then return blocked true
				else if(!rightWall && line.upNormal && center.x >= line.getBounds().x)
					return true;
			}
		}
		return false;
	}

	public boolean isContactAgent() {
		return !agentContacts.isEmpty(); 
	}

	public boolean isContacting() {
		return !agentContacts.isEmpty() || !lineSegContacts.isEmpty();
	}
}
