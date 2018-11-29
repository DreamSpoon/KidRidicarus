package com.ridicarus.kid.roles;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public interface RobotRole {
	// TODO: this is a guess value (0.001f) - test more to refine - may depend upon Pixels Per Meter and Pixels Per Tile
	public static final float ROBOT_VS_VERT_BOUND_EPSILON = 0.001f;

	public Vector2 getPosition();
	public Rectangle getBounds();

	public void onTouchRobot(RobotRole robo);

	public void update(float delta);
	public void draw(Batch batch);
	public void dispose();
	public void setActive(boolean b);
}
