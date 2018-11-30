package com.ridicarus.kid.roles.robot.SMB;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.GameInfo.SpriteDrawOrder;
import com.ridicarus.kid.InfoSMB.PointsAmount;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.sprites.SMB.FloatingPointsSprite;
import com.ridicarus.kid.worldrunner.WorldRunner;

public class FloatingPoints implements RobotRole {
	private static final float FLOAT_TIME = 1f;
	private static final float FLOAT_HEIGHT = GameInfo.P2M(48);

	private WorldRunner runner;
	private FloatingPointsSprite pointsSprite;
	private float stateTimer;
	private Vector2 originalPosition;

	public FloatingPoints(WorldRunner runner, PointsAmount amount, Vector2 position) {
		this.runner = runner;
		this.originalPosition = position;

		pointsSprite = new FloatingPointsSprite(runner.getAtlas(), position, amount);

		stateTimer = 0f;
		runner.enableRobotUpdate(this);
		runner.setRobotDrawLayer(this, SpriteDrawOrder.TOP);
	}

	@Override
	public Vector2 getPosition() {
		return originalPosition;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(originalPosition.x, originalPosition.y, 0f, 0f);
	}

	@Override
	public void onTouchRobot(RobotRole robo) {
	}

	@Override
	public void update(float delta) {
		float yOffset = stateTimer <= FLOAT_TIME ? FLOAT_HEIGHT * stateTimer / FLOAT_TIME : FLOAT_HEIGHT;
		pointsSprite.update(delta, originalPosition.cpy().add(0f, yOffset));
		stateTimer += delta;
		if(stateTimer > FLOAT_TIME)
			runner.removeRobot(this);
	}

	@Override
	public void draw(Batch batch){
		pointsSprite.draw(batch);
	}

	@Override
	public void dispose() {
	}

	@Override
	public void setActive(boolean b) {
	}
}
