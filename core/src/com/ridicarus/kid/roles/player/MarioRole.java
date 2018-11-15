/*
 * By: David Loucks
 * Approx. Date: 2018.11.08
*/

package com.ridicarus.kid.roles.player;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.MyKidRidicarus;
import com.ridicarus.kid.SpecialTiles.InteractiveTileObject;
import com.ridicarus.kid.collisionmap.LineSeg;
import com.ridicarus.kid.roles.PlayerRole;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.roles.robot.HeadBounceBot;
import com.ridicarus.kid.roles.robot.ItemRobot;
import com.ridicarus.kid.roles.robot.ItemRobot.PowerupType;
import com.ridicarus.kid.roles.robot.MarioFireball;
import com.ridicarus.kid.roles.robot.TouchDmgBot;
import com.ridicarus.kid.roles.robot.Turtle;
import com.ridicarus.kid.roles.robot.WalkingRobot;
import com.ridicarus.kid.sprites.MarioSprite;
import com.ridicarus.kid.tools.WorldRunner;

public class MarioRole implements PlayerRole {
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

	private static final float DMG_INVINCIBLE_TIME = 3f;

	public enum MarioPowerState { SMALL, BIG, FIRE };
	// the body may receive different impulses than the sprite receives texture regions
	public enum MarioCharState { STAND, WALKRUN, BRAKE, JUMP, FALL, FIREBALL, DIE };

	private WorldRunner runner;
	private MarioSprite marioSprite;
	private Body b2body;
	private Fixture marioBodyFixture;	// for making mario invincible after damage

	private MarioCharState curCharState;
	private float stateTimer;

	private boolean wantsToGoRight, wantsToGoLeft, wantsToJump, wantsToRun;

	private boolean marioIsDead;
	private boolean isFacingRight;

	private int onGroundCount;
	private boolean isOnGround;
	private boolean isNewJumpAllowed;

	private boolean isDmgInvincible;
	private float dmgInvincibleTime;

	private boolean canHeadBang;
	private Array<InteractiveTileObject> headHits;

	private MarioPowerState curPowerState;

	private boolean isTakeDamage;
	private boolean isHeadBouncing;
	private PowerupType receivedPowerup;

	private static final float TIME_PER_FIREBALL = 0.5f;
	private float fireballTimer;
	private boolean wantsToRunOnPrevUpdate;
	private boolean isBrakeAvailable;
	private float brakeTimer;
	private float jumpGroundCheckTimer;
	private boolean isJumping;
	private float jumpForceTimer;

	public MarioRole(WorldRunner runner, Vector2 position) {
		this.runner = runner;

		marioIsDead = false;
		isFacingRight = true;
		onGroundCount = 0;
		isOnGround = false;
		isNewJumpAllowed = false;
		isDmgInvincible = false;
		dmgInvincibleTime = 0f;
		canHeadBang = true;
		headHits = new Array<InteractiveTileObject>();

		curCharState = MarioCharState.STAND;
		stateTimer = 0f;
		curPowerState = MarioPowerState.SMALL;

		isTakeDamage = false;
		isHeadBouncing = false;
		receivedPowerup = PowerupType.NONE;

		fireballTimer = TIME_PER_FIREBALL * 2f;
		wantsToRunOnPrevUpdate = false;

		isBrakeAvailable = true;
		brakeTimer = 0f;
		jumpGroundCheckTimer = 0f;
		isJumping = false;
		jumpForceTimer = 0f;

		// graphic
		marioSprite = new MarioSprite(runner.getAtlas(), curCharState, curPowerState, isFacingRight);
		// physic
		defineBody(position, new Vector2(0f, 0f), curPowerState);
	}

