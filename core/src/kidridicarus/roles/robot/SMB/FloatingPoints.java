package kidridicarus.roles.robot.SMB;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.GameInfo;
import kidridicarus.GameInfo.SpriteDrawOrder;
import kidridicarus.InfoSMB.PointAmount;
import kidridicarus.roles.RobotRole;
import kidridicarus.sprites.SMB.FloatingPointsSprite;
import kidridicarus.worldrunner.WorldRunner;

/*
 * SMB floating points, and 1-up
 */
public class FloatingPoints implements RobotRole {
	private static final float FLOAT_TIME = 1f;
	private static final float FLOAT_HEIGHT = GameInfo.P2M(48);

	private WorldRunner runner;
	private FloatingPointsSprite pointsSprite;
	private float stateTimer;
	private Vector2 originalPosition;

	public FloatingPoints(WorldRunner runner, PointAmount amount, Vector2 position) {
		this.runner = runner;
		this.originalPosition = position;

		pointsSprite = new FloatingPointsSprite(runner.getAtlas(), position, amount);

		stateTimer = 0f;
		runner.enableRobotUpdate(this);
		runner.setRobotDrawLayer(this, SpriteDrawOrder.TOP);
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
	public Vector2 getPosition() {
		return originalPosition;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(originalPosition.x, originalPosition.y, 0f, 0f);
	}

	@Override
	public void setActive(boolean active) {
	}

	@Override
	public void dispose() {
	}
}
