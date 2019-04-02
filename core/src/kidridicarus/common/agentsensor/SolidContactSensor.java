package kidridicarus.common.agentsensor;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.agentcontact.AgentContactSensor;
import kidridicarus.common.metaagent.tiledmap.solidlayer.SolidLineSeg;

public class SolidContactSensor extends AgentContactSensor {
	private LinkedList<SolidLineSeg> lineSegContacts;
	private LinkedList<Agent> agentContacts;

	public SolidContactSensor(Object parent) {
		super(parent);
		lineSegContacts = new LinkedList<SolidLineSeg>();
		agentContacts = new LinkedList<Agent>();
	}

	@Override
	public void onBeginSense(AgentBodyFilter abf) {
		Agent agent = AgentBodyFilter.getAgentFromFilter(abf);
		if(agent != null) {
			if(!agentContacts.contains(agent))
				agentContacts.add(agent);
		}
		else if(abf.userData instanceof SolidLineSeg) {
			SolidLineSeg ls = (SolidLineSeg) abf.userData;
			if(!lineSegContacts.contains(ls))
				lineSegContacts.add(ls);
		}
	}

	@Override
	public void onEndSense(AgentBodyFilter abf) {
		Agent agent = AgentBodyFilter.getAgentFromFilter(abf);
		if(agent != null) {
			if(agentContacts.contains(agent))
				agentContacts.remove(agent);
		}
		else if(abf.userData instanceof SolidLineSeg) {
			SolidLineSeg ls = (SolidLineSeg) abf.userData;
			if(lineSegContacts.contains(ls))
				lineSegContacts.remove(ls);
		}
	}

	private List<SolidLineSeg> getLineSegsFiltered(boolean filterHorV, boolean isHorizontal, boolean filterUpNormal,
			boolean upNormal) {
		List<SolidLineSeg> list = new LinkedList<SolidLineSeg>();
		for(SolidLineSeg ls : lineSegContacts) {
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

	public boolean isContactCeiling() {
		// return true if the contacts list contains at least 1 ceiling
		return !getLineSegsFiltered(true, true, true, false).isEmpty();
	}

	public boolean isSolidOnThisSide(Rectangle testBounds, boolean rightSide) {
		return isLineSegOnThisSide(testBounds, rightSide) || isAgentOnThisSide(testBounds, rightSide);
	}

	private boolean isLineSegOnThisSide(Rectangle testBounds, boolean rightSide) {
		Vector2 center = testBounds.getCenter(new Vector2());
		// loop through list of walls contacted
		for(SolidLineSeg line : getLineSegsFiltered(true, false, false, false)) {
			// Check for actual bound contact, not just close call...
			// to know if this bound is blocking just a teensy bit or a large amount
			if(line.dblCheckContact(testBounds)) {
				// If moving right and there is a right wall on the right then return blocked true, or
				// if moving left and there is a left wall on the left then return blocked true.
				if((rightSide && !line.upNormal && center.x <= line.getBounds().x) ||
						(!rightSide && line.upNormal && center.x >= line.getBounds().x))
					return true;
			}
		}
		return false;
	}

	private boolean isAgentOnThisSide(Rectangle testBounds, boolean rightSide) {
		for(Agent agent : agentContacts) {
			Rectangle otherBounds = agent.getBounds();
			// if other is too far above or below testBounds then no contact
			if(otherBounds.y >= testBounds.y+testBounds.height ||
					otherBounds.y+otherBounds.height <= testBounds.y)
				continue;
			// If testing for right side and center x of other is on right then return true, or
			// if testing for left side and center x of other is on left then return true.
			if((rightSide && otherBounds.x+otherBounds.width/2f > testBounds.x+testBounds.width/2f) ||
					(!rightSide && otherBounds.x+otherBounds.width/2f < testBounds.x+testBounds.width/2f))
				return true;
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
