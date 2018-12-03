package com.ridicarus.kid.bodies;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.GameInfo.Direction4;
import com.ridicarus.kid.collisionmap.LineSeg;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.roles.player.MarioRole;
import com.ridicarus.kid.roles.player.MarioRole.MarioPowerState;
import com.ridicarus.kid.roles.robot.DamageableBot;
import com.ridicarus.kid.roles.robot.HeadBounceBot;
import com.ridicarus.kid.roles.robot.ItemBot;
import com.ridicarus.kid.roles.robot.Levelend;
import com.ridicarus.kid.roles.robot.TouchDmgBot;
import com.ridicarus.kid.roles.robot.SMB.Flagpole;
import com.ridicarus.kid.roles.robot.SMB.PipeEntrance;
import com.ridicarus.kid.roles.robot.SMB.Turtle;
import com.ridicarus.kid.tiles.InteractiveTileObject;
import com.ridicarus.kid.tools.BasicInputs;
import com.ridicarus.kid.worldrunner.WorldRunner;

public class MarioBody implements PlayerBody {
	private static final float MARIO_WALKMOVE_XIMP = 0.025f;
	private static final float MARIO_MIN_WALKSPEED = MARIO_WALKMOVE_XIMP * 2;
	private static final float MARIO_RUNMOVE_XIMP = MARIO_WALKMOVE_XIMP * 1.5f;
	private static final float DECEL_XIMP = MARIO_WALKMOVE_XIMP * 1.37f;
	private static final float MARIO_BRAKE_XIMP = MARIO_WALKMOVE_XIMP * 2.75f;
	private static final float MARIO_BRAKE_TIME = 0.2f;
	private static final float MARIO_MAX_WALKVEL = MARIO_WALKMOVE_XIMP * 42f;
	private static final float MARIO_MAX_RUNVEL = MARIO_MAX_WALKVEL * 1.65f;
	private static final float MARIO_JUMP_IMPULSE = 1.75f;
	private static final float MARIO_JUMP_FORCE = 14f;
	private static final float MARIO_AIRMOVE_XIMP = 0.04f;
	private static final float MARIO_RUNJUMP_MULT = 0.25f;
	private static final float MARIO_MAX_RUNJUMPVEL = MARIO_MAX_RUNVEL;
	private static final float MARIO_JUMP_GROUNDCHECK_DELAY = 0.05f;
	private static final float MARIO_JUMPFORCE_TIME = 0.5f;
	private static final float MARIO_HEADBOUNCE_VEL = 1.75f;	// up velocity

	public enum MarioBodyState { STAND, WALKRUN, BRAKE, JUMP, FALL, DUCK, DEAD };

	private MarioRole role;
	private WorldRunner runner;
	private Body b2body;
	private Fixture marioBodyFixture;	// for making mario invincible after damage

	private boolean isFacingRight;
	private boolean isBig;
	private int onGroundCount;
	private boolean isOnGround;
	private boolean isBrakeAvailable;
	private float brakeTimer;
	private boolean isNewJumpAllowed;
	private float jumpGroundCheckTimer;
	private boolean isJumping;
	private float jumpForceTimer;
	private boolean isDucking;
	private boolean isHeadBouncing;
	private boolean isTakeDamage;
	private boolean canHeadBang;
	private Array<InteractiveTileObject> headHits;

	private LinkedBlockingQueue<PipeEntrance> adjacentPipeAddQ;
	private LinkedBlockingQueue<PipeEntrance> adjacentPipeRemoveQ;
	private LinkedList<PipeEntrance> adjacentPipes;
	private PipeEntrance pipeToEnter;

	private Flagpole flagpoleTouched;
	private Levelend levelendTouched;

	private MarioBodyState curState;
	private float stateTimer;

