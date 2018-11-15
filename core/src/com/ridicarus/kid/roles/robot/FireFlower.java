package com.ridicarus.kid.roles.robot;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.collisionmap.LineSeg;
import com.ridicarus.kid.roles.PlayerRole;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.roles.player.MarioRole;
import com.ridicarus.kid.sprites.FireFlowerSprite;
import com.ridicarus.kid.tools.WorldRunner;

public class FireFlower extends ItemRobot {
	private static final float BODY_WIDTH = GameInfo.P2M(14f);
	private static final float BODY_HEIGHT = GameInfo.P2M(12f);
	private static final float SPROUT_TIME = 0.5f;

	private FireFlowerSprite flowerSprite;
	private Body b2body;
	private float stateTimer;

	private WorldRunner runner;

	public FireFlower(WorldRunner runner, float x, float y) {
		this.runner = runner;

		flowerSprite = new FireFlowerSprite(runner.getAtlas(), x, y);

		defineBody(x, y);

		stateTimer = 0f;
	}

	private void defineBody(float x, float y) {
		BodyDef bdef;
		FixtureDef fdef;
		PolygonShape shroomShape;

		bdef = new BodyDef();
		bdef.position.set(x, y);
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = runner.getWorld().createBody(bdef);

		fdef = new FixtureDef();
		shroomShape = new PolygonShape();
		shroomShape.setAsBox(BODY_WIDTH/2f,  BODY_HEIGHT/2f);
		fdef.filter.categoryBits = GameInfo.ITEM_BIT;
		// items touch mario but can pass through goombas, turtles, etc.
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT | GameInfo.MARIO_ROBOT_SENSOR_BIT;

		fdef.shape = shroomShape;
		b2body.createFixture(fdef).setUserData(this);

		b2body.setActive(true);
	}

	public void update(float delta) {
		flowerSprite.update(delta, b2body.getPosition());

		// increment state timer
		stateTimer += delta;
	}

	public void draw(Batch batch){
		flowerSprite.draw(batch);
	}

	@Override
	public Body getBody() {
		return b2body;
	}

	protected void onInnerTouchBoundLine(LineSeg seg) {
	}
	
	@Override
	public void onTouchRobot(RobotRole robo) {
	}

	@Override
	public void onTouchGround() {
	}

	@Override
	public void onLeaveGround() {
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
		if(stateTimer > SPROUT_TIME && role instanceof MarioRole) {
			((MarioRole) role).applyPowerup(PowerupType.FIREFLOWER);
			runner.removeRobot(this);
		}
	}
}