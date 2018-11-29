package com.ridicarus.kid.roles.robot;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.GameInfo.SpriteDrawOrder;
import com.ridicarus.kid.collisionmap.LineSeg;
import com.ridicarus.kid.roles.MobileRobot;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.sprites.MarioFireballSprite;
import com.ridicarus.kid.tools.WorldRunner;

public class MarioFireball extends MobileRobot {
	private static final float BODY_WIDTH = GameInfo.P2M(7f);
	private static final float BODY_HEIGHT = GameInfo.P2M(7f);

	private static final Vector2 MOVE_VEL = new Vector2(2.4f, -1.25f);
	private static final float MAX_Y_VEL = 2.0f;

	private WorldRunner runner;

	private Body b2body;
	private MarioFireballSprite fireballSprite;

	public enum FireballState { FLY, EXPLODE };
	private FireballState prevState;
	private float stateTimer;

	private enum TouchState { NONE, WALL, ROBOT };
	private TouchState curTouchState;

	public MarioFireball(WorldRunner runner, Vector2 position, boolean right){
		this.runner = runner;

		fireballSprite = new MarioFireballSprite(runner.getAtlas(), position);

		if(right)
			defineBody(position, MOVE_VEL.cpy().scl(1, 1));
		else
			defineBody(position, MOVE_VEL.cpy().scl(-1, 1));

		prevState = FireballState.FLY;
		stateTimer = 0f;
		curTouchState = TouchState.NONE;

		runner.enableRobotUpdate(this);
		runner.setRobotDrawLayer(this, SpriteDrawOrder.MIDDLE);
	}

	private void defineBody(Vector2 position, Vector2 velocity) {
		BodyDef bdef = new BodyDef();
		bdef.position.set(position);
		bdef.linearVelocity.set(velocity);
		bdef.gravityScale = 2f;
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = runner.getWorld().createBody(bdef);

		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(BODY_WIDTH/2f, BODY_HEIGHT/2f);
		fdef.friction = 0f;
		fdef.restitution = 1f;
		fdef.filter.categoryBits = GameInfo.ROBOT_BIT;
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT |
				GameInfo.ROBOT_BIT;

		fdef.shape = boxShape;
		b2body.createFixture(fdef).setUserData(this);

		b2body.setActive(true);
	}

	private FireballState getState() {
		if(curTouchState == TouchState.NONE)
			return FireballState.FLY;
		return FireballState.EXPLODE;
	}

	@Override
	public void update(float delta) {
		FireballState curState = getState();
		switch(curState) {
			case EXPLODE:
				if(curState != prevState) {
					Filter filter = new Filter();
					filter.categoryBits = GameInfo.NOTHING_BIT;
					filter.maskBits = GameInfo.NOTHING_BIT;
					b2body.setLinearVelocity(0f, 0f);
					b2body.setGravityScale(0f);
					for(Fixture fix : b2body.getFixtureList())
						fix.setFilterData(filter);
					if(curTouchState == TouchState.ROBOT)
						runner.playSound(GameInfo.SOUND_KICK);
					else
						runner.playSound(GameInfo.SOUND_BUMP);
				}
				if(fireballSprite.isExplodeFinished())
					runner.removeRobot(this);
				break;
			case FLY:
				break;
		}

		if(b2body.getLinearVelocity().y > MAX_Y_VEL)
			b2body.setLinearVelocity(b2body.getLinearVelocity().x, MAX_Y_VEL);
		else if(b2body.getLinearVelocity().y < -MAX_Y_VEL)
			b2body.setLinearVelocity(b2body.getLinearVelocity().x, -MAX_Y_VEL);

		// update sprite position and graphic
		fireballSprite.update(delta, b2body.getPosition(), curState);

		// increment state timer if state stayed the same, otherwise reset timer
		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		prevState = curState;
	}

	@Override
	public void draw(Batch batch) {
		fireballSprite.draw(batch);
	}

	@Override
	public Vector2 getPosition() {
		return b2body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(b2body.getPosition().x - BODY_WIDTH/2f,
				b2body.getPosition().y - BODY_HEIGHT/2f, BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	protected void onInnerTouchBoundLine(LineSeg seg) {
		curTouchState = TouchState.WALL;
	}

	@Override
	public void onTouchRobot(RobotRole robo) {
		curTouchState = TouchState.ROBOT;
		if(robo instanceof DamageableBot)
			((DamageableBot) robo).onDamage(1f, b2body.getPosition());
	}

	@Override
	public void dispose() {
		runner.getWorld().destroyBody(b2body);
	}

	@Override
	public void setActive(boolean b) {
		b2body.setActive(b);
	}
}
