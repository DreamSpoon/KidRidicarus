package kidridicarus.agent.bodies.sensor;

import kidridicarus.collisionmap.LineSeg;

/*
 * An Agent's sensor, rather than a sensor for detecting agents.
 * Intended to be used for foot sensors for detecting isOnGround, crawl sensors for detecting boundaries, etc.
 */
public interface AgentSensor {
	public abstract void onBeginContact(LineSeg lineSeg);
	public abstract void onEndContact(LineSeg lineSeg);
}