	// Process the body and return a character state based on the findings.
	private MarioCharState processBodyState(float delta) {
		MarioCharState returnState;
		boolean isVelocityLeft, isVelocityRight;
		boolean doWalkRunMove;
		boolean doDecelMove;
		boolean doBrakeMove;

		if(marioIsDead) {
			// make sure mario doesn't move left or right while dead
			b2body.setLinearVelocity(0f, b2body.getLinearVelocity().y);
			return MarioCharState.DIE;
		}

		returnState = MarioCharState.STAND;
		isVelocityRight = b2body.getLinearVelocity().x > MARIO_MIN_WALKSPEED;
		isVelocityLeft = b2body.getLinearVelocity().x < -MARIO_MIN_WALKSPEED;

		// multiple concurrent body impulses may be necessary
		doWalkRunMove = false;
		doDecelMove = false;
		doBrakeMove = false;
		// want to move left or right? (but not both! because they would cancel each other)
		if((wantsToGoRight && !wantsToGoLeft) || (!wantsToGoRight && wantsToGoLeft)) {
			doWalkRunMove = true;

			// mario can change facing direction, but not while airborne
			if(isOnGround) {
				// brake becomes available again when facing direction changes
				if(isFacingRight != wantsToGoRight) {
					isBrakeAvailable = true;
					brakeTimer = 0f;
				}

				// update facing direction
				isFacingRight = wantsToGoRight;
			}
		}
		// decelerate if on ground and not wanting to move left or right
		else if(isOnGround && ((isFacingRight && isVelocityRight) || (!isFacingRight && isVelocityLeft)))
			doDecelMove = true;

		// check for brake application
		if(isOnGround && isBrakeAvailable && ((isFacingRight && isVelocityLeft) || (!isFacingRight && isVelocityRight))) {
			isBrakeAvailable = false;
			brakeTimer = MARIO_BRAKE_TIME;
		}
		// this catches brake applications from this update() call and previous update() calls
		if(brakeTimer > 0f) {
			doBrakeMove = true;
			brakeTimer -= delta;
		}

		// apply impulses if necessary
		if(doWalkRunMove) {
			moveBodyLeftRight(wantsToGoRight, wantsToRun, isOnGround);
			returnState = MarioCharState.WALKRUN;
		}
		else if(doDecelMove) {	// cannot walk/run and decel at same time
			decelLeftRight(isFacingRight);
			returnState = MarioCharState.WALKRUN;
		}
		if(doBrakeMove) {
			brakeLeftRight(isFacingRight);
			// body brake overrides character's run animation
			returnState = MarioCharState.BRAKE;
		}

		// Do not check mario's "on ground" status for a short time after mario jumps, because his foot sensor
		// might still be touching the ground even after his body enters the air.
		if(jumpGroundCheckTimer > delta)
			jumpGroundCheckTimer -= delta;
		else {
			jumpGroundCheckTimer = 0f;
			isNewJumpAllowed = false;
			// The player can jump once per press of the jump key, so let them jump again when they release the
			// button but, they need to be on the ground with the button released.
			if(isOnGround) {
				isJumping = false;
				if(!wantsToJump)
					isNewJumpAllowed = true;
			}
		}

		// jump?
		if(wantsToJump && isNewJumpAllowed) {	// do jump
			isNewJumpAllowed = false;
			isJumping = true;
			// start a timer to delay checking for onGround status
			jumpGroundCheckTimer = MARIO_JUMP_GROUNDCHECK_DELAY;
			returnState = MarioCharState.JUMP;

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
		}
		else if(isJumping) {	// jumped and is mid-air
			returnState = MarioCharState.JUMP;
			// jump force stops, and cannot be restarted, if the player releases the jump key
			if(!wantsToJump)
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
		else if(!isOnGround && jumpGroundCheckTimer <= 0f)
			returnState = MarioCharState.FALL;

		return returnState;
	}

	@Override
	public void update(float delta) {
		MarioCharState nextState;
		boolean threwFireball;

		processHeadHits();	// hitting bricks with his head
		processHeadBounces();	// bouncing on heads of goombas, turtles, etc.
		threwFireball = processFireball(delta);
		processPowerups();
		processDamage(delta);

		nextState = processBodyState(delta);
		stateTimer = nextState == curCharState ? stateTimer + delta : 0f;
		curCharState = nextState;

		wantsToRunOnPrevUpdate = wantsToRun;

		wantsToGoRight = false;
		wantsToGoLeft = false;
		wantsToRun = false;
		wantsToJump = false;

		if(threwFireball)
			marioSprite.update(delta, b2body.getPosition(), MarioCharState.FIREBALL, curPowerState, isFacingRight, isDmgInvincible);
		else
			marioSprite.update(delta, b2body.getPosition(), nextState, curPowerState, isFacingRight, isDmgInvincible);
	}

	private void processHeadBounces() {
		if(isHeadBouncing) {
			isHeadBouncing = false;
			b2body.setLinearVelocity(b2body.getLinearVelocity().x, 0f);
			b2body.applyLinearImpulse(new Vector2(0f, MARIO_HEADBOUNCE_VEL), b2body.getWorldCenter(), true);
		}
	}

	// mario can shoot fireballs two at a time, but must wait if his "fireball timer" runs low
	private boolean processFireball(float delta) {
		fireballTimer += delta;
		if(fireballTimer > TIME_PER_FIREBALL)
			fireballTimer = TIME_PER_FIREBALL;

		// fire a ball?
		if(curPowerState == MarioPowerState.FIRE && wantsToRun && !wantsToRunOnPrevUpdate && fireballTimer > 0f) {
System.out.println("release fireball!!!" + ((float) Math.floorMod(System.currentTimeMillis(), 100000) / 1000f));
			fireballTimer -= TIME_PER_FIREBALL;
			throwFireball();
			return true;
		}

		return false;
	}

	private static final float FIREBALL_OFFSET = GameInfo.P2M(8f);
	private void throwFireball() {
		MarioFireball ball;

		if(isFacingRight)
			ball = new MarioFireball(runner, b2body.getPosition().cpy().add(FIREBALL_OFFSET, 0f), true);
		else
			ball = new MarioFireball(runner, b2body.getPosition().cpy().add(-FIREBALL_OFFSET, 0f), false);

		runner.addRobot(ball);
	}

	private void decelLeftRight(boolean right) {
		if(right)
			b2body.applyLinearImpulse(new Vector2(-DECEL_XIMP, 0f), b2body.getWorldCenter(), true);
		else
			b2body.applyLinearImpulse(new Vector2(DECEL_XIMP, 0f), b2body.getWorldCenter(), true);
	}

	private void moveBodyLeftRight(boolean right, boolean isRun, boolean isOnGround) {
		float speed, max;
		if(isOnGround)
			speed = isRun ? MARIO_RUNMOVE_XIMP : MARIO_WALKMOVE_XIMP;
		else
			speed = MARIO_AIRMOVE_XIMP;
		if(isRun)
			max = MARIO_MAX_RUNVEL;
		else
			max = MARIO_MAX_WALKVEL;
		if(right && b2body.getLinearVelocity().x <= max)
			b2body.applyLinearImpulse(new Vector2(speed, 0f), b2body.getWorldCenter(), true);
		else if(!right && b2body.getLinearVelocity().x >= -max)
			b2body.applyLinearImpulse(new Vector2(-speed, 0f), b2body.getWorldCenter(), true);
	}

	private void brakeLeftRight(boolean right) {
		if(right)
			b2body.applyLinearImpulse(new Vector2(MARIO_BRAKE_XIMP, 0f), b2body.getWorldCenter(),  true);
		else
			b2body.applyLinearImpulse(new Vector2(-MARIO_BRAKE_XIMP, 0f), b2body.getWorldCenter(),  true);
	}

	private void moveBodyY(float value) {
		b2body.applyLinearImpulse(new Vector2(0, value),
				b2body.getWorldCenter(), true);
	}

	private void useTheForceMario(float notMyFather) {
		b2body.applyForce(new Vector2(0, notMyFather), b2body.getWorldCenter(), true);
	}

	private void processPowerups() {
		// apply powerup if received
		switch(receivedPowerup) {
			case MUSHROOM:
				if(curPowerState == MarioPowerState.SMALL) {
					curPowerState = MarioPowerState.BIG;
					defineBody(b2body.getPosition().add(0f, GameInfo.P2M(8f)), b2body.getLinearVelocity(), curPowerState);
					MyKidRidicarus.manager.get(GameInfo.SOUND_POWERUP_USE, Sound.class).play(GameInfo.SOUND_VOLUME);
				}
				break;
			case FIREFLOWER:
				if(curPowerState == MarioPowerState.SMALL) {
					curPowerState = MarioPowerState.BIG;
					defineBody(b2body.getPosition().add(0f, GameInfo.P2M(8f)), b2body.getLinearVelocity(), curPowerState);
					MyKidRidicarus.manager.get(GameInfo.SOUND_POWERUP_USE, Sound.class).play(GameInfo.SOUND_VOLUME);
				}
				else if(curPowerState == MarioPowerState.BIG) {
					curPowerState = MarioPowerState.FIRE;
					defineBody(b2body.getPosition(), b2body.getLinearVelocity(), curPowerState);
					MyKidRidicarus.manager.get(GameInfo.SOUND_POWERUP_USE, Sound.class).play(GameInfo.SOUND_VOLUME);
				}
				break;
			case NONE:
				break;
		}

		receivedPowerup = PowerupType.NONE;
	}

	private void processDamage(float delta) {
		if(dmgInvincibleTime > 0f)
			dmgInvincibleTime -= delta;
		else if(isDmgInvincible)
			endDmgInvincibility();

		// apply damage if received
		if(isTakeDamage) {
			isTakeDamage = false;
			// fire mario loses fire
			if(curPowerState == MarioPowerState.FIRE) {
				curPowerState = MarioPowerState.BIG;
				defineBody(b2body.getPosition(), b2body.getLinearVelocity(), curPowerState);
				startDmgInvincibility();
				MyKidRidicarus.manager.get(GameInfo.SOUND_POWERDOWN, Sound.class).play(GameInfo.SOUND_VOLUME);
			}
			// big mario gets smaller
			else if(curPowerState == MarioPowerState.BIG) {
				curPowerState = MarioPowerState.SMALL;
				defineBody(b2body.getPosition().sub(0f, GameInfo.P2M(8f)), b2body.getLinearVelocity(), curPowerState);
				startDmgInvincibility();
				MyKidRidicarus.manager.get(GameInfo.SOUND_POWERDOWN, Sound.class).play(GameInfo.SOUND_VOLUME);
			}
			// die if small and not invincible
			else
				die();
		}
	}

	private void startDmgInvincibility() {
		isDmgInvincible = true;
		dmgInvincibleTime = DMG_INVINCIBLE_TIME;

		// ensure mario cannot collide with enemies
		Filter filter = new Filter();
		filter.categoryBits = GameInfo.MARIO_BIT;
		filter.maskBits = GameInfo.BOUNDARY_BIT;
		marioBodyFixture.setFilterData(filter);
	}

	private void endDmgInvincibility() {
		isDmgInvincible = false;
		dmgInvincibleTime = 0f;

		Filter filter = new Filter();
		filter.categoryBits = GameInfo.MARIO_BIT;
		filter.maskBits = GameInfo.BOUNDARY_BIT | GameInfo.ROBOT_BIT;
		marioBodyFixture.setFilterData(filter);
	}

	// process the list of head hits for a head bang
	private void processHeadHits() {
		float closest;
		InteractiveTileObject closestTile;

		// check the list of tiles for the closest to mario
		closest = GameInfo.MAX_FLOAT_HACK;
		closestTile = null;
		for(InteractiveTileObject thingHit : headHits) {
			float dist;
			dist = Math.abs(thingHit.getPosition().x - b2body.getPosition().x);
			if(closestTile == null || dist < closest) {
				closest = dist;
				closestTile = thingHit;
			}
		}
		headHits.clear();

		// we have a weiner!
		if(closestTile != null) {
			canHeadBang = false;
			closestTile.onHeadHit(this);
		}
		else {
			// mario can headbang once per up/down cycle of movement, so re-enable head bang when mario moves down
			if(b2body.getLinearVelocity().y < 0f)
				canHeadBang = true;
		}
	}

	private void defineBody(Vector2 position, Vector2 velocity, MarioPowerState subState ) {
		BodyDef bdef;
		FixtureDef fdef;
		PolygonShape marioShape;
		PolygonShape headSensor;
		PolygonShape footSensor;

		if(b2body != null)
			runner.getWorld().destroyBody(b2body);

		bdef = new BodyDef();
		bdef.position.set(position);
		bdef.linearVelocity.set(velocity);
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = runner.getWorld().createBody(bdef);

		fdef = new FixtureDef();
		marioShape = new PolygonShape();
		if(subState == MarioPowerState.SMALL)
			marioShape.setAsBox(GameInfo.P2M(7f),  GameInfo.P2M(6f));
		else
			marioShape.setAsBox(GameInfo.P2M(7f),  GameInfo.P2M(13f));

		fdef.filter.categoryBits = GameInfo.MARIO_BIT;
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT;

		fdef.shape = marioShape;
		fdef.friction = 0.01f;	// slide a bit more (default is 0.2)
		marioBodyFixture = b2body.createFixture(fdef);
		marioBodyFixture.setUserData(this);

		// head sensor for detecting head banging behavior
		headSensor = new PolygonShape();
		if(subState == MarioPowerState.SMALL)
			headSensor.setAsBox(GameInfo.P2M(2f), GameInfo.P2M(1f), new Vector2(GameInfo.P2M(0f), GameInfo.P2M(8f)), 0f);
		else
			headSensor.setAsBox(GameInfo.P2M(2f), GameInfo.P2M(1f), new Vector2(GameInfo.P2M(0f), GameInfo.P2M(16f)), 0f);

		fdef.filter.categoryBits = GameInfo.MARIOHEAD_BIT;
		fdef.filter.maskBits = GameInfo.BANGABLE_BIT;
		fdef.shape = headSensor;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(this);
		// foot sensor for detecting onGround
		footSensor = new PolygonShape();
		if(subState == MarioPowerState.SMALL)
			footSensor.setAsBox(GameInfo.P2M(5f), GameInfo.P2M(2f), new Vector2(0f, GameInfo.P2M(-6)), 0f);
		else
			footSensor.setAsBox(GameInfo.P2M(5f), GameInfo.P2M(2f), new Vector2(0f, GameInfo.P2M(-16)), 0f);

		fdef.filter.categoryBits = GameInfo.MARIOFOOT_BIT;
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT;
		fdef.shape = footSensor;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(this);

		// Create a robot sensor, so that mario doesn't collide with goombas or items like mushrooms and slow down -
		// he should only sense when they contact
		fdef.filter.categoryBits = GameInfo.MARIO_ROBOT_SENSOR_BIT;
		fdef.filter.maskBits = GameInfo.ROBOT_BIT | GameInfo.ITEM_BIT;
		fdef.shape = marioShape;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(this);
	}

	private void die() {
		if(!marioIsDead) {
			marioIsDead = true;
			MyKidRidicarus.manager.get("audio/music/mario_music.ogg", Music.class).stop();
			MyKidRidicarus.manager.get("audio/sounds/mariodie.wav", Sound.class).play();

			Filter filter = new Filter();
			filter.maskBits = GameInfo.NOTHING_BIT;
			for(Fixture fixture : b2body.getFixtureList())
				fixture.setFilterData(filter);

			b2body.setLinearVelocity(0f, 0f);
			b2body.applyLinearImpulse(new Vector2(0, 4f), b2body.getWorldCenter(), true);
		}
	}

	public void applyPowerup(PowerupType powerup) {
		// TODO: check if already received powerup, and check for rank
		receivedPowerup = powerup;
	}

	@Override
	public boolean isDead() {
		return marioIsDead;
	}

	@Override
	public Body getB2Body() {
		return b2body;
	}

	@Override
	public void draw(Batch batch) {
		marioSprite.draw(batch);
	}

	@Override
	public void rightIt() {
		wantsToGoRight = true;
	}

	@Override
	public void leftIt() {
		wantsToGoLeft = true;
	}

	@Override
	public void runIt() {
		wantsToRun = true;
	}

	@Override
	public void jumpIt() {
		wantsToJump = true;
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
		float robotY = robo.getBody().getPosition().y;
		float marioHeight = marioSprite.getHeight();

		if(robo instanceof WalkingRobot) {
			// test for bounce on head
			if(robo instanceof HeadBounceBot && marioY - (marioHeight/2f) >= robotY) {
				((HeadBounceBot) robo).onHeadBounce(b2body.getPosition());
				isHeadBouncing = true;
			}
			// does the robot do touch damage? (from non-head bounce source)
			else if(robo instanceof TouchDmgBot && ((TouchDmgBot) robo).isTouchDamage()) {
				if(dmgInvincibleTime > 0)
					return;
				isTakeDamage = true;
			}
			else if(robo instanceof Turtle)
				((Turtle) robo).onPlayerTouch(b2body.getPosition());	// push shell
		}
	}

	@Override
	public void onTouchItem(RobotRole robo) {
		if(robo instanceof ItemRobot) {
			((ItemRobot) robo).use(this);
		}
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
	public float getStateTimer() {
		return stateTimer;
	}

	public boolean isBig() {
		return (curPowerState != MarioPowerState.SMALL);
	}
}
