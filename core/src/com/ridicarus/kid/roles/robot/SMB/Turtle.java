package com.ridicarus.kid.roles.robot.SMB;

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
import com.ridicarus.kid.GameInfo.SpriteDrawOrder;
import com.ridicarus.kid.InfoSMB.PointsAmount;
import com.ridicarus.kid.collisionmap.LineSeg;
import com.ridicarus.kid.roles.MobileRobotRole;
import com.ridicarus.kid.roles.PlayerRole;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.roles.robot.BumpableBot;
import com.ridicarus.kid.roles.robot.DamageableBot;
import com.ridicarus.kid.roles.robot.GroundCheckBot;
import com.ridicarus.kid.roles.robot.HeadBounceBot;
import com.ridicarus.kid.roles.robot.TouchDmgBot;
import com.ridicarus.kid.sprites.SMB.TurtleSprite;
import com.ridicarus.kid.worldrunner.WorldRunner;

/*
 * TODO:
 * -Do sliding turtle shells break bricks when they strike them?
 *  I couldn't find any maps in SMB 1 that would clear up this matter.
 * -turtle shells do not slide properly when they are kicked while touching a robot, since the slide kill
 *  robot code is only called when touching starts
 */
public class Turtle extends MobileRobotRole implements HeadBounceBot, TouchDmgBot, BumpableBot, DamageableBot, GroundCheckBot
{
	private static final float BODY_WIDTH = GameInfo.P2M(14f);
	private static final float BODY_HEIGHT = GameInfo.P2M(14f);
	private static final float FOOT_WIDTH = GameInfo.P2M(12f);
	private static final float FOOT_HEIGHT = GameInfo.P2M(4f);
	private static final float WALK_VEL = 0.4f;
	private static final float BUMP_UP_VEL = 2f;
	private static final float BUMP_SIDE_VEL = 1f;
	private static final float SLIDE_VEL = 2f;
	private static final float WAKING_TIME = 3f;
	private static final float WAKE_UP_DELAY = 1.7f;
	private static final float DIE_FALL_TIME = 6f;

	public enum TurtleState { WALK, HIDE, WAKE_UP, SLIDE, DEAD };

	private Body b2body;

	private WorldRunner runner;

	private TurtleSprite turtleSprite;

	private TurtleState prevState;
	private float stateTimer;

	private boolean facingRight;
	private int onGroundCount;
	private boolean isOnGround;
	private boolean isHiding;	// after mario bounces on head, turtle hides in shell
	private boolean isWaking;
	private boolean isSliding;
	private boolean isDead;
	private boolean isDeadToRight;
	private boolean isHeadBounced;
	private PlayerRole perp;
	private PointsAmount slidingTotal;

	public Turtle(WorldRunner runner, MapObject object) {
		Rectangle bounds;
		Vector2 position;

		this.runner = runner;

		facingRight = false;
		velocity = new Vector2(-WALK_VEL, 0f);

		bounds = ((RectangleMapObject) object).getRectangle();
		position = new Vector2(GameInfo.P2M(bounds.getX() + bounds.getWidth() / 2f),
				GameInfo.P2M(bounds.getY() + bounds.getHeight() / 2f));
		turtleSprite = new TurtleSprite(runner.getAtlas(), position);
		defineBody(position);

		prevState = TurtleState.WALK;
		stateTimer = 0f;

		onGroundCount = 0;
		isOnGround = false;
		isHiding = false;
		isWaking = false;
		isSliding = false;
		isDead = false;
		isDeadToRight = false;
		isHeadBounced = false;
		perp = null;
		// the more sequential hits while sliding the higher the points per hit
		slidingTotal = PointsAmount.ZERO;

		runner.enableRobotUpdate(this);
		runner.setRobotDrawLayer(this, SpriteDrawOrder.MIDDLE);
	}

	private TurtleState getState() {
		if(isDead)
			return TurtleState.DEAD;
		else if(isSliding)
			return TurtleState.SLIDE;
		else if(isHiding) {
			if(isWaking)
				return TurtleState.WAKE_UP;
			else
				return TurtleState.HIDE;
		}
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
				else if(stateTimer > DIE_FALL_TIME)
					runner.removeRobot(this);
				break;
			case HIDE:
				// wait a short time and reappear
				if(curState != prevState) {
					isWaking = false;
					startHideInShell();
				}
				else if(stateTimer > WAKE_UP_DELAY)
					isWaking = true;
				break;
			case WAKE_UP:
				if(curState == prevState && stateTimer > WAKING_TIME)
					endHideInShell();
				break;
			case SLIDE:
				if(curState != prevState)
					startSlide();
				// Intentionally not using break;
				// Because sliding turtle needs to move when onGround.
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

		isHeadBounced = false;
	}

	private void startSlide() {
		runner.playSound(GameInfo.SOUND_KICK);
		slidingTotal = PointsAmount.P400;
		// how to tell if player kicked from side or head bounced?
		if(perp != null)
			runner.givePointsToPlayer(perp, slidingTotal, true, b2body.getPosition(), GameInfo.P2M(16), isHeadBounced);
	}

