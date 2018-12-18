package kidridicarus.roles.robot.SMB;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.info.AudioInfo;
import kidridicarus.info.KVInfo;
import kidridicarus.info.SMBInfo;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.SMBInfo.PointAmount;
import kidridicarus.info.UInfo;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.player.MarioRole;
import kidridicarus.sprites.SMB.FloatingPointsSprite;
import kidridicarus.worldrunner.RobotRoleDef;
import kidridicarus.worldrunner.RoleWorld;

/*
 * SMB floating points, and 1-up.
 * 
 * Relative vs Absolute Points Notes:
 *   This distinction is necessary due to the way mario can gain points.
 * i.e. The sliding turtle shell points multiplier, and the consecutive head bounce multiplier.
 * 
 * The sliding turtle shell awards only absolute points, and head bounces award only relative points.
 * Currently, mario fireball strikes award only absolute points.
 */
public class FloatingPoints implements RobotRole {
	private static final float FLOAT_TIME = 1f;
	private static final float FLOAT_HEIGHT = UInfo.P2M(48);

	private MapProperties properties;
	private RoleWorld runner;
	private FloatingPointsSprite pointsSprite;
	private float stateTimer;
	private Vector2 originalPosition;

	public FloatingPoints(RoleWorld runner, RobotRoleDef rdef) {
		properties = rdef.properties;
		this.runner = runner;
		originalPosition = rdef.bounds.getCenter(new Vector2());

		// default to zero points
		PointAmount amount = PointAmount.ZERO;
		// check for point amount property
		if(rdef.properties.containsKey(KVInfo.KEY_POINTAMOUNT))
			amount = SMBInfo.strToPointAmount(rdef.properties.get(KVInfo.KEY_POINTAMOUNT, String.class));
		// relative points can stack, absolute points can not
		boolean relative = false;
		if(rdef.properties.containsKey(KVInfo.KEY_RELPOINTAMOUNT))
			relative = rdef.properties.get(KVInfo.KEY_POINTAMOUNT, Boolean.class);

		// give points to player and get the actual amount awarded (since player may have points multiplier)
		if(rdef.userData != null) {
			amount = ((MarioRole) rdef.userData).givePoints(amount, relative);
			if(amount == PointAmount.P1UP)
				runner.playSound(AudioInfo.SOUND_1UP);
		}

		pointsSprite = new FloatingPointsSprite(runner.getEncapTexAtlas(), originalPosition, amount);

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
			runner.destroyRobot(this);
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
	public MapProperties getProperties() {
		return properties;
	}
/*
	// add points to mario's total, with option to display the point amount on-screen
	public void givePlayerPoints(PlayerRole playrole, PointAmount amount, boolean visible, Vector2 position,
			float yOffset, boolean isHeadBounce) {
		MarioRole mario;
		PointAmount finalAmt;

		if(playrole == null)
			throw new IllegalArgumentException("Cannot give points to null player.");

		finalAmt = amount;
		if(playrole instanceof MarioRole) {
			mario = (MarioRole) playrole;
			// check for points increase due to mario bouncing multiple times without touching the ground
			if(isHeadBounce) {
				mario.incrementFlyingPoints();
				// if the flying points are greater than the incoming points amount then use the flying points
				if(mario.getFlyingPoints().compareTo(amount) > 0 || mario.getFlyingPoints() == PointAmount.P1UP)
					finalAmt = mario.getFlyingPoints();
			}

			if(finalAmt == PointAmount.P1UP) {
				runner.playSound(AudioInfo.SOUND_1UP);
				mario.give1UP();
			}
			else
				mario.givePoints(finalAmt);
		}

		// if visible then create floating points that despawn after a short time
		if(visible && position != null) {
//			subWR.addRobot(new FloatingPoints(this, finalAmt, position.cpy().add(0f, yOffset)));
			createRobot(RRDefFactory.makeFloatingPoints(this, playrole, finalAmt, position.cpy().add(0f, yOffset)));
		}
	}
*/
	@Override
	public void dispose() {
	}
}
