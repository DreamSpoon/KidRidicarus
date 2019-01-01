package kidridicarus.agent.body.sensor;

import java.util.LinkedList;

import kidridicarus.agency.contact.AgentBodyFilter;
import kidridicarus.collisionmap.LineSeg;

/*
 * Crawl sensor can contact horizontal and vertical LineSegs.
 */
public class CrawlSensor extends ContactSensor {
	private LinkedList<LineSeg> contacts;

	public CrawlSensor() {
		contacts = new LinkedList<LineSeg>();
	}

	@Override
	public void onBeginSense(AgentBodyFilter obj) {
		if(obj.userData instanceof LineSeg && !contacts.contains(obj.userData))
			contacts.add((LineSeg) obj.userData);
	}

	@Override
	public void onEndSense(AgentBodyFilter obj) {
		if(obj.userData instanceof LineSeg && contacts.contains(obj.userData))
			contacts.remove((LineSeg) obj.userData);
	}

	public boolean isContacting() {
		return contacts.size() > 0;
	}

	@Override
	public Object getParent() {
		// TODO: implements this method
		return null;
	}
}
