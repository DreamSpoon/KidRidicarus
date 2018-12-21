package kidridicarus.agencydirector;

import kidridicarus.collisionmap.LineSeg;

public interface AgentSensor {
	public abstract void onBeginContact(LineSeg lineSeg);
	public abstract void onEndContact(LineSeg lineSeg);
}
