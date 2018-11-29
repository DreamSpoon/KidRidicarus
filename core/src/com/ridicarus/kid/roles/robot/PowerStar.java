package com.ridicarus.kid.roles.robot;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.GameInfo.SpriteDrawOrder;
import com.ridicarus.kid.InfoSMB.PowerupType;
import com.ridicarus.kid.collisionmap.LineSeg;
import com.ridicarus.kid.roles.MobileRobot;
import com.ridicarus.kid.roles.PlayerRole;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.roles.player.MarioRole;
import com.ridicarus.kid.sprites.PowerStarSprite;
import com.ridicarus.kid.tools.WorldRunner;

/*
 * TODO:
 * -allow the star to spawn down-right out of bricks like on level 1-1
 * -test the star's onBump method - I could not bump it, needs precise timing - maybe loosen the timing? 
 */
public class PowerStar extends MobileRobot implements ItemBot, BumpableBot {
	private static final float BODY_WIDTH = GameInfo.P2M(14f);
	private static final float BODY_HEIGHT = GameInfo.P2M(12f);
	private static final float SPROUT_TIME = 0.5f;
	private static final Vector2 START_BOUNCE_VEL = new Vector2(0.5f, 2f); 
	private static final float SPROUT_OFFSET = GameInfo.P2M(-13f);
	private enum StarState { SPROUT, WALK };

	private WorldRunner runner;
	private Body b2body;
	private PowerStarSprite starSprite;

	private float stateTimer;
	private StarState prevState;

	public PowerStar(WorldRunner runner, Vector2 position) {
		this.runner = runner;

		starSprite = new PowerStarSprite(runner.getAtlas(), position.cpy().add(0f, SPROUT_OFFSET));

		defineBody(position);

		velocity = START_BOUNCE_VEL.cpy();

		prevState = StarState.SPROUT;
		stateTimer = 0f;

		runner.enableRobotUpdate(this);
		runner.setRobotDrawLayer(this, SpriteDrawOrder.BOTTOM);
	}

	private void defineBody(Vector2 position) {
		BodyDef bdef;
		FixtureDef fdef;
		PolygonShape shroomShape;

		bdef = new BodyDef();
		bdef.position.set(position);
		bdef.type = BodyDef.BodyType.DynamicBody;
		bdef.gravityScale = 0.5f;	// floaty
		b2body = runner.getWorld().createBody(bdef);

		fdef = new FixtureDef();
		fdef.filter.categoryBits = GameInfo.ITEM_BIT;
		// items touch mario but can pass through goombas, turtles, etc.
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT | GameInfo.MARIO_ROBOSENSOR_BIT;

		// bouncy
		fdef.restitution = 1f;

		shroomShape = new PolygonShape();
		shroomShape.setAsBox(BODY_WIDTH/2f, BODY_HEIGHT/2f);
		fdef.shape = shroomShape;
		b2body.createFixture(fdef).setUserData(this);

		b2body.setActive(true);
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
					b2body.applyLinearImpulse(START_BOUNCE_VEL, b2body.getWorldCenter(), true);
					break;
				}

				// clamp y velocity and maintain steady x velocity
				if(b2body.getLinearVelocity().y > velocity.y)
					b2body.setLinearVelocity(velocity.x, velocity.y);
				else if(b2body.getLinearVelocity().y < -velocity.y)
					b2body.setLinearVelocity(velocity.x, -velocity.y);
				else
					b2body.setLinearVelocity(velocity.x, b2body.getLinearVelocity().y);
				break;
			case SPROUT:
				if(stateTimer > SPROUT_TIME)
					runner.setRobotDrawLayer(this, SpriteDrawOrder.MIDDLE);
				else
					yOffset = SPROUT_OFFSET * (SPROUT_TIME - stateTimer) / SPROUT_TIME;
				break;
		}

		starSprite.update(delta, b2body.getPosition().cpy().add(0f, yOffset));

		// increment state timer
		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		prevState = curState;
	}

	@Override
	public void draw(Batch batch){
		starSprite.draw(batch);
	}

	@Override
	public Vector2 getPosition() {
		return b2body.getPosition();
	}

	@Override
	protected void onInnerTouchBoundLine(LineSeg seg) {
		// bounce off of vertical bounds only
		if(!seg.isHorizontal)
			reverseVelocity(true,  false);
	}

	@Override
	public void onTouchRobot(RobotRole robo) {
	}

	@Override
	public void dispose() {
		runner.getWorld().destroyBody(b2body);
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(b2body.getPosition().x - BODY_WIDTH/2f, b2body.getPosition().y - BODY_HEIGHT/2f,
				BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public void use(PlayerRole role) {
		if(stateTimer <= SPROUT_TIME)
			return;

		if(role instanceof MarioRole) {
			((MarioRole) role).applyPowerup(PowerupType.POWERSTAR);
			runner.removeRobot(this);
		}
	}

	@Override
	public void onBump(Vector2 fromCenter) {
		if(stateTimer <= SPROUT_TIME)
			return;

		// if bump came from left and star is moving left then reverse
		if(fromCenter.x < b2body.getPosition().x && b2body.getLinearVelocity().x < 0f)
			b2body.setLinearVelocity(velocity.x, velocity.y);
		else
			b2body.setLinearVelocity(-velocity.x, velocity.y);
	}

	@Override
	public void setActive(boolean b) {
		b2body.setActive(b);
	}
}