	private void startHideInShell() {
		// stop moving
		b2body.setLinearVelocity(0f, 0f);
		runner.playSound(GameInfo.SOUND_STOMP);
		if(perp != null)
			runner.givePointsToPlayer(perp, PointsAmount.P100, true, b2body.getPosition(), GameInfo.P2M(16), isHeadBounced);
	}

	private void endHideInShell() {
		isWaking = false;
		isHiding = false;
		if(isOnGround)
			b2body.setLinearVelocity(velocity);
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
			b2body.applyLinearImpulse(new Vector2(BUMP_SIDE_VEL, BUMP_UP_VEL),
					b2body.getWorldCenter(), true);
		}
		else {
			b2body.applyLinearImpulse(new Vector2(-BUMP_SIDE_VEL, BUMP_UP_VEL),
					b2body.getWorldCenter(), true);
		}

		if(perp != null) {
			runner.givePointsToPlayer(perp, PointsAmount.P500, true, b2body.getPosition(), GameInfo.P2M(16),
					isHeadBounced);
		}
	}

	private void defineBody(Vector2 position) {
		BodyDef bdef;
		FixtureDef fdef;
		PolygonShape boxShape;
		PolygonShape footSensor;

		bdef = new BodyDef();
		bdef.position.set(position);
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = runner.getWorld().createBody(bdef);

		fdef = new FixtureDef();
		boxShape = new PolygonShape();
		boxShape.setAsBox(BODY_WIDTH/2f, BODY_HEIGHT/2f);
		fdef.filter.categoryBits = GameInfo.ROBOT_BIT;
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT |
				GameInfo.ROBOT_BIT |
				GameInfo.MARIO_ROBOSENSOR_BIT;

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
	public void onHeadBounce(PlayerRole perp, Vector2 fromPos) {
		if(isDead)
			return;

		isHeadBounced = true;
		this.perp = perp;
		if(isSliding)
			cancelSlide();
		else if(isHiding) {
			if(fromPos.x > b2body.getPosition().x)
				initSlide(false);	// slide right
			else
				initSlide(true);	// slide left
		}
		else
			isHiding = true;
	}

	private void initSlide(boolean right) {
		isSliding = true;
		facingRight = right;
		if(right)
			velocity.x = SLIDE_VEL;
		else
			velocity.x = -SLIDE_VEL;
	}

	private void cancelSlide() {
		isSliding = false;
		if(velocity.x > 0)
			velocity.set(WALK_VEL, 0f);
		else
			velocity.set(-WALK_VEL, 0f);
	}

	@Override
	public void onTouchRobot(RobotRole robo) {
		// if walking normally and touched another robot then reverse direction
		if(!isHiding && !isSliding)
			reverseVelocity(true, false);
		else if(isSliding) {
			// if hit another sliding turtle, then both die
			if(robo instanceof Turtle && ((Turtle) robo).isSliding) {
				((DamageableBot) robo).onDamage(perp, 1f, b2body.getPosition());
				onDamage(perp, 1f, robo.getPosition());
			}
			// else if sliding and strikes a dmgable bot...
			else if(robo instanceof DamageableBot) {
				// give the dmgable bot a null player ref so that it doesn't give points, we will give points here
				((DamageableBot) robo).onDamage(null, 1f, robo.getPosition());
				runner.playSound(GameInfo.SOUND_KICK);
				if(perp != null) {
					slidingTotal = slidingTotal.increment();
					runner.givePointsToPlayer(perp, slidingTotal, true, b2body.getPosition(), GameInfo.P2M(16), false);
				}
			}
		}
	}

	// Foot sensor might come into contact with multiple boundary lines, so increment for each contact start,
	// and decrement for each contact end. If onGroundCount reaches zero then turtle is not on the ground.
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
		if(!seg.isHorizontal) {
			reverseVelocity(true,  false);
			if(isSliding)
				runner.playSound(GameInfo.SOUND_BUMP);
		}
	}

	@Override
	public void onBump(PlayerRole perp, Vector2 fromCenter) {
		this.perp = perp;
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
	public void onDamage(PlayerRole perp, float amount, Vector2 fromCenter) {
		this.perp = perp;
		isDead = true;
		if(fromCenter.x < b2body.getPosition().x)
			isDeadToRight = true;
		else
			isDeadToRight = false;
	}

	 // the player can "kick" a turtle hiding in its shell
	public void onPlayerTouch(PlayerRole perp, Vector2 position) {
		if(isDead)
			return;

		// a living turtle hiding in the shell and not sliding can be "pushed" to slide
		if(isHiding && !isSliding) {
			this.perp = perp;
			// pushed from left?
			if(position.x < b2body.getPosition().x)
				initSlide(true);	// slide right
			else
				initSlide(false);	// slide left
		}
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(b2body.getPosition().x - BODY_WIDTH/2f, b2body.getPosition().y - BODY_HEIGHT/2,
				BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public Vector2 getPosition() {
		return b2body.getPosition();
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
