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
import com.ridicarus.kid.roles.PlayerRole;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.screens.PlayScreen;
import com.ridicarus.kid.sprites.GoombaSprite;

/**
 * Created by brentaureli on 9/14/15.
 */
// Edited by Dave after that
public class Goomba extends RobotRole
{
	public static final float GOOMBA_SQUISH_TIME = 1f;

	// ***
	public enum GoombaState { WALK, DEAD, FALL };

	private Body b2body;
	private Vector2 velocity;

	private PlayScreen screen;

	private GoombaSprite goombaSprite;

	private GoombaState prevState;
	private float stateTimer;

	private int onGroundCount;
	private boolean isOnGround;
	private boolean isDead;

	public Goomba(PlayScreen screen, MapObject object){
		Rectangle bounds;

		this.screen = screen;

		velocity = new Vector2(-0.4f, 0f);

		bounds = ((RectangleMapObject) object).getRectangle();
		goombaSprite = new GoombaSprite(screen.getAtlas());
		goombaSprite.setPosition(GameInfo.P2M(bounds.getX() + bounds.getWidth() / 2f),
				GameInfo.P2M(bounds.getY() + bounds.getHeight() / 2f));
		goombaSprite.setBounds(goombaSprite.getX(), goombaSprite.getY(), GameInfo.P2M(16), GameInfo.P2M(16));

		defineGoomba(GameInfo.P2M(bounds.getX() + bounds.getWidth() / 2f),
				GameInfo.P2M(bounds.getY() + bounds.getHeight() / 2f));

		prevState = GoombaState.WALK;
		stateTimer = 0f;

		onGroundCount = 0;
		isOnGround = false;
		isDead = false;
	}

	public void reverseVelocity(boolean x, boolean y){
		if(x)
			velocity.x = -velocity.x;
		if(y)
			velocity.y = -velocity.y;
	}

	private GoombaState getState() {
		if(isDead)
			return GoombaState.DEAD;
		else if(isOnGround)
			return GoombaState.WALK;
		else
			return GoombaState.FALL;
	}

	private void die() {
		isDead = true;
	}

	public void update(float delta) {
		GoombaState curState = getState();
		// TODO: should the following two lines go at the end of this function?
		if(curState != prevState)
			stateTimer = 0f;

		switch(curState) {
			case WALK:
				// move if walking
				b2body.setLinearVelocity(velocity);
				break;
			case DEAD:
				// wait a short time and disappear, if dead
				if(stateTimer > 2f)
					screen.getWorldRunner().removeRobot(this);
				break;
			case FALL:
				break;	// do nothing if falling
		}

		// update sprite position and graphic
		goombaSprite.setPosition(b2body.getPosition().x - goombaSprite.getWidth() / 2,
				b2body.getPosition().y - goombaSprite.getHeight() / 2);
		goombaSprite.update(delta, curState);

		// increment state timer if state stayed the same, otherwise reset timer
		stateTimer += delta;
		prevState = curState;
	}

	private void defineGoomba(float x, float y) {
		BodyDef bdef = new BodyDef();
		bdef.position.set(x, y);
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = screen.getWorld().createBody(bdef);

		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(GameInfo.P2M(7f),  GameInfo.P2M(7f));
		fdef.filter.categoryBits = GameInfo.ROBOT_BIT;
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT |
				GameInfo.ROBOT_BIT |
				GameInfo.MARIO_ROBOT_SENSOR_BIT;

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

		// start in the inactive state, becoming active when the player is close enough
		b2body.setActive(false);
	}

	public void draw(Batch batch){
		goombaSprite.draw(batch);
	}

	public void hitOnHead(PlayerRole role) {
		MyKidRidicarus.manager.get("audio/sounds/stomp.wav", Sound.class).play();
	}

	@Override
	public Body getBody() {
		return b2body;
	}

	public void applySquish() {
		Filter filter = new Filter();
		filter.categoryBits = GameInfo.ROBOT_BIT;
		filter.maskBits = GameInfo.BOUNDARY_BIT;

		// stop dead
		b2body.setLinearVelocity(0f, 0f);

		// players and other robots can now pass through the body
		for(Fixture fix : b2body.getFixtureList())
			fix.setFilterData(filter);

		die();
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
		screen.getWorld().destroyBody(b2body);
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(b2body.getPosition().x - goombaSprite.getWidth()/2,
				b2body.getPosition().y - goombaSprite.getHeight()/2,
				goombaSprite.getWidth(), goombaSprite.getHeight());
	}

	@Override
	public void onInnerTouchBoundLine(LineSeg seg) {
		// bounce off of vertical bounds
		if(!seg.isHorizontal)
			reverseVelocity(true,  false);
	}

	// needed for items, not for goombas
	@Override
	public void use(PlayerRole role) {
	}
}
