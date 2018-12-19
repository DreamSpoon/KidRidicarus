package kidridicarus.agent.bodies;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agent.bodies.SMB.PipeWarpBody;
import kidridicarus.collisionmap.LineSeg;

public interface PlayerBody {
	public Vector2 getPosition();
	public Rectangle getBounds();
	public float getStateTimer();

	public void onFootBeginContactBound(LineSeg seg);
	public void onFootEndContactBound(LineSeg seg);
	public void onContactAgent(AgentBody agentBody);
	public void onContactItem(AgentBody agentBody);
	public void onHeadTileContactStart(AgentBody agentBody);
	public void onHeadTileContactEnd(AgentBody agentBody);
	public void onBeginContactPipe(PipeWarpBody pipeEnt);
	public void onEndContactPipe(PipeWarpBody pipeEnt);
	public void onContactDespawn();
}
