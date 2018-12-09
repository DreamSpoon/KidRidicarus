package kidridicarus.bodies;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.bodies.SMB.PipeWarpBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.tiles.InteractiveTileObject;

public interface PlayerBody {
	public Vector2 getPosition();
	public Rectangle getBounds();

	public void onFootTouchBound(LineSeg seg);
	public void onFootLeaveBound(LineSeg seg);
	public void onTouchRobot(RobotBody robotBody);
	public void onTouchItem(RobotBody robotBody);
	public void onHeadTileContactStart(InteractiveTileObject thing);
	public void onHeadTileContactEnd(InteractiveTileObject thing);
	public float getStateTimer();
	public void onStartTouchPipe(PipeWarpBody pipeEnt);
	public void onEndTouchPipe(PipeWarpBody pipeEnt);
	public void onTouchDespawn();
}
