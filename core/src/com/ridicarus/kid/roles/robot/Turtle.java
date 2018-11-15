/*
 * By: David Loucks
 * Approx. Date: 2018.11.08
*/
package com.ridicarus.kid.roles.robot;

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
import com.ridicarus.kid.collisionmap.LineSeg;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.sprites.TurtleSprite;
import com.ridicarus.kid.tools.WorldRunner;

/*
 * TODO:
 *  Do sliding turtle shells break bricks when they strike them?
 *  I couldn't find any maps in SMB 1 that would clear up this matter.
 */
public class Turtle extends WalkingRobot implements HeadBounceBot, TouchDmgBot, BumpableBot, DamageableBot
{
	private static final float BODY_WIDTH = GameInfo.P2M(14f);
	private static final float BODY_HEIGHT = GameInfo.P2M(14f);
	private static final float FOOT_WIDTH = GameInfo.P2M(12f);
	private static final float FOOT_HEIGHT = GameInfo.P2M(4f);
	private static final float TURTLE_WALK_VEL = 0.4f;
	private static final float TURTLE_HIDE_TIME = 3f;
	private static final float TURTLE_DIE_FALL_TIME = 6f;
	private static final float TURTLE_BUMP_UP_VEL = 2f;
	private static final float TURTLE_BUMP_SIDE_VEL = 1f;
	private static final float TURTLE_SLIDE_VEL = 1.5f;

	public enum TurtleState { WALK, HIDE, SLIDE, DEAD };

	private Body b2body;

	private WorldRunner runner;

	private TurtleSprite turtleSprite;

	private TurtleState prevState;
	private float stateTimer;

	private boolean facingRight;
	private int onGroundCount;
	private boolean isOnGround;
	private boolean isHiding;	// after mario bounces on head, turtle hides in shell
	private boolean isSliding;
	private boolean isDead;
	private boolean isDeadToRight;

	public Turtle(WorldRunner runner, MapObject object) {
		Rectangle bounds;

		this.runner = runner;

		facingRight = false;
		velocity = new Vector2(-TURTLE_WALK_VEL, 0f);

		bounds = ((RectangleMapObject) object).getRectangle();
		turtleSprite = new TurtleSprite(runner.getAtlas(), GameInfo.P2M(bounds.getX() + bounds.getWidth() / 2f),
				GameInfo.P2M(bounds.getY() + bounds.getHeight() / 2f));

		defineBody(GameInfo.P2M(bounds.getX() + bounds.getWidth() / 2f),
				GameInfo.P2M(bounds.getY() + bounds.getHeight() / 2f));

		prevState = TurtleState.WALK;
		stateTimer = 0f;

		onGroundCount = 0;
		isOnGround = false;
		isHiding = false;
		isSliding = false;
		isDead = false;
		isDeadToRight = false;
	}

	private TurtleState getState() {
		if(isDead)
			return TurtleState.DEAD;
		else if(isSliding)
			return TurtleState.SLIDE;
		else if(isHiding)
			return TurtleState.HIDE;
		else
			return TurtleState.WALK;
	}

	public void update(float delta) {
		TurtleState curState = getState();
		switch(curState) {
			case DEAD:
				// newly deceased?
				if(curState != prevState)
					startDeath();
				// check the old deceased for timeout
				else if(stateTimer > TURTLE_DIE_FALL_TIME)
					runner.removeRobot(this);
				break;
			case HIDE:
				// TODO: turtle should poke it's feet out and pull them back in a few times before unhiding
				// wait a short time and reappear
				if(curState != prevState)
					startHideInShell();
				else if(stateTimer > TURTLE_HIDE_TIME)
					endHideInShell();
				break;
			case SLIDE:
			case WALK:
				if(isOnGround)
					b2body.setLinearVelocity(velocity);
				break;
		}

		// update sprite position and graphic
		turtleSprite.update(delta, b2body.getPosition(), curState, facingRight);

		// increment state timer if state stayed the same, otherwise reset timer
		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		prevState = curState;
	}

	private void startHideInShell() {
		// stop moving
		b2body.setLinearVelocity(0f, 0f);
	}

	private void endHideInShell() {
		isHiding = false;
	}

