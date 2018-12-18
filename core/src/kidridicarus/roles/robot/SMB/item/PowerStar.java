package kidridicarus.roles.robot.SMB.item;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.bodies.SMB.PowerStarBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.SMBInfo.PowerupType;
import kidridicarus.info.UInfo;
import kidridicarus.roles.PlayerRole;
import kidridicarus.roles.SimpleWalkRobotRole;
import kidridicarus.roles.player.MarioRole;
import kidridicarus.roles.robot.BumpableBot;
import kidridicarus.roles.robot.ItemBot;
import kidridicarus.sprites.SMB.PowerStarSprite;
import kidridicarus.worldrunner.RobotRoleDef;
import kidridicarus.worldrunner.RoleWorld;

/*
 * TODO:
 * -allow the star to spawn down-right out of bricks like on level 1-1
 * -test the star's onBump method - I could not bump it, needs precise timing - maybe loosen the timing? 
 */
public class PowerStar extends SimpleWalkRobotRole implements ItemBot, BumpableBot {
	private static final float SPROUT_TIME = 0.5f;
	private static final Vector2 START_BOUNCE_VEL = new Vector2(0.5f, 2f); 
	private static final float SPROUT_OFFSET = UInfo.P2M(-13f);
	private enum StarState { SPROUT, WALK };

	private MapProperties properties;
	private RoleWorld runner;
	private PowerStarBody starBody;
	private PowerStarSprite starSprite;

	private float stateTimer;
	private StarState prevState;

	public PowerStar(RoleWorld runner, RobotRoleDef rdef) {
		properties = rdef.properties;
		this.runner = runner;

		setConstVelocity(START_BOUNCE_VEL);

		prevState = StarState.SPROUT;
		stateTimer = 0f;

		Vector2 position = rdef.bounds.getCenter(new Vector2());
		starSprite = new PowerStarSprite(runner.getEncapTexAtlas(), position.add(0f, SPROUT_OFFSET));
		starBody = new PowerStarBody(this, runner.getWorld(), position);

		runner.enableRobotUpdate(this);
		runner.setRobotDrawLayer(this, SpriteDrawOrder.BOTTOM);
	}

	private StarState getState() {
		// still sprouting?
		if(prevState == StarState.SPROUT && stateTimer <= SPROUT_TIME)
			return StarState.SPROUT;
		else
			return StarState.WALK;
	}

	@Override
	public void update(float delta) {
		float yOffset = 0f;
		StarState curState = getState();
		switch(curState) {
			case WALK:
				// start bounce to the right if this is first time walking
				if(prevState == StarState.SPROUT) {
					starBody.applyImpulse(START_BOUNCE_VEL);
					break;
				}

				// clamp y velocity and maintain steady x velocity
				if(starBody.getVelocity().y > getConstVelocity().y)
					starBody.setVelocity(getConstVelocity().x, getConstVelocity().y);
				else if(starBody.getVelocity().y < -getConstVelocity().y)
					starBody.setVelocity(getConstVelocity().x, -getConstVelocity().y);
				else
					starBody.setVelocity(getConstVelocity().x, starBody.getVelocity().y);
				break;
			case SPROUT:
				if(stateTimer > SPROUT_TIME)
					runner.setRobotDrawLayer(this, SpriteDrawOrder.MIDDLE);
				else
					yOffset = SPROUT_OFFSET * (SPROUT_TIME - stateTimer) / SPROUT_TIME;
				break;
		}

		starSprite.update(delta, starBody.getPosition().cpy().add(0f, yOffset));

		// increment state timer
		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		prevState = curState;
	}

	@Override
	public void draw(Batch batch){
		starSprite.draw(batch);
	}

	@Override
	public void use(PlayerRole playerRole) {
		if(stateTimer <= SPROUT_TIME)
			return;

		if(playerRole instanceof MarioRole) {
			((MarioRole) playerRole).applyPowerup(PowerupType.POWERSTAR);
			runner.destroyRobot(this);
		}
	}

	public void onTouchBoundLine(LineSeg seg) {
		// bounce off of vertical bounds only
		if(!seg.isHorizontal)
			reverseConstVelocity(true,  false);
	}

	@Override
	public Vector2 getPosition() {
		return starBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return starBody.getBounds();
	}

	@Override
	public void onBump(PlayerRole perp, Vector2 fromCenter) {
		if(stateTimer <= SPROUT_TIME)
			return;

		// if bump came from left and star is moving left then reverse,
		// if bump came from right and star is moving right then reverse
		if((fromCenter.x < starBody.getPosition().x && starBody.getVelocity().x < 0f) ||
			(fromCenter.x > starBody.getPosition().x && starBody.getVelocity().x > 0f))
			reverseConstVelocity(true, false);

		starBody.setVelocity(getConstVelocity().x, getConstVelocity().y);
	}

	@Override
	public MapProperties getProperties() {
		// return empty properties
		return properties;
	}

	@Override
	public void dispose() {
		starBody.dispose();
	}
}
