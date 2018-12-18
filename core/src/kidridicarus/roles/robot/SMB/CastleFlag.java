package kidridicarus.roles.robot.SMB;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.UInfo;
import kidridicarus.roles.RobotRole;
import kidridicarus.sprites.SMB.CastleFlagSprite;
import kidridicarus.worldrunner.RobotRoleDef;
import kidridicarus.worldrunner.RoleWorld;

public class CastleFlag implements RobotRole {
	private enum CastleFlagState { DOWN, RISING, UP};
	private static final float RISE_DIST = UInfo.P2M(32);
	private static final float RISE_TIME = 1f;
	private static final float BODY_WIDTH = UInfo.P2M(16f);
	private static final float BODY_HEIGHT = UInfo.P2M(16f);

	private MapProperties properties;
	private RoleWorld runner;
	private CastleFlagSprite flagSprite;
	private Vector2 startPosition;
	private boolean isTriggered;
	private CastleFlagState curState;
	private float stateTimer;

	public CastleFlag(RoleWorld runner, RobotRoleDef rdef) {
		this.runner = runner;
		properties = rdef.properties;

		startPosition = new Vector2(UInfo.P2M(rdef.bounds.getX() + rdef.bounds.getWidth() / 2f),
				UInfo.P2M(rdef.bounds.getY() + rdef.bounds.getHeight() / 2f));

		flagSprite = new CastleFlagSprite(runner.getEncapTexAtlas(), startPosition);

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
		return startPosition;
	}

	@Override
	public Rectangle getBounds() {
		// TODO: return actual position of flag, not just start position
		return new Rectangle(startPosition.x - BODY_WIDTH/2f, startPosition.y - BODY_HEIGHT/2f, BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public MapProperties getProperties() {
		return properties;
	}

	@Override
	public void dispose() {
	}
}
