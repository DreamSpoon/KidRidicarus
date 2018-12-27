package kidridicarus.agent.bodies.sensor;

import kidridicarus.agent.bodies.CrawlAgentBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.GameInfo.DiagonalDir4;

public class CrawlSensor implements LineSegContactSensor {
	private CrawlAgentBody body;
	private DiagonalDir4 quad;

	public CrawlSensor(CrawlAgentBody caBody, DiagonalDir4 quad) {
		this.body = caBody;
		this.quad = quad;
	}

	public void onBeginContact(LineSeg lineSeg) {
		body.onBeginContactWall(quad, lineSeg);
	}

	public void onEndContact(LineSeg lineSeg) {
		body.onEndContactWall(quad, lineSeg);
	}
}