	public MarioBody(MarioRole role, WorldRunner runner, Vector2 position, boolean isFacingRight, boolean isBig) {
		this.role = role;
		this.runner = runner;

		this.isFacingRight = isFacingRight;
		this.isBig = isBig;
		onGroundCount = 0;
		isOnGround = false;
		isBrakeAvailable = true;
		brakeTimer = 0f;
		isNewJumpAllowed = false;
		jumpGroundCheckTimer = 0f;
		isJumping = false;
		jumpForceTimer = 0f;
		isDucking = false;
		isHeadBouncing = false;
		isTakeDamage = false;
		canHeadBang = true;
		headHits = new Array<InteractiveTileObject>();

		adjacentPipes = new LinkedList<PipeEntrance>();
		adjacentPipeAddQ = new LinkedBlockingQueue<PipeEntrance>();
		adjacentPipeRemoveQ = new LinkedBlockingQueue<PipeEntrance>();
		pipeToEnter = null;
		flagpoleTouched = null;
		levelendTouched = null;

		curState = MarioBodyState.STAND;
		stateTimer = 0f;

		// physic
		defineBody(position, new Vector2(0f, 0f));
	}

	private void processHeadBounces() {
		if(isHeadBouncing) {
			isHeadBouncing = false;
			b2body.setLinearVelocity(b2body.getLinearVelocity().x, 0f);
			b2body.applyLinearImpulse(new Vector2(0f, MARIO_HEADBOUNCE_VEL), b2body.getWorldCenter(), true);
		}
	}

	// process the list of head hits for a head bang
	private void processHeadHits() {
		// check the list of tiles for the closest to mario
		float closest = 0;
		InteractiveTileObject closestTile = null;
		for(InteractiveTileObject thingHit : headHits) {
			float dist = Math.abs(thingHit.getPosition().x - b2body.getPosition().x);
			if(closestTile == null || dist < closest) {
				closest = dist;
				closestTile = thingHit;
			}
		}
		headHits.clear();

		// we have a weiner!
		if(closestTile != null) {
			canHeadBang = false;
			closestTile.onHeadHit(role);
		}
		// mario can headbang once per up/down cycle of movement, so re-enable head bang when mario moves down
		else if(b2body.getLinearVelocity().y < 0f)
			canHeadBang = true;
	}

	private void processPipes(BasicInputs bi) {
		// process the add/remove queues
		while(!adjacentPipeAddQ.isEmpty()) {
			PipeEntrance adj = adjacentPipeAddQ.poll();
			if(!adjacentPipes.contains(adj))
				adjacentPipes.add(adj);
		}
		while(!adjacentPipeRemoveQ.isEmpty()) {
			PipeEntrance adj = adjacentPipeRemoveQ.poll();
			if(adjacentPipes.contains(adj))
				adjacentPipes.remove(adj);
		}

		// check for pipe entry 
		Direction4 dir = null;
		if(bi.wantsToGoRight)
			dir = Direction4.RIGHT;
		else if(bi.wantsToGoUp)
			dir = Direction4.UP;
		else if(bi.wantsToGoLeft)
			dir = Direction4.LEFT;
		else if(bi.wantsToGoDown)
			dir = Direction4.DOWN;
		else
			return;
		for(PipeEntrance ent : adjacentPipes) {
			if(ent.canPlayerEnterPipe(this, dir)) {
				// player can enter pipe, so save a ref to the pipe
				pipeToEnter = ent;
				return;
			}
		}
	}

