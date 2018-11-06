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
import com.ridicarus.kid.roles.robot.Goomba;
import com.ridicarus.kid.roles.robot.PowerMushroom;
import com.ridicarus.kid.sprites.MarioSprite;
import com.ridicarus.kid.tools.WorldRunner;

public class MarioRole implements PlayerRole {
	private static final float MARIO_WALKMOVE_XIMP = 0.042f;
	private static final float MARIO_MIN_WALKSPEED = MARIO_WALKMOVE_XIMP * 2;
	private static final float MARIO_RUNMOVE_XIMP = MARIO_WALKMOVE_XIMP * 1.5f;
	private static final float MARIO_MAX_WALKVEL = MARIO_WALKMOVE_XIMP * 42f;
	private static final float MARIO_MAX_RUNVEL = MARIO_MAX_WALKVEL * 1.65f;
	private static final float DECEL_XIMP = MARIO_WALKMOVE_XIMP * 1.37f;
	private static final float MARIO_BRAKE_XIMP = MARIO_WALKMOVE_XIMP * 2.75f;
	private static final float MARIO_BRAKE_TIME = 0.2f;

	private static final float MARIO_JUMP_IMPULSE = 1.75f;
	private static final float MARIO_JUMP_FORCE = 14f;
	private static final float MARIO_AIRMOVE_XIMP = 0.04f;
	private static final float MARIO_RUNJUMP_MULT = 0.25f;
	private static final float MARIO_MAX_RUNJUMPVEL = MARIO_MAX_RUNVEL;
	private static final float MARIO_JUMP_GROUNDCHECK_DELAY = 0.05f;
	private static final float MARIO_JUMPFORCE_TIME = 0.5f;

	private static final float DMG_INVINCIBLE_TIME = 3f;

	public enum MarioRoleState {
		STAND, RUN, JUMP, FALL, BRAKE, DEAD
	};

	private WorldRunner runner;
	private MarioSprite marioSprite;
	private Body b2body;
	private Fixture marioBodyFixture;	// for making mario invincible after damage

	private MarioRoleState curState;
	private float stateTimer;

	private boolean wantsToGoRight, wantsToGoLeft, wantsToJump, wantsToRun;

	private boolean marioIsDead;
	private boolean isBig;
	private boolean isFacingRight;

	private int onGroundCount;
	private boolean isOnGround;
	private boolean isJumpAllowed;
	private boolean isJumpForceAllowed;

	private boolean timeToDefineBigMario;
	private boolean timeToDefineSmallMario;
	private boolean isDmgInvincible;
	private float dmgInvincibleTime;

	private boolean canHeadBang;
	private Array<InteractiveTileObject> headHits;

	public MarioRole(WorldRunner runner, Vector2 position) {
		this.runner = runner;

		marioIsDead = false;
		isBig = false;
		isFacingRight = true;
		onGroundCount = 0;
		isOnGround = false;
		isJumpAllowed = false;
		isJumpForceAllowed = false;
		timeToDefineBigMario = false;
		timeToDefineSmallMario = false;
		isDmgInvincible = false;
		dmgInvincibleTime = 0f;
		canHeadBang = true;
		headHits = new Array<InteractiveTileObject>();

		curState = MarioRoleState.STAND;
		stateTimer = 0f;

		// graphic
		marioSprite = new MarioSprite(runner.getAtlas(), curState, isBig, isFacingRight, false);
		// physic
		defineBody(position, new Vector2(0f, 0f), isBig);
	}

	public void grow() {
		if(isBig)
			return;

		timeToDefineBigMario = true;
		isBig = true;
		MyKidRidicarus.manager.get(GameInfo.SOUND_POWERUP_USE, Sound.class).play(GameInfo.SOUND_VOLUME);
	}

