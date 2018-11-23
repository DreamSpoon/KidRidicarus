package com.ridicarus.kid.roles;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.box2d.Body;
import com.ridicarus.kid.collisionmap.LineSeg;
import com.ridicarus.kid.tiles.InteractiveTileObject;

public interface PlayerRole {
	void update(float delta);
	void draw(Batch batch);

	Body getB2Body();
	float getStateTimer();
	boolean isDead();

	void rightIt();
	void leftIt();
	void downIt();
	void runIt();
	void jumpIt();

	void onFootTouchBound(LineSeg seg);
	void onFootLeaveBound(LineSeg seg);
	void onTouchRobot(RobotRole robo);
	void onHeadHit(InteractiveTileObject thing);
	void onTouchItem(RobotRole robo);
}
