package kidridicarus.agent.bodies.sensor;

import kidridicarus.agent.bodies.Metroid.enemy.ZoomerBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.GameInfo.DiagonalDir4;

public class CrawlSensor implements AgentSensor {
	private ZoomerBody zBody;
	private DiagonalDir4 quad;

	public CrawlSensor(ZoomerBody zBody, DiagonalDir4 quad) {
		this.zBody = zBody;
		this.quad = quad;
	}

	public void onBeginContact(LineSeg lineSeg) {
		zBody.onBeginContactWall(quad, lineSeg);
	}

	public void onEndContact(LineSeg lineSeg) {
		zBody.onEndContactWall(quad, lineSeg);
	}
}