	private void startDeath() {
		Filter filter;

		filter = new Filter();
		filter.categoryBits = GameInfo.NOTHING_BIT;
		filter.maskBits = GameInfo.NOTHING_BIT;
		for(Fixture fix : b2body.getFixtureList())
			fix.setFilterData(filter);

		b2body.setLinearVelocity(0f, 0f);
		// die move to the right or die move to to the left?
		if(isDeadToRight) {
			b2body.applyLinearImpulse(new Vector2(TURTLE_BUMP_SIDE_VEL, TURTLE_BUMP_UP_VEL),
					b2body.getWorldCenter(), true);
		}
		else {
			b2body.applyLinearImpulse(new Vector2(-TURTLE_BUMP_SIDE_VEL, TURTLE_BUMP_UP_VEL),
					b2body.getWorldCenter(), true);
		}
	}

	private void defineBody(float x, float y) {
		BodyDef bdef;
		FixtureDef fdef;
		PolygonShape boxShape;
		PolygonShape footSensor;

		bdef = new BodyDef();
		bdef.position.set(x, y);
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = runner.getWorld().createBody(bdef);

		fdef = new FixtureDef();
		boxShape = new PolygonShape();
		boxShape.setAsBox(BODY_WIDTH/2f, BODY_HEIGHT/2f);
		fdef.filter.categoryBits = GameInfo.ROBOT_BIT;
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT |
				GameInfo.ROBOT_BIT |
				GameInfo.MARIO_ROBOT_SENSOR_BIT;

		fdef.shape = boxShape;
		b2body.createFixture(fdef).setUserData(this);

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

	@Override
	public void draw(Batch batch){
		turtleSprite.draw(batch);
	}

	@Override
	public void onHeadBounce(Vector2 fromPos) {
		if(isDead)
			return;

		if(isSliding)
			stopSlide();
		else if(isHiding) {
			if(fromPos.x > b2body.getPosition().x)
				startSlide(false);	// slide right
			else
				startSlide(true);	// slide left
		}
		else
			isHiding = true;
	}

	private void startSlide(boolean right) {
		isSliding = true;
		facingRight = right;
		if(right)
			velocity.x = TURTLE_SLIDE_VEL;
		else
			velocity.x = -TURTLE_SLIDE_VEL;
	}

	private void stopSlide() {
		isSliding = false;
		velocity.set(0f, 0f);
	}

	@Override
	public void onTouchRobot(RobotRole robo) {
		// if walking normally and touched another robot then reverse direction
		if(!isHiding && !isSliding)
			reverseVelocity(true, false);
		else if(isSliding) {
			// if hit another sliding turtle, then both die
			if(robo instanceof Turtle && ((Turtle) robo).isSliding) {
				((DamageableBot) robo).onDamage(1f, b2body.getPosition());
				onDamage(1f, robo.getBody().getPosition());
			}
			// else if sliding and strikes a dmgable bot...
			else if(robo instanceof DamageableBot)
				((DamageableBot) robo).onDamage(1f, robo.getBody().getPosition());
		}
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
	public void onBump(Vector2 fromCenter) {
		isDead = true;
		if(fromCenter.x < b2body.getPosition().x)
			isDeadToRight = true;
		else
			isDeadToRight = false;
	}

	@Override
	protected void reverseVelocity(boolean x, boolean y){
		super.reverseVelocity(x, y);
		facingRight = !facingRight;
	}

	@Override
	public boolean isTouchDamage() {
		if(isDead || (isHiding && !isSliding))
			return false;
		return true;
	}

	// assume any amount of damage kills, for now...
	@Override
	public void onDamage(float amount, Vector2 fromCenter) {
		isDead = true;
		if(fromCenter.x < b2body.getPosition().x)
			isDeadToRight = true;
		else
			isDeadToRight = false;
	}

	/*
	 * The player can "kick" a turtle hiding in its shell.
	 */
	public void onPlayerTouch(Vector2 position) {
		if(isDead)
			return;

		// a living turtle hiding in the shell and not sliding can be "pushed" to slide
		if(isHiding && !isSliding) {
			// pushed from left?
			if(position.x < b2body.getPosition().x)
				startSlide(true);	// slide right
			else
				startSlide(false);	// slide left
		}
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(b2body.getPosition().x - BODY_WIDTH/2f, b2body.getPosition().y - BODY_HEIGHT/2,
				BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public Body getBody() {
		return b2body;
	}

	@Override
	public void dispose() {
		runner.getWorld().destroyBody(b2body);
	}
}
