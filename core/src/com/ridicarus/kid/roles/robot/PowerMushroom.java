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
import com.ridicarus.kid.sprites.MushroomSprite;
import com.ridicarus.kid.tools.WorldRunner;

public class PowerMushroom extends MobileRobot implements ItemBot, BumpableBot, GroundCheckBot {
	private static final float BODY_WIDTH = GameInfo.P2M(14f);
	private static final float BODY_HEIGHT = GameInfo.P2M(12f);
	private static final float FOOT_WIDTH = GameInfo.P2M(12f);
	private static final float FOOT_HEIGHT = GameInfo.P2M(4f);
	private static final float SPROUT_TIME = 1f;
	private static final float SPROUT_OFFSET = GameInfo.P2M(-13f);
	private static final float WALK_VEL = 0.6f;
	private static final float BUMP_UPVEL = 1.5f;

	private enum MushroomState { SPROUT, WALK, FALL };

	private Body b2body;

	private WorldRunner runner;

	private MushroomState prevState;
	private float stateTimer;

	private int onGroundCount;
	private boolean isOnGround;
	private boolean isSprouting;
	private boolean isBumped;
	private Vector2 bumpCenter;

	private MushroomSprite mSprite;

	private Vector2 sproutingPosition;

	public PowerMushroom(WorldRunner runner, Vector2 position) {
		this.runner = runner;

		velocity = new Vector2(WALK_VEL, 0f);

		mSprite = new MushroomSprite(runner.getAtlas(), position.cpy().add(0f, SPROUT_OFFSET));

		sproutingPosition = position;

		prevState = MushroomState.WALK;
		stateTimer = 0f;

		onGroundCount = 0;
		isOnGround = false;
		isSprouting = true;
		isBumped = false;

		runner.enableRobotUpdate(this);
		runner.setRobotDrawLayer(this, SpriteDrawOrder.BOTTOM);
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
		MushroomState curState;
		float yOffset;

		// process bumpings
		if(isBumped) {
			isBumped = false;
			// If moving right and bumped from the right then reverse velocity,
			// if moving left and bumped from the left then reverse velocity
			if((velocity.x > 0 && bumpCenter.x > b2body.getPosition().x) ||
					(velocity.x < 0 && bumpCenter.x < b2body.getPosition().x)) {
				reverseVelocity(true, false);
			}
			b2body.applyLinearImpulse(new Vector2(0f, BUMP_UPVEL), b2body.getWorldCenter(), true);
		}

		yOffset = 0f;
		curState = getState();
		switch(curState) {
			case WALK:
				// move if walking
				b2body.setLinearVelocity(velocity.x, b2body.getLinearVelocity().y);
				break;
			case SPROUT:
				// wait a short time to finish sprouting
				if(stateTimer > SPROUT_TIME) {
					isSprouting = false;
					runner.setRobotDrawLayer(this, SpriteDrawOrder.MIDDLE);
					defineBody(sproutingPosition);
				}
				else
					yOffset = SPROUT_OFFSET * (SPROUT_TIME - stateTimer) / SPROUT_TIME;
				break;
			case FALL:
				break;	// do nothing if falling
		}

		if(isSprouting)
			mSprite.update(delta, sproutingPosition.cpy().add(0f, yOffset));
		else
			mSprite.update(delta, b2body.getPosition().cpy().add(0f, yOffset));

		// increment state timer if state stayed the same, otherwise reset timer
		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		prevState = curState;
	}

	private void defineBody(Vector2 position) {
		BodyDef bdef;
		FixtureDef fdef;
		PolygonShape boxShape;

		bdef = new BodyDef();
		bdef.position.set(position);
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = runner.getWorld().createBody(bdef);

		fdef = new FixtureDef();
		boxShape = new PolygonShape();
		boxShape.setAsBox(BODY_WIDTH/2f, BODY_HEIGHT/2f);
		fdef.filter.categoryBits = GameInfo.ITEM_BIT;
		// items can pass through goombas, turtles, etc.
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT | GameInfo.MARIO_ROBOSENSOR_BIT;

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

		b2body.setActive(true);
	}

	@Override
	public void draw(Batch batch) {
		mSprite.draw(batch);
	}

	@Override
	protected void onInnerTouchBoundLine(LineSeg seg) {
		// bounce off of vertical bounds only
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
	public void onBump(Vector2 fromCenter) {
		if(isSprouting)
			return;

		isBumped = true;
		bumpCenter = fromCenter.cpy(); 
	}

	@Override
	public void use(PlayerRole role) {
		if(isSprouting)
			return;

		if(role instanceof MarioRole) {
			((MarioRole) role).applyPowerup(PowerupType.MUSHROOM);
			runner.removeRobot(this);
		}
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
	public Vector2 getPosition() {
		return b2body.getPosition();
	}

	@Override
	public void setActive(boolean b) {
		b2body.setActive(b);
	}
}