	public MarioBodyState update(float delta, BasicInputs bi, MarioPowerState curPowerState) {
		MarioBodyState nextState;
		boolean isVelocityLeft, isVelocityRight;
		boolean doWalkRunMove;
		boolean doDecelMove;
		boolean doBrakeMove;

		processPipes(bi);	// moving into warp pipes
		processHeadHits();	// hitting bricks with his head
		processHeadBounces();	// bouncing on heads of goombas, turtles, etc.

		nextState = MarioBodyState.STAND;
		isVelocityRight = b2body.getLinearVelocity().x > MARIO_MIN_WALKSPEED;
		isVelocityLeft = b2body.getLinearVelocity().x < -MARIO_MIN_WALKSPEED;

		// if mario's velocity is below min walking speed while on ground then set velocity to 0
		if(isOnGround && !isVelocityRight && !isVelocityLeft && !bi.wantsToGoRight && !bi.wantsToGoLeft)
			b2body.setLinearVelocity(0f, b2body.getLinearVelocity().y);

		// multiple concurrent body impulses may be necessary
		doWalkRunMove = false;
		doDecelMove = false;
		doBrakeMove = false;

		// eligible for duck/unduck?
		if(curPowerState != MarioPowerState.SMALL && isOnGround) {
			// first time duck check
			if(bi.wantsToGoDown && !isDucking) {
				// quack
				isDucking = true;
				defineBody(b2body.getPosition().cpy().sub(0f, GameInfo.P2M(8f)), b2body.getLinearVelocity());
			}
			// first time unduck check
			else if(!bi.wantsToGoDown && isDucking) {
				// kcauq
				isDucking = false;
				defineBody(b2body.getPosition().cpy().add(0f, GameInfo.P2M(8f)), b2body.getLinearVelocity());
			}
		}

		// want to move left or right? (but not both! because they would cancel each other)
		if((bi.wantsToGoRight && !bi.wantsToGoLeft) || (!bi.wantsToGoRight && bi.wantsToGoLeft)) {
			doWalkRunMove = true;

			// mario can change facing direction, but not while airborne
			if(isOnGround) {
				// brake becomes available again when facing direction changes
				if(isFacingRight != bi.wantsToGoRight) {
					isBrakeAvailable = true;
					brakeTimer = 0f;
				}

				// can't run/walk on ground while ducking, only slide
				if(isDucking) {
					doWalkRunMove = false;
					doDecelMove = true;
				}
				else	// update facing direction
					isFacingRight = bi.wantsToGoRight;
			}
		}
		// decelerate if on ground and not wanting to move left or right
		else if(isOnGround && (isVelocityRight || isVelocityLeft))
			doDecelMove = true;

		// check for brake application
		if(!isDucking && isOnGround && isBrakeAvailable &&
				((isFacingRight && isVelocityLeft) || (!isFacingRight && isVelocityRight))) {
			isBrakeAvailable = false;
			brakeTimer = MARIO_BRAKE_TIME;
		}
		// this catches brake applications from this update() call and previous update() calls
		if(brakeTimer > 0f) {
			doBrakeMove = true;
			brakeTimer -= delta;
		}

		// apply impulses if necessary
		if(doBrakeMove) {
			brakeLeftRight(isFacingRight);
			nextState = MarioBodyState.BRAKE;
		}
		else if(doWalkRunMove) {
			moveBodyLeftRight(bi.wantsToGoRight, bi.wantsToRun);
			nextState = MarioBodyState.WALKRUN;
		}
		else if(doDecelMove) {
			decelLeftRight(isFacingRight);
			nextState = MarioBodyState.WALKRUN;
		}

		// Do not check mario's "on ground" status for a short time after mario jumps, because his foot sensor
		// might still be touching the ground even after his body enters the air.
		if(jumpGroundCheckTimer > delta)
			jumpGroundCheckTimer -= delta;
		else {
			jumpGroundCheckTimer = 0f;
			// The player can jump once per press of the jump key, so let them jump again when they release the
			// button but, they need to be on the ground with the button released.
			if(isOnGround) {
				isJumping = false;
				if(!bi.wantsToJump)
					isNewJumpAllowed = true;
			}
		}

		// jump?
		if(bi.wantsToJump && isNewJumpAllowed) {	// do jump
			isNewJumpAllowed = false;
			isJumping = true;
			// start a timer to delay checking for onGround status
			jumpGroundCheckTimer = MARIO_JUMP_GROUNDCHECK_DELAY;
			nextState = MarioBodyState.JUMP;

			// the faster mario is moving, the higher he jumps, up to a max
			float mult = Math.abs(b2body.getLinearVelocity().x) / MARIO_MAX_RUNJUMPVEL;
			// cap the multiplier
			if(mult > 1f)
				mult = 1f;

			mult *= (float) MARIO_RUNJUMP_MULT;
			mult += 1f;

			// apply initial (and only) jump impulse
			moveBodyY(MARIO_JUMP_IMPULSE * mult);
			// the remainder of the jump up velocity is achieved through mid-air up-force
			jumpForceTimer = MARIO_JUMPFORCE_TIME;
			if(curPowerState != MarioPowerState.SMALL)
				runner.playSound(GameInfo.SOUND_MARIOBIGJUMP);
			else
				runner.playSound(GameInfo.SOUND_MARIOSMLJUMP);
		}
		else if(isJumping) {	// jumped and is mid-air
			nextState = MarioBodyState.JUMP;
			// jump force stops, and cannot be restarted, if the player releases the jump key
			if(!bi.wantsToJump)
				jumpForceTimer = 0f;
			// The longer the player holds the jump key, the higher they go,
			// if mario is moving up (no jump force allowed while mario is moving down)
			// TODO: what if mario is initally moving down because he jumped from an elevator?
			else if(b2body.getLinearVelocity().y > 0f && jumpForceTimer > 0f) {
				jumpForceTimer -= delta;
				// the force was strong to begin and tapered off over time - some said it became irrelevant
				useTheForceMario(MARIO_JUMP_FORCE * jumpForceTimer / MARIO_JUMPFORCE_TIME);
			}
		}
		// finally, if mario is not on the ground (for reals) then he is falling since he is not jumping
		else if(!isOnGround && jumpGroundCheckTimer <= 0f) {
			// cannot jump while falling
			isNewJumpAllowed = false;
			nextState = MarioBodyState.FALL;
		}

		if(isDucking)
			nextState = MarioBodyState.DUCK;

		stateTimer = nextState == curState ? stateTimer + delta : 0f;
		curState = nextState;

		return nextState;
	}
 
