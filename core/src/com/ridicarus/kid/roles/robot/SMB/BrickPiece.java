package com.ridicarus.kid.roles.robot.SMB;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.GameInfo.SpriteDrawOrder;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.sprites.SMB.BrickPieceSprite;
import com.ridicarus.kid.worldrunner.WorldRunner;

public class BrickPiece implements RobotRole {
	private static final float BODY_WIDTH = GameInfo.P2M(8);
	private static final float BODY_HEIGHT = GameInfo.P2M(8);
	// bricks should be auto-removed when off screen, use this timeout for other cases
	private static final float BRICK_DIE_TIME = 7f;

	private BrickPieceSprite bpSprite;
	private Body b2body;
	private WorldRunner runner;
	private float stateTimer;

	public BrickPiece(WorldRunner runner, Vector2 position, Vector2 velocity, int startFrame) {
		this.runner = runner;
		defineBody(position, velocity);
		bpSprite = new BrickPieceSprite(runner.getAtlas(), position, BODY_WIDTH, startFrame);
		stateTimer = 0f;

		runner.enableRobotUpdate(this);
		runner.setRobotDrawLayer(this, SpriteDrawOrder.MIDDLE);
	}

	private void defineBody(Vector2 position, Vector2 velocity) {
		BodyDef bdef;
		FixtureDef fdef;
		CircleShape pieceShape;

		bdef = new BodyDef();
		bdef.position.set(position);
		bdef.linearVelocity.set(velocity);
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = runner.getWorld().createBody(bdef);

		fdef = new FixtureDef();
        pieceShape = new CircleShape();
        pieceShape.setRadius(BODY_WIDTH / 2f);

		// does not interact with anything
		fdef.filter.categoryBits = GameInfo.NOTHING_BIT;
		fdef.filter.maskBits = GameInfo.NOTHING_BIT;

		fdef.shape = pieceShape;
		b2body.createFixture(fdef);
	}

	@Override
	public void update(float delta) {
		bpSprite.update(b2body.getPosition(), delta);
		if(b2body.getPosition().y < 0f || stateTimer > BRICK_DIE_TIME)
			runner.removeRobot(this);
		stateTimer += delta;
	}

	@Override
	public void draw(Batch batch) {
		bpSprite.draw(batch);
	}

	@Override
	public Vector2 getPosition() {
		return b2body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(b2body.getPosition().x - BODY_WIDTH/2f, b2body.getPosition().y - BODY_HEIGHT/2f,
				BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public void onTouchRobot(RobotRole robo) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void setActive(boolean b) {
		b2body.setActive(b);
	}
}
