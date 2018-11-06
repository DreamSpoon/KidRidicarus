package com.ridicarus.kid.roles.robot;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.collisionmap.LineSeg;
import com.ridicarus.kid.roles.PlayerRole;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.roles.player.MarioRole;
import com.ridicarus.kid.tools.WorldRunner;

public class PowerMushroom extends RobotRole {
	// TODO: this is a guess value (0.001f) - test more to refine - may depend upon Pixels Per Meter and Pixels Per Tile
	public static final float GOOMBA_VS_VERT_BOUND_EPSILON = 0.001f;
	public static final float SPROUT_TIME = 1f;

	private enum MushroomState { SPROUT, WALK, FALL };

	private Body b2body;
	private Vector2 velocity;

	private WorldRunner runner;

	private MushroomState prevState;
	private float stateTimer;

	private int onGroundCount;
	private boolean isOnGround;
	private boolean isSprouting;

	private Sprite mushroomSprite;

	public PowerMushroom(WorldRunner runner, float x, float y) {
		this.runner = runner;

		velocity = new Vector2(0.6f, 0f);

		mushroomSprite = new Sprite(runner.getAtlas().findRegion(GameInfo.TEXATLAS_MUSHROOM));
		mushroomSprite.setPosition(x, y);
		mushroomSprite.setBounds(mushroomSprite.getX(), mushroomSprite.getY(),
				GameInfo.P2M(GameInfo.TILEPIX_X), GameInfo.P2M(GameInfo.TILEPIX_Y));

		defineMushroom(x, y);

		prevState = MushroomState.WALK;
		stateTimer = 0f;

		onGroundCount = 0;
		isOnGround = false;
		isSprouting = true;
	}

	private void reverseVelocity(boolean x, boolean y){
		if(x)
			velocity.x = -velocity.x;
		if(y)
			velocity.y = -velocity.y;
	}

	private MushroomState getState() {
		if(isSprouting)
			return MushroomState.SPROUT;
		else if(isOnGround)
			return MushroomState.WALK;
		else
			return MushroomState.FALL;
	}

	public void update(float delta) {
		MushroomState curState = getState();
		switch(curState) {
			case WALK:
				// move if walking
				b2body.setLinearVelocity(velocity);
				break;
			case SPROUT:
				// wait a short time to finish sprouting
				if(stateTimer > SPROUT_TIME)
					isSprouting = false;
				break;
			case FALL:
				break;	// do nothing if falling
		}

		// update sprite position graphic
		mushroomSprite.setPosition(b2body.getPosition().x - mushroomSprite.getWidth() / 2,
				b2body.getPosition().y - mushroomSprite.getHeight() / 2);

		// increment state timer if state stayed the same, otherwise reset timer
		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		prevState = curState;
	}

	private void defineMushroom(float x, float y) {
		BodyDef bdef = new BodyDef();
		bdef.position.set(x, y);
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = runner.getWorld().createBody(bdef);

		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(GameInfo.P2M(7f),  GameInfo.P2M(6f));
		fdef.filter.categoryBits = GameInfo.ITEM_BIT;
		// items can pass through goombas, turtles, etc.
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT | GameInfo.MARIO_ROBOT_SENSOR_BIT;

		fdef.shape = boxShape;
		b2body.createFixture(fdef).setUserData(this);

		PolygonShape footSensor;
		footSensor = new PolygonShape();
		footSensor.setAsBox(GameInfo.P2M(6f), GameInfo.P2M(2f), new Vector2(0f, GameInfo.P2M(-6)), 0f);
		fdef.filter.categoryBits = GameInfo.ROBOTFOOT_BIT;
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT;
		fdef.shape = footSensor;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(this);

		b2body.setActive(true);
	}

	public void draw(Batch batch){
		mushroomSprite.draw(batch);
	}

	@Override
	public Body getBody() {
		return b2body;
	}

	protected void onInnerTouchBoundLine(LineSeg seg) {
		// bounce off of vertical bounds
		if(!seg.isHorizontal)
			reverseVelocity(true,  false);
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
	public void dispose() {
		runner.getWorld().destroyBody(b2body);
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(b2body.getPosition().x - mushroomSprite.getWidth()/2,
				b2body.getPosition().y - mushroomSprite.getHeight()/2,
				mushroomSprite.getWidth(), mushroomSprite.getHeight());
	}

	@Override
	public void use(PlayerRole role) {
		if(role instanceof MarioRole) {
			((MarioRole) role).grow();
			runner.removeRobot(this);
		}
	}
}
