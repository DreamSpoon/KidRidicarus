package kidridicarus.agent.bodies;

import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.GameInfo.DiagonalDir4;

public abstract class CrawlAgentBody extends MobileAgentBody {
	public abstract void onBeginContactWall(DiagonalDir4 quad, LineSeg lineSeg);
	public abstract void onEndContactWall(DiagonalDir4 quad, LineSeg lineSeg);
}
