package com.ridicarus.kid.roles.robot;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.MyKidRidicarus;
import com.ridicarus.kid.collisionmap.LineSeg;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.sprites.GoombaSprite;
import com.ridicarus.kid.tools.WorldRunner;

public class Goomba extends WalkingRobot implements HeadBounceBot, TouchDmgBot, BumpableBot, DamageableBot
{
	private static final float BODY_WIDTH = GameInfo.P2M(14f);
	private static final float BODY_HEIGHT = GameInfo.P2M(14f);
	private static final float FOOT_WIDTH = GameInfo.P2M(12f);
	private static final float FOOT_HEIGHT = GameInfo.P2M(4f);
	private static final float GOOMBA_SQUISH_TIME = 2f;
	private static final float GOOMBA_BUMP_FALL_TIME = 6f;
	private static final float GOOMBA_BUMP_UP_VEL = 2f;
	private static final float GOOMBA_WALK_VEL = 0.4f;

	public enum GoombaState { WALK, FALL, DEAD_SQUISH, DEAD_BUMPED };

	private WorldRunner runner;

	private Body b2body;
	private GoombaSprite goombaSprite;

	private GoombaState prevState;
	private float stateTimer;

	private int onGroundCount;
	private boolean isOnGround;
	private boolean isSquished;
	private boolean isBumped;

	public Goomba(WorldRunner runner, MapObject object){
		Rectangle bounds;

		this.runner = runner;

		velocity = new Vector2(-GOOMBA_WALK_VEL, 0f);

		bounds = ((RectangleMapObject) object).getRectangle();
		goombaSprite = new GoombaSprite(runner.getAtlas(), GameInfo.P2M(bounds.getX() + bounds.getWidth() / 2f),
				GameInfo.P2M(bounds.getY() + bounds.getHeight() / 2f));

		defineBody(GameInfo.P2M(bounds.getX() + bounds.getWidth() / 2f),
				GameInfo.P2M(bounds.getY() + bounds.getHeight() / 2f));

		prevState = GoombaState.WALK;
		stateTimer = 0f;

		onGroundCount = 0;
		isOnGround = false;
		// the equivalent of isDead: bumped | squished
		isBumped = false;
		isSquished = false;
	}

	private GoombaState getState() {
		if(isBumped)
			return GoombaState.DEAD_BUMPED;
		else if(isSquished)
			return GoombaState.DEAD_SQUISH;
		else if(isOnGround)
			return GoombaState.WALK;
		else
			return GoombaState.FALL;
	}

	public void update(float delta) {
		GoombaState curState = getState();
		switch(curState) {
			case DEAD_SQUISH:
				// new squish?
				if(curState != prevState)
					startSquish();
				// wait a short time and disappear, if dead
				else if(stateTimer > GOOMBA_SQUISH_TIME)
					runner.removeRobot(this);
				break;
			case DEAD_BUMPED:
				// new bumper?
				if(curState != prevState)
					startBump();
				// check the old bumper for timeout
				else if(stateTimer > GOOMBA_BUMP_FALL_TIME)
					runner.removeRobot(this);
				break;
			case WALK:
				// move if walking
				b2body.setLinearVelocity(velocity);
				break;
			case FALL:
				break;	// do nothing if falling
		}

		// update sprite position and graphic
		goombaSprite.update(delta, b2body.getPosition(), curState);

		// increment state timer if state stayed the same, otherwise reset timer
		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		prevState = curState;
	}

	private void defineBody(float x, float y) {
		BodyDef bdef = new BodyDef();
		bdef.position.set(x, y);
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = runner.getWorld().createBody(bdef);

		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(BODY_WIDTH/2f, BODY_HEIGHT/2f);
		fdef.filter.categoryBits = GameInfo.ROBOT_BIT;
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT |
				GameInfo.ROBOT_BIT |
				GameInfo.MARIO_ROBOT_SENSOR_BIT;

		fdef.shape = boxShape;
		b2body.createFixture(fdef).setUserData(this);

		PolygonShape footSensor;
		footSensor = new PolygonShape();
		footSensor.setAsBox(FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -BODY_HEIGHT/2f), 0f);
		fdef.filter.categoryBits = GameInfo.ROBOTFOOT_BIT;
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT;
		fdef.shape = footSensor;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(this);

		// start in the inactive state, becoming active when the player is close enough
		b2body.setActive(false);
	}

	private void startSquish() {
		Filter filter;

		// stop dead
		b2body.setLinearVelocity(0f, 0f);

		// other robots can now pass through the body
		filter = new Filter();
		filter.categoryBits = GameInfo.ROBOT_BIT;
		filter.maskBits = GameInfo.BOUNDARY_BIT;
		for(Fixture fix : b2body.getFixtureList())
			fix.setFilterData(filter);

		MyKidRidicarus.manager.get(GameInfo.SOUND_STOMP, Sound.class).play();
	}

	private void startBump() {
		Filter filter;

		// body can pass through everything, to fall off screen (don't destroy body because we need gravity calcs).
		filter = new Filter();
		filter.categoryBits = GameInfo.NOTHING_BIT;
		filter.maskBits = GameInfo.NOTHING_BIT;
		for(Fixture fix : b2body.getFixtureList())
			fix.setFilterData(filter);

		// keep x velocity, but redo the y velocity so goomba bounces up
		b2body.setLinearVelocity(b2body.getLinearVelocity().x, GOOMBA_BUMP_UP_VEL);
	}
	
	@Override
	public void draw(Batch batch){
		goombaSprite.draw(batch);
	}

	@Override
	public void onTouchRobot(RobotRole robo) {
		reverseVelocity(true, false);
	}

	// Foot sensor might come into contact with multiple boundary lines, so increment for each contact start,
	// and decrement for each contact end. If onGroundCount reaches zero then mario is not on the ground.
	@Override
	public void onTouchGround() {
		onGroundCount++;
		isOnGround = true;
	}

	@Override
	public void onLeaveGround() {
		onGroundCount--;
		if(onGroundCount == 0)
			isOnGround = false;
	}

	@Override
	public void onInnerTouchBoundLine(LineSeg seg) {
		// bounce off of vertical bounds
		if(!seg.isHorizontal)
			reverseVelocity(true,  false);
	}

	@Override
	public void onHeadBounce(Vector2 fromPos) {
		isSquished = true;
	}
	
	/*
	 * A goomba, bumped from below, should bounce up into the air a bit and drop off the screen upside down.
	 * (non-Javadoc)
	 * @see com.ridicarus.kid.roles.robot.BumpableBot#onBump(com.badlogic.gdx.math.Vector2)
	 */
	@Override
	public void onBump(Vector2 fromCenter) {
		isBumped = true;
	}

	// assume any amount of damage kills, for now...
	@Override
	public void onDamage(float amount, Vector2 fromCenter) {
		isBumped = true;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(b2body.getPosition().x - BODY_WIDTH/2f,
				b2body.getPosition().y - BODY_HEIGHT/2f, BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public Body getBody() {
		return b2body;
	}

	// touching goomba does damage to players
	@Override
	public boolean isTouchDamage() {
		return true;
	}

	@Override
	public void dispose() {
		runner.getWorld().destroyBody(b2body);
	}
}