	private void shrink() {
		if(!isBig)
			return;

		timeToDefineSmallMario = true;
		isBig = false;
		MyKidRidicarus.manager.get(GameInfo.SOUND_POWERDOWN, Sound.class).play(GameInfo.SOUND_VOLUME);
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

	private MarioRoleState getState() {
		MarioRoleState state;
		boolean wasFacingRight;
		boolean changeFacingDir;
		boolean isStartingJump;

		if(marioIsDead)
			return MarioRoleState.DEAD;

		// Do not check mario's "on ground" status for a short time after mario jumps, because his foot sensor
		// might still be touching the ground even after his body enters the air.
		if(curState == MarioRoleState.JUMP && stateTimer < MARIO_JUMP_GROUNDCHECK_DELAY)
			isStartingJump = true;
		else
			isStartingJump = false;

		// assume default STAND state to begin with, and state may change later
		state = MarioRoleState.STAND;
		wasFacingRight = isFacingRight;
		if(isOnGround && !isStartingJump) {
			if(wantsToGoRight && !wantsToGoLeft) {
				isFacingRight = true;
				state = MarioRoleState.RUN;
			}
			else if(wantsToGoLeft && !wantsToGoRight) {
				isFacingRight = false;
				state = MarioRoleState.RUN;
			}

			if(wantsToJump && isJumpAllowed)
				state = MarioRoleState.JUMP;
			else {
				// mario brakes when he changes direction while he has velocity
				changeFacingDir = isFacingRight != wasFacingRight ? true : false;

				// new direction right, old direction left, and velocity is still to the left?
				if(isFacingRight && changeFacingDir && b2body.getLinearVelocity().x < -MARIO_MIN_WALKSPEED)
					return MarioRoleState.BRAKE;
				// new direction left, old direction right, and velocity is still to the right?
				else if(!isFacingRight && changeFacingDir && b2body.getLinearVelocity().x > MARIO_MIN_WALKSPEED)
					return MarioRoleState.BRAKE;
				else {
					// is mario still braking?
					if(curState == MarioRoleState.BRAKE && stateTimer <= MARIO_BRAKE_TIME)
						return MarioRoleState.BRAKE;
					else {
						if(isFacingRight && b2body.getLinearVelocity().x > MARIO_MIN_WALKSPEED)
							return MarioRoleState.RUN;
						else if(!isFacingRight && b2body.getLinearVelocity().x < -MARIO_MIN_WALKSPEED)
							return MarioRoleState.RUN;
						else
							return MarioRoleState.STAND;
					}
				}
			}
		}
		else {
			if(curState == MarioRoleState.JUMP)
				return MarioRoleState.JUMP;
			else	// if mario is not on the ground and he did not jump, then he fell
				return MarioRoleState.FALL;
		}

		return state;
	}

	@Override
	public void update(float delta) {
		MarioRoleState nextState;

		processHeadHits();

		// If mario grows and shrinks on the same frame, it means he got a mushroom and he hit an enemy at the
		// same time. Since a big mario would have damage invulnerability after the hit, so will we (but no
		// grow/shrink animation).
		if(timeToDefineBigMario && timeToDefineSmallMario) {
			startDmgInvincibility();
		}
		if(timeToDefineBigMario) {
			defineBody(b2body.getPosition().add(0f, GameInfo.P2M(8f)), b2body.getLinearVelocity(), true);
			timeToDefineBigMario = false;
		}
		else if(timeToDefineSmallMario) {
			defineBody(b2body.getPosition().sub(0f, GameInfo.P2M(8f)), b2body.getLinearVelocity(), false);
			timeToDefineSmallMario = false;

			// assuming small mario because touched an enemy - so start limit invulnerability time
			startDmgInvincibility();
		}

		// the player can jump once per press of the jump key, so let them jump again when they release the button
		if(isOnGround && !wantsToJump)
			isJumpAllowed = true;

		nextState = getState();
		switch(nextState) {
			case DEAD:
				// make sure mario doesn't move left or right while dead
				b2body.setLinearVelocity(0f, b2body.getLinearVelocity().y);
				break;
			case BRAKE:
				if(isFacingRight && b2body.getLinearVelocity().x < 0f)
					brakeLeftRight(true);
				else if(!isFacingRight && b2body.getLinearVelocity().x > 0f)
					brakeLeftRight(false);
				break;
			case RUN:
				if(wantsToGoRight && !wantsToGoLeft)
					moveBodyLeftRight(true, wantsToRun, true);
				else if(wantsToGoLeft && !wantsToGoRight)
					moveBodyLeftRight(false, wantsToRun, true);
				else {
					if(isFacingRight && b2body.getLinearVelocity().x > MARIO_MIN_WALKSPEED)
						decelLeftRight(true);
					else if(!isFacingRight && b2body.getLinearVelocity().x < -MARIO_MIN_WALKSPEED)
						decelLeftRight(false);
				}
				break;
			case STAND:
				if(wantsToGoRight && !wantsToGoLeft)
					moveBodyLeftRight(true, wantsToRun, true);
				else if(wantsToGoLeft && !wantsToGoRight)
					moveBodyLeftRight(false, wantsToRun, true);
				break;
			case JUMP:
				// if first state of jump then move body like it's on ground
				if(nextState != curState) {
					// do left/right move (if needed) before jump
					if(wantsToGoRight && !wantsToGoLeft)
						moveBodyLeftRight(true, wantsToRun, true);
					else if(wantsToGoLeft && !wantsToGoRight)
						moveBodyLeftRight(false, wantsToRun, true);

					if(isJumpAllowed) {
						// the faster mario is moving, the higher he jumps, up to a max
						float mult = Math.abs(b2body.getLinearVelocity().x) / MARIO_MAX_RUNJUMPVEL;
						// cap the multiplier
						if(mult > 1)
							mult = 1;

						mult *= MARIO_RUNJUMP_MULT;
						mult += 1;

						moveBodyY(MARIO_JUMP_IMPULSE * mult);
						isJumpAllowed = false;
						isJumpForceAllowed = true;
					}
				}
				else {	// jumped and mid-air
					if(wantsToGoRight && !wantsToGoLeft)
						moveBodyLeftRight(true, wantsToRun, false);
					else if(wantsToGoLeft && !wantsToGoRight)
						moveBodyLeftRight(false, wantsToRun, false);

					// The longer the player holds the jump key, the higher they go,
					// if mario is moving up (no jump force allowed while mario is moving down)
					// TODO: what if mario is moving down because he jumped from an elevator?
					if(wantsToJump && isJumpForceAllowed && b2body.getLinearVelocity().y > 0f &&
							stateTimer < MARIO_JUMPFORCE_TIME) {
						useTheForceMario(MARIO_JUMP_FORCE * (MARIO_JUMPFORCE_TIME - stateTimer) / MARIO_JUMPFORCE_TIME);
					}

					// jump force stops, and cannot be restarted, if the player releases the jump key
					if(!wantsToJump)
						isJumpForceAllowed = false;
				}
				break;
			case FALL:
				break;
		}

		stateTimer = nextState == curState ? stateTimer + delta : 0f;
		curState = nextState;

		wantsToGoRight = false;
		wantsToGoLeft = false;
		wantsToRun = false;
		wantsToJump = false;

		if(dmgInvincibleTime > 0f)
			dmgInvincibleTime -= delta;
		else if(isDmgInvincible)
			endDmgInvincibility();

		marioSprite.update(delta, b2body.getPosition(), curState, isBig, isFacingRight, isDmgInvincible);
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

	private void defineBody(Vector2 position, Vector2 velocity, boolean isBig) {
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
		if(isBig)
			marioShape.setAsBox(GameInfo.P2M(7f),  GameInfo.P2M(13f));
		else
			marioShape.setAsBox(GameInfo.P2M(7f),  GameInfo.P2M(6f));

		fdef.filter.categoryBits = GameInfo.MARIO_BIT;
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT;

		fdef.shape = marioShape;
		fdef.friction = 0.1f;	// slide a bit more (default is 0.2) 
		marioBodyFixture = b2body.createFixture(fdef);
		marioBodyFixture.setUserData(this);

		// head sensor for detecting head banging behavior
		headSensor = new PolygonShape();
		if(isBig)
			headSensor.setAsBox(GameInfo.P2M(2f), GameInfo.P2M(1f), new Vector2(GameInfo.P2M(0f), GameInfo.P2M(16f)), 0f);
		else
			headSensor.setAsBox(GameInfo.P2M(2f), GameInfo.P2M(1f), new Vector2(GameInfo.P2M(0f), GameInfo.P2M(8f)), 0f);

		fdef.filter.categoryBits = GameInfo.MARIOHEAD_BIT;
		fdef.filter.maskBits = GameInfo.BANGABLE_BIT;
		fdef.shape = headSensor;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(this);
		// foot sensor for detecting onGround
		footSensor = new PolygonShape();
		if(isBig)
			footSensor.setAsBox(GameInfo.P2M(5f), GameInfo.P2M(2f), new Vector2(0f, GameInfo.P2M(-16)), 0f);
		else
			footSensor.setAsBox(GameInfo.P2M(5f), GameInfo.P2M(2f), new Vector2(0f, GameInfo.P2M(-6)), 0f);

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
System.out.println("touched robot");
		// If the bottom of mario sprite is at least as high as the middle point of the robot sprite, then the robot
		// takes damage. Otherwise mario takes damage.
		float marioY = b2body.getPosition().y;
		float robotY = robo.getBody().getPosition().y;
		float marioHeight = marioSprite.getHeight();

		if(robo instanceof Goomba) {
System.out.println("touched goomba");
			// test for step on head of goomba
			if(marioY - (marioHeight/2f) >= robotY)
				((Goomba) robo).applySquish();
			// shrink if big
			else if(isBig)
				shrink();
			// die if small and not invincible
			else if(dmgInvincibleTime <= 0f)
				die();
		}
	}

	@Override
	public void onTouchItem(RobotRole robo) {
System.out.println("touched item");
		if(robo instanceof PowerMushroom) {
System.out.println("touched powermush");
			((PowerMushroom) robo).use(this);
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

	public void die() {
		if(!isDead()) {
			MyKidRidicarus.manager.get("audio/music/mario_music.ogg", Music.class).stop();
			MyKidRidicarus.manager.get("audio/sounds/mariodie.wav", Sound.class).play();
			marioIsDead = true;

			Filter filter = new Filter();
			filter.maskBits = GameInfo.NOTHING_BIT;
			for (Fixture fixture : b2body.getFixtureList())
				fixture.setFilterData(filter);

			b2body.setLinearVelocity(0f, 0f);
			b2body.applyLinearImpulse(new Vector2(0, 4f), b2body.getWorldCenter(), true);
		}
	}

	@Override
	public float getStateTimer() {
		return stateTimer;
	}

	public boolean isBig() {
		return isBig;
	}
}
