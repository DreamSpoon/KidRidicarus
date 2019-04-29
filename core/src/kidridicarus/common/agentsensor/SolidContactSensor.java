package kidridicarus.common.agentsensor;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBodyFilter;
import kidridicarus.agency.agentbody.AgentContactSensor;
import kidridicarus.common.agent.optional.SolidAgent;
import kidridicarus.common.metaagent.tiledmap.solidlayer.SolidLineSeg;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;

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
		if(abf.userData instanceof SolidLineSeg) {
			if(!lineSegContacts.contains(abf.userData))
				lineSegContacts.add((SolidLineSeg) abf.userData);
			return;
		}

		Agent agent = AgentBodyFilter.getAgentFromFilter(abf);
		if(agent instanceof SolidAgent && !agentContacts.contains(agent))
			agentContacts.add(agent);
	}

	@Override
	public void onEndSense(AgentBodyFilter abf) {
		if(abf.userData instanceof SolidLineSeg) {
			if(lineSegContacts.contains(abf.userData))
				lineSegContacts.remove(abf.userData);
			return;
		}

		Agent agent = AgentBodyFilter.getAgentFromFilter(abf);
		if(agent instanceof SolidAgent && agentContacts.contains(agent))
			agentContacts.remove(agent);
	}

	/*
	 * If filterHorV = true then filter based on whether lineSeg is horizontal or not, using isHorizontal
	 * to do the filtering.
	 * If filterUpNormal = false then filter based on whether lineSeg's upnormal is true or false, using upNormal
	 * to do the filtering.
	 *
	 * Example filters, to filter for exactly these things:
	 *   Floor (solid below, empty space above):
	 *     filterHorV=true
	 *     isHorizontal=true
	 *     filterUpNormal=true
	 *     upNormal=true
	 *
	 *   Ceiling (solid above, empty space below):
	 *     filterHorV=true
	 *     isHorizontal=true
	 *     filterUpNormal=true
	 *     upNormal=false
	 *
	 *   Right Wall (solid on right, empty space on left):
	 *     filterHorV=true
	 *     isHorizontal=false
	 *     filterUpNormal=true
	 *     upNormal=false
	 *
	 *   Left Wall (solid on left, empty space on right):
	 *     filterHorV=true
	 *     isHorizontal=false
	 *     filterUpNormal=true
	 *     upNormal=true
	 */
	private List<SolidLineSeg> getLineSegsFiltered(boolean filterHorV, boolean isHorizontal,
			boolean filterUpNormal, boolean upNormal) {
		List<SolidLineSeg> list = new LinkedList<SolidLineSeg>();
		for(SolidLineSeg ls : lineSegContacts) {
			// If filtering on property isHorizontal then compare line isHorizontal to filter value and continue
			// if line fails filter, or
			// If filtering on property upNormal then compare line upNormal to filter value and continue
			// if line fails filter.
			if((filterHorV && ls.isHorizontal != isHorizontal) ||
					(filterUpNormal && ls.upNormal != upNormal))
				continue;
			list.add(ls);
		}
		return list;
	}

	public boolean isContacting() {
		return !agentContacts.isEmpty() || !lineSegContacts.isEmpty();
	}

	public boolean isDirSolid(Direction4 dir, Rectangle testBounds) {
		return isDirBlockedByLineSeg(dir, testBounds) || isDirBlockedByAgent(dir, testBounds);
	}

	private boolean isDirBlockedByLineSeg(Direction4 dir, Rectangle testBounds) {
		if(dir == null || dir == Direction4.NONE)
			throw new IllegalArgumentException("dir must be non null and not NONE, but dir="+dir);
		Vector2 center = testBounds.getCenter(new Vector2());
		// loop through list of line segs contacted based on floor/ceiling or left-wall/right-wall
		List<SolidLineSeg> lineSegs;
		switch(dir) {
			case RIGHT:
				lineSegs = getLineSegsFiltered(true, false, true, false);
				break;
			case LEFT:
				lineSegs = getLineSegsFiltered(true, false, true, true);
				break;
			case UP:
				lineSegs = getLineSegsFiltered(true, true, true, false);
				break;
			case DOWN:
			default:
				lineSegs = getLineSegsFiltered(true, true, true, true);
				break;
		}
		for(SolidLineSeg line : lineSegs) {
			// Check for actual bound contact, not just close call...
			// to know if this bound is blocking just a teensy bit or a large amount
			if(!line.dblCheckContact(testBounds))
				continue;
			// If moving right and there is a right wall on the right then return blocked true, or
			// if moving left and there is a left wall on the left then return blocked true.
			switch(dir) {
				case RIGHT:
					if(!line.upNormal && center.x <= line.getBounds().x)
						return true;
					break;
				case LEFT:
					if(line.upNormal && center.x >= line.getBounds().x)
						return true;
					break;
				case UP:
					if(!line.upNormal && center.y <= line.getBounds().y)
						return true;
					break;
				case DOWN:
				default:
					if(line.upNormal && center.y >= line.getBounds().y)
						return true;
					break;
			}
		}
		return false;
	}

	private boolean isDirBlockedByAgent(Direction4 dir, Rectangle testBounds) {
		if(dir == null || dir == Direction4.NONE)
			throw new IllegalArgumentException("dir must be non null and not NONE, but dir="+dir);
		for(Agent agent : agentContacts) {
			// Get other Agent's bounds, skipping other Agent if bounds are null. If other is too far above or
			// below testBounds then skip Agent because contact is impossible.
			Rectangle otherBounds = AP_Tool.getBounds(agent);
			if(otherBounds == null || (dir.isHorizontal() && (otherBounds.y >= testBounds.y+testBounds.height ||
					otherBounds.y+otherBounds.height <= testBounds.y)) ||
				(dir.isVertical() && (otherBounds.x >= testBounds.x+testBounds.width ||
					otherBounds.x+otherBounds.width <= testBounds.x)))
				continue;
			// If testing for right side and center x of other is on right then return true, or
			// if testing for left side and center x of other is on left then return true.
			switch(dir) {
				case RIGHT:
					if(otherBounds.x+otherBounds.width/2f > testBounds.x+testBounds.width/2f)
						return true;
					break;
				case LEFT:
					if(otherBounds.x+otherBounds.width/2f < testBounds.x+testBounds.width/2f)
						return true;
					break;
				case UP:
					if(otherBounds.y+otherBounds.height/2f > testBounds.y+testBounds.height/2f)
						return true;
					break;
				case DOWN:
					if(otherBounds.y+otherBounds.height/2f < testBounds.y+testBounds.height/2f)
						return true;
					break;
				default:
					throw new IllegalStateException("Impossible state, dir already checked for null/NONE");
			}
		}
		return false;
	}
}
