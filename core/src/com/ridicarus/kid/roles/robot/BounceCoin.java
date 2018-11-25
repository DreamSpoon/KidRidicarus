package com.ridicarus.kid.roles.robot;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.collisionmap.LineSeg;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.sprites.BounceCoinSprite;
import com.ridicarus.kid.tools.WorldRunner;
import com.ridicarus.kid.tools.WorldRunner.RobotDrawLayers;

public class BounceCoin extends RobotRole {
	private static final float BODY_WIDTH = GameInfo.P2M(7f);
	private static final float BODY_HEIGHT = GameInfo.P2M(7f);
	private static final float COIN_SPIN_TIME = 0.54f;
	private static final Vector2 START_VELOCITY = new Vector2(0f, 3.1f);

	private WorldRunner runner;
	private Body b2body;
	private BounceCoinSprite coinSprite;
	private float stateTimer;

	public BounceCoin(WorldRunner runner, Vector2 position) {
		this.runner = runner;

		coinSprite = new BounceCoinSprite(runner.getAtlas(), position);
		defineBody(position, START_VELOCITY);
		stateTimer = 0f;

		runner.enableRobotUpdate(this);
		runner.setRobotDrawLayer(this, RobotDrawLayers.MIDDLE);
	}

	private void defineBody(Vector2 position, Vector2 velocity) {
		BodyDef bdef;
		FixtureDef fdef;
		PolygonShape coinShape;

		bdef = new BodyDef();
		bdef.position.set(position);
		bdef.linearVelocity.set(velocity);
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = runner.getWorld().createBody(bdef);

		fdef = new FixtureDef();
		coinShape = new PolygonShape();
		coinShape.setAsBox(BODY_WIDTH/2f,  BODY_HEIGHT/2f);
		// coin does not touch anything
		fdef.filter.categoryBits = GameInfo.NOTHING_BIT;
		fdef.filter.maskBits = GameInfo.NOTHING_BIT;

		fdef.shape = coinShape;
		b2body.createFixture(fdef).setUserData(this);

		b2body.setActive(true);
	}

	@Override
	public void update(float delta) {
		coinSprite.update(delta, b2body.getPosition());

		stateTimer += delta;
		if(stateTimer > COIN_SPIN_TIME)
			runner.removeRobot(this);
	}

	@Override
	public void draw(Batch batch) {
		coinSprite.draw(batch);
	}

	@Override
	public Body getBody() {
		return b2body;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(b2body.getPosition().x - BODY_WIDTH/2f, b2body.getPosition().y - BODY_HEIGHT/2f,
				BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	protected void onInnerTouchBoundLine(LineSeg seg) {
	}

	@Override
	public void onTouchRobot(RobotRole robo) {
	}

	@Override
	public void dispose() {
	}
}