	private void defineBody(Vector2 position, Vector2 velocity) {
		createB2Body(position, velocity);
		createBodyFixture();
		createTopSensorFixture();
		createBottomSensorFixture();
		createSideSensorFixtures();
		createRobotSensorFixture();
	}

	private void createB2Body(Vector2 position, Vector2 velocity) {
		BodyDef bdef;
	
		if(b2body != null)
			runner.getWorld().destroyBody(b2body);

		bdef = new BodyDef();
		bdef.position.set(position);
		bdef.linearVelocity.set(velocity);
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = runner.getWorld().createBody(bdef);
	}

	private void createBodyFixture() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape bodyShape = new PolygonShape();
		Vector2 bs = getB2BodySize();

		bodyShape.setAsBox(bs.x/2f, bs.y/2f);

		fdef.filter.categoryBits = GameInfo.MARIO_BIT;
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT | GameInfo.DESPAWN_BIT;
		fdef.shape = bodyShape;
		// mario should slide easily, but still have some friction to prevent sliding forever
		fdef.friction = 0.01f;	// (default is 0.2f)

		// save a ref to the body fixture for use later when mario has dmg invincibility
		marioBodyFixture = b2body.createFixture(fdef);
		marioBodyFixture.setUserData(this);
	}

	private void createTopSensorFixture() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape sensorShape = new PolygonShape();

		// head sensor for detecting head banging behavior
		if(!isBig || isDucking)
			sensorShape.setAsBox(GameInfo.P2M(5f), GameInfo.P2M(1f), new Vector2(GameInfo.P2M(0f), GameInfo.P2M(8f)), 0f);
		else
			sensorShape.setAsBox(GameInfo.P2M(5f), GameInfo.P2M(1f), new Vector2(GameInfo.P2M(0f), GameInfo.P2M(16f)), 0f);
		fdef.filter.categoryBits = GameInfo.MARIOHEAD_BIT;
		fdef.filter.maskBits = GameInfo.BANGABLE_BIT | GameInfo.PIPE_BIT;
		fdef.shape = sensorShape;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(this);
	}

	private void createBottomSensorFixture() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape sensorShape = new PolygonShape();

		// foot sensor for detecting onGround and warp pipes
		if(!isBig || isDucking)
			sensorShape.setAsBox(GameInfo.P2M(5f), GameInfo.P2M(2f), new Vector2(0f, GameInfo.P2M(-6)), 0f);
		else
			sensorShape.setAsBox(GameInfo.P2M(5f), GameInfo.P2M(2f), new Vector2(0f, GameInfo.P2M(-16)), 0f);
		fdef.filter.categoryBits = GameInfo.MARIOFOOT_BIT;
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT | GameInfo.PIPE_BIT;
		fdef.shape = sensorShape;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(this);
	}

	private void createSideSensorFixtures() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape sensorShape = new PolygonShape();

		// right side sensor for detecting warp pipes
		sensorShape.setAsBox(GameInfo.P2M(1f), GameInfo.P2M(5f), new Vector2(GameInfo.P2M(7), 0f), 0f);
		fdef.filter.categoryBits = GameInfo.MARIOSIDE_BIT;
		fdef.filter.maskBits = GameInfo.PIPE_BIT;
		fdef.shape = sensorShape;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(this);

		// left side sensor for detecting warp pipes
		sensorShape.setAsBox(GameInfo.P2M(1f), GameInfo.P2M(5f), new Vector2(GameInfo.P2M(-7), 0f), 0f);
		fdef.filter.categoryBits = GameInfo.MARIOSIDE_BIT;
		fdef.filter.maskBits = GameInfo.PIPE_BIT;
		fdef.shape = sensorShape;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(this);
	}

	private void createRobotSensorFixture() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape bodyShape = new PolygonShape();
		Vector2 bs = getB2BodySize();

		bodyShape.setAsBox(bs.x/2f, bs.y/2f);

		// Create a robot sensor, so that mario doesn't collide with goombas or items like mushrooms and slow down -
		// he should only sense when they contact
		fdef.filter.categoryBits = GameInfo.MARIO_ROBOSENSOR_BIT;
		fdef.filter.maskBits = GameInfo.ROBOT_BIT | GameInfo.ITEM_BIT;
		fdef.shape = bodyShape;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(this);
	}

	private void decelLeftRight(boolean right) {
		float vx = b2body.getLinearVelocity().x;
		if(vx == 0f)
			return;

		if(vx > 0f)
			b2body.applyLinearImpulse(new Vector2(-DECEL_XIMP, 0f), b2body.getWorldCenter(), true);
		else if(vx < 0f)
			b2body.applyLinearImpulse(new Vector2(DECEL_XIMP, 0f), b2body.getWorldCenter(), true);

		// do not decel so hard he moves in opposite direction
		if((vx > 0f && b2body.getLinearVelocity().x < 0f) || (vx < 0f && b2body.getLinearVelocity().x > 0f))
			b2body.setLinearVelocity(0f, b2body.getLinearVelocity().y);
	}

	public void moveBodyLeftRight(boolean right, boolean doRunRun) {
		float speed, max;
		if(isOnGround)
			speed = doRunRun ? MARIO_RUNMOVE_XIMP : MARIO_WALKMOVE_XIMP;
		else {
			speed = MARIO_AIRMOVE_XIMP;
			if(isDucking)
				speed /= 2f;
		}
		if(doRunRun)
			max = MARIO_MAX_RUNVEL;
		else
			max = MARIO_MAX_WALKVEL;
		if(right && b2body.getLinearVelocity().x <= max)
			b2body.applyLinearImpulse(new Vector2(speed, 0f), b2body.getWorldCenter(), true);
		else if(!right && b2body.getLinearVelocity().x >= -max)
			b2body.applyLinearImpulse(new Vector2(-speed, 0f), b2body.getWorldCenter(), true);
	}

	private void brakeLeftRight(boolean right) {
		float vx = b2body.getLinearVelocity().x;
		if(vx == 0f)
			return;

		if(right && vx < 0f)
			b2body.applyLinearImpulse(new Vector2(MARIO_BRAKE_XIMP, 0f), b2body.getWorldCenter(),  true);
		else if(!right && vx > 0f)
			b2body.applyLinearImpulse(new Vector2(-MARIO_BRAKE_XIMP, 0f), b2body.getWorldCenter(),  true);

		// do not brake so hard he moves in opposite direction
		if((vx > 0f && b2body.getLinearVelocity().x < 0f) || (vx < 0f && b2body.getLinearVelocity().x > 0f))
			b2body.setLinearVelocity(0f, b2body.getLinearVelocity().y);
	}

	private void moveBodyY(float value) {
		b2body.applyLinearImpulse(new Vector2(0, value),
				b2body.getWorldCenter(), true);
	}

	private void useTheForceMario(float notMyFather) {
		b2body.applyForce(new Vector2(0, notMyFather), b2body.getWorldCenter(), true);
	}

	// Foot sensor might come into contact with multiple boundary lines, so increment for each contact start,
	// and decrement for each contact end. If onGroundCount reaches zero then mario's foot sensor is not touching
	// a boundary line, hence mario is not on the ground.
	@Override
	public void onFootTouchBound(LineSeg seg) {
		if(!seg.isHorizontal)
			return;

		onGroundCount++;
		isOnGround = true;
	}

	@Override
	public void onFootLeaveBound(LineSeg seg) {
		if(!seg.isHorizontal)
			return;

		onGroundCount--;
		if(onGroundCount == 0)
			isOnGround = false;
	}

	@Override
	public void onTouchRobot(RobotRole robo) {
		// If the bottom of mario sprite is at least as high as the middle point of the robot sprite, then the robot
		// takes damage. Otherwise mario takes damage.
		float marioY = b2body.getPosition().y;
		float robotY = robo.getPosition().y;

		// touch end of level flagpole?
		if(robo instanceof Flagpole)
			flagpoleTouched = (Flagpole) robo;
		else if(robo instanceof Levelend)
			levelendTouched = (Levelend) robo;
		// test for powerstar damage
		else if(robo instanceof DamageableBot && role.isPowerStarOn()) {
			// playSound should go in the processBody method, but... this is so much easier!
			runner.playSound(GameInfo.SOUND_KICK);
			((DamageableBot) robo).onDamage(role, 1f, b2body.getPosition());
		}
		// test for bounce on head
		else if(robo instanceof HeadBounceBot && marioY - (getB2BodySize().y/2f) >= robotY) {
			((HeadBounceBot) robo).onHeadBounce(role, b2body.getPosition());
			isHeadBouncing = true;
		}
		// does the robot do touch damage? (from non-head bounce source)
		else if(robo instanceof TouchDmgBot && ((TouchDmgBot) robo).isTouchDamage()) {
			if(role.isDmgInvincibleOn())
				return;
			isTakeDamage = true;
		}
		else if(robo instanceof Turtle)
			((Turtle) robo).onPlayerTouch(role, b2body.getPosition());	// push shell
	}

	@Override
	public void onTouchItem(RobotRole robo) {
		if(robo instanceof ItemBot)
			((ItemBot) robo).use(role);
	}

	@Override
	public void onHeadHit(InteractiveTileObject thing) {
		// After banging his head while moving up, mario cannot bang his head again until he has moved down a
		// sufficient amount.
		// Also, mario can only break one block per head bang - but if his head touches multiple blocks when
		// he hits, then choose the block closest to mario on the x axis.

		// if can bang and is moving up, keep track of things that head hit - check the list once per update
		if(canHeadBang && b2body.getLinearVelocity().y > 0f) {
			headHits.add(thing);
		}
	}

	@Override
	public void onStartTouchPipe(PipeEntrance pipeEnt) {
		if(!adjacentPipeAddQ.contains(pipeEnt))
			adjacentPipeAddQ.add(pipeEnt);
	}

	@Override
	public void onEndTouchPipe(PipeEntrance pipeEnt) {
		if(!adjacentPipeRemoveQ.contains(pipeEnt))
			adjacentPipeRemoveQ.add(pipeEnt);
	}

	public void respawn() {
		resetPipeToEnter();
		onGroundCount = 0;
		adjacentPipes.clear();
	}

	// use defineBody method to re-enable contacts
	public void disableContacts() {
		// make mario untouchable while warping down a pipe
		Filter filter = new Filter();
		filter.maskBits = GameInfo.NOTHING_BIT;
		for(Fixture fixture : b2body.getFixtureList())
			fixture.setFilterData(filter);
	}

	public void enableRobotContact() {
		Filter filter = new Filter();
		filter.categoryBits = GameInfo.MARIO_BIT;
		filter.maskBits = GameInfo.BOUNDARY_BIT | GameInfo.ROBOT_BIT;
		marioBodyFixture.setFilterData(filter);
	}

	public void disableRobotContact() {
		// ensure mario cannot collide with enemies
		Filter filter = new Filter();
		filter.categoryBits = GameInfo.MARIO_BIT;
		filter.maskBits = GameInfo.BOUNDARY_BIT;
		marioBodyFixture.setFilterData(filter);
	}

	@Override
	public float getStateTimer() {
		return stateTimer;
	}

	@Override
	public Vector2 getPosition() {
		return b2body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		Vector2 s = getB2BodySize();
		return new Rectangle(b2body.getPosition().x - s.x/2f, b2body.getPosition().y - s.y/2f, s.x, s.y);
	}

	public PipeEntrance getPipeToEnter() {
		return pipeToEnter;
	}

	public void resetPipeToEnter() {
		pipeToEnter = null;
	}

	public Flagpole getFlagpoleTouched() {
		return flagpoleTouched;
	}

	public void resetFlagpoleTouched() {
		this.flagpoleTouched = null;
	}

	public Levelend getLevelEndTouched() {
		return levelendTouched;
	}

	public boolean isOnGround() {
		return isOnGround;
	}

	public boolean isFacingRight() {
		return isFacingRight;
	}

	public void setFacingRight(boolean isFacingRight) {
		this.isFacingRight = isFacingRight; 
	}

	public void setVelocity(float x, float y) {
		b2body.setLinearVelocity(x, y);
	}

	public void setPosAndVel(Vector2 pos, Vector2 vel) {
		defineBody(pos, vel);
	}

	public void setBodyPosVelAndSize(Vector2 pos, Vector2 vel, boolean isBig) {
		this.isBig = isBig;
		isDucking = false;
		defineBody(pos, vel);
	}

	public void zeroVelocity(boolean zeroX, boolean zeroY) {
		b2body.setLinearVelocity(zeroX ? 0f : b2body.getLinearVelocity().x, zeroY ? 0f : b2body.getLinearVelocity().y);
	}

	public void applyImpulse(Vector2 imp) {
		b2body.applyLinearImpulse(imp, b2body.getWorldCenter(), true);
	}

	public void enableGravity() {
		b2body.setGravityScale(1f);
	}

	public void disableGravity() {
		b2body.setGravityScale(0f);
	}

	public Vector2 getVelocity() {
		return b2body.getLinearVelocity();
	}

	public boolean isDucking() {
		return isDucking;
	}

	public boolean isBigBody() {
		// not really "fake" height", rather it's the ideal height in pixels 
		if(!isBig || isDucking)
			return false;
		else
			return true;
	}

	public Vector2 getB2BodySize() {
		if(!isBig || isDucking)
			return new Vector2(GameInfo.P2M(7f * 2), GameInfo.P2M(6f * 2));
		else
			return new Vector2(GameInfo.P2M(7f * 2), GameInfo.P2M(13f * 2));
	}

	public boolean getAndResetTakeDamage() {
		boolean t = isTakeDamage;
		isTakeDamage = false;
		return t;
	}

	@Override
	public void onTouchDespawn() {
		role.die();
	}
}
