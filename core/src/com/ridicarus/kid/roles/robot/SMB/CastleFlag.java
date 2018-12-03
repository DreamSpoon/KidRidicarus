package com.ridicarus.kid.roles.robot.SMB;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.GameInfo.SpriteDrawOrder;
import com.ridicarus.kid.sprites.SMB.CastleFlagSprite;
import com.ridicarus.kid.worldrunner.WorldRunner;
import com.ridicarus.kid.roles.RobotRole;

public class CastleFlag implements RobotRole {
	private enum CastleFlagState { DOWN, RISING, UP};
	private static final float RISE_DIST = GameInfo.P2M(32);
	private static final float RISE_TIME = 1f;

	private WorldRunner runner;
	private CastleFlagSprite flagSprite;
	private Vector2 startPosition;
	private boolean isTriggered;
	private CastleFlagState curState;
	private float stateTimer;

	public CastleFlag(WorldRunner runner, MapObject object) {
		this.runner = runner;

		Rectangle bounds = ((RectangleMapObject) object).getRectangle();
		startPosition = new Vector2(GameInfo.P2M(bounds.getX() + bounds.getWidth() / 2f),
				GameInfo.P2M(bounds.getY() + bounds.getHeight() / 2f));

		flagSprite = new CastleFlagSprite(runner.getAtlas(), startPosition);

		isTriggered = false;
		curState = CastleFlagState.DOWN;
		stateTimer = 0f;

		runner.setRobotDrawLayer(this, SpriteDrawOrder.BOTTOM);
	}

	private CastleFlagState getState() {
		switch(curState) {
			case DOWN:
			default:
				if(isTriggered)
					return CastleFlagState.RISING;
				return CastleFlagState.DOWN;
			case RISING:
				if(stateTimer > RISE_TIME)
					return CastleFlagState.UP;
				return CastleFlagState.RISING;
			case UP:
				return CastleFlagState.UP;
		}
	}

	public void update(float delta) {
		float yOffset;
		CastleFlagState nextState = getState();
		switch(nextState) {
			case DOWN:
			default:
				yOffset = 0f;
				if(isTriggered)
					curState = CastleFlagState.RISING;
				break;
			case RISING:
				if(curState != nextState)
					yOffset = 0f;
				else
					yOffset = RISE_DIST / RISE_TIME * stateTimer;
				break;
			case UP:
				yOffset = RISE_DIST;
				break;
		}
		stateTimer = curState == nextState ? stateTimer+delta : 0f;
		curState = nextState;

		flagSprite.update(startPosition.cpy().add(0f, yOffset));
	}

	public void draw(Batch batch) {
		if(isTriggered)
			flagSprite.draw(batch);
	}

	public void trigger() {
		isTriggered = true;
		runner.enableRobotUpdate(this);
	}

	@Override
	public Vector2 getPosition() {
		return null;
	}

	@Override
	public Rectangle getBounds() {
		return null;
	}

	@Override
	public void onTouchRobot(RobotRole robo) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void setActive(boolean b) {
	}
}
