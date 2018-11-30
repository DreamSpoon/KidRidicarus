package com.ridicarus.kid.bodies;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.ridicarus.kid.collisionmap.LineSeg;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.roles.robot.SMB.PipeEntrance;
import com.ridicarus.kid.tiles.InteractiveTileObject;

public interface PlayerBody {
	public Vector2 getPosition();
	public Rectangle getBounds();

	public void onFootTouchBound(LineSeg seg);
	public void onFootLeaveBound(LineSeg seg);
	public void onTouchRobot(RobotRole robo);
	public void onTouchItem(RobotRole robo);
	public void onHeadHit(InteractiveTileObject thing);
	public float getStateTimer();
	public void onStartTouchPipe(PipeEntrance pipeEnt);
	public void onEndTouchPipe(PipeEntrance pipeEnt);
	public void onTouchDespawn();
}
