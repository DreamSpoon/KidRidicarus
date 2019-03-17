package kidridicarus.game.agent.SMB.player.mario;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentscript.ScriptedSpriteState;
import kidridicarus.agency.agentscript.ScriptedSpriteState.SpriteState;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.GameAgentObserver;
import kidridicarus.common.agent.despawnbox.DespawnBox;
import kidridicarus.common.agent.AgentSupervisor;
import kidridicarus.common.agent.GameTeam;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.metaagent.tiledmap.collision.CollisionTiledMapAgent;
import kidridicarus.common.tool.MoveAdvice;
import kidridicarus.game.agent.SMB.HeadBounceTakeAgent;
import kidridicarus.game.agent.SMB.TileBumpTakeAgent;
import kidridicarus.game.agent.SMB.other.bumptile.BumpTile.TileBumpStrength;
import kidridicarus.game.agent.SMB.other.flagpole.Flagpole;
import kidridicarus.game.agent.SMB.other.floatingpoints.FloatingPoints;
import kidridicarus.game.agent.SMB.other.levelendtrigger.LevelEndTrigger;
import kidridicarus.game.agent.SMB.other.pipewarp.PipeWarp;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.info.PowerupInfo.PowType;
import kidridicarus.game.info.SMBInfo.PointAmount;

/*
 * TODO:
 * -the body physics code has only been tested with non-moving surfaces, needs to be tested with moving platforms
 * -mario will sometimes not go down a pipe warp even though he is in the right place on top of the pipe - fix this
 */
public class Mario extends Agent implements PlayerAgent, DisposableAgent {
	public enum MarioAgentState { PLAY, FIREBALL, DEAD }	// TODO merge this with move state

	private static final float MARIO_DEAD_TIME = 3f;
	private static final float LEVEL_MAX_TIME = 300f;

	private static final float DMG_INVINCIBLE_TIME = 3f;
	private static final float FIREBALL_OFFSET = UInfo.P2M(8f);
	private static final float TIME_PER_FIREBALL = 0.5f;
	private static final float POWERSTAR_TIME = 15f;
	private static final float BLINK_DURATION = 0.05f;

	private static final float MARIO_JUMP_GROUNDCHECK_DELAY = 0.05f;
	private static final float MARIO_JUMPFORCE_TIME = 0.5f;
	private static final float MARIO_JUMP_IMPULSE = 1.75f;
	private static final float MARIO_JUMP_FORCE = 14f;
	private static final float MARIO_RUNJUMP_MULT = 0.25f;
	private static final float MARIO_MAX_RUNJUMPVEL = MarioBody.MARIO_MAX_RUNVEL;
	private static final float MARIO_HEADBOUNCE_VEL = 1.75f;	// up velocity
	private static final float MIN_HEADBANG_VEL = 0.01f;	// TODO: test this with different values to the best

	public enum MarioBodyState { STAND, WALKRUN, BRAKE, JUMP, FALL, DUCK, DEAD, CLIMB }

	public enum MarioPowerState { SMALL, BIG, FIRE }

	private MarioSupervisor supervisor;
	private MarioObserver observer;
	private MarioBody mBody;
	private MarioSprite marioSprite;
	private MarioPowerState curPowerState;
	private MarioAgentState curAgentState;
	private float stateTimer;

	private boolean marioIsDead;
	private boolean prevFrameAdvisedShoot;
	private boolean isDmgInvincible;
	private float dmgInvincibleTime;
	private float fireballTimer;
	private float powerStarTimer;
	private float levelTimeRemaining;
	private int extraLives;
	private int coinTotal;
	private int pointTotal;
	private PointAmount consecBouncePoints;
	// powerup received
	private PowType powerupRec;

	private boolean isBrakeAvailable;
	private float brakeTimer;
	private boolean isNewJumpAllowed;
	private float jumpGroundCheckTimer;
	private boolean isJumping;
	private float jumpForceTimer;
	private boolean isLastVelocityRight;
	private boolean isDuckSlideRight;
	private boolean canHeadBang;
	private MarioBodyState curBodyState;
	private boolean isFacingRight;
	private boolean isBig;
	private boolean isDucking;
	private boolean isDuckSliding;
	private boolean isTakeDamage;
	private PipeWarp pipeToEnter;

	public Mario(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		marioIsDead = false;
		prevFrameAdvisedShoot = false;
		isDmgInvincible = false;
		dmgInvincibleTime = 0f;
		fireballTimer = TIME_PER_FIREBALL * 2f;
		powerStarTimer = 0f;

		curPowerState = MarioPowerState.SMALL;

		powerupRec = PowType.NONE;

		levelTimeRemaining = LEVEL_MAX_TIME;

		curAgentState = MarioAgentState.PLAY;
		stateTimer = 0f;

		extraLives = 2;
		coinTotal = pointTotal = 0;
		consecBouncePoints = PointAmount.ZERO;

		isBrakeAvailable = true;
		brakeTimer = 0f;
		isNewJumpAllowed = false;
		jumpGroundCheckTimer = 0f;
		isJumping = false;
		jumpForceTimer = 0f;
		isDucking = false;
		isLastVelocityRight = false;
		isDuckSliding = false;
		isDuckSlideRight = false;
		isTakeDamage = false;
		canHeadBang = true;
		pipeToEnter = null;

		curBodyState = MarioBodyState.STAND;

		mBody = new MarioBody(this, agency, Agent.getStartPoint(properties), false, false);
		marioSprite = new MarioSprite(agency.getAtlas(), mBody.getPosition(), curPowerState);

		supervisor = new MarioSupervisor(this);
		observer = new MarioObserver(this, agency.getAtlas());

		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_TOP, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	private void doUpdate(float delta) {
		processContacts();
		processMove(delta, supervisor.pollMoveAdvice());
		processSprite(delta);
	}

	private void processContacts() {
	}

	private void processMove(float delta, MoveAdvice moveAdvice) {
		MarioBodyState bodyState;
		MarioAgentState nextAgentState;
		boolean isStarPowered;

		levelTimeRemaining -= delta;

		if(supervisor.isRunningScript()) {
			// if a script is running with move advice, then switch advice to the scripted move advice
			if(supervisor.isRunningScriptMoveAdvice())
				moveAdvice = supervisor.getScriptAgentState().scriptedMoveAdvice;
			else {
				// use the scripted agent state
				mBody.useScriptedBodyState(supervisor.getScriptAgentState().scriptedBodyState, isBig, false);
				MarioBodyState scriptedBodyState;
				ScriptedSpriteState sss = supervisor.getScriptAgentState().scriptedSpriteState;
				switch(sss.spriteState) {
					case CLIMB:
						scriptedBodyState = MarioBodyState.CLIMB;
						break;
					case MOVE:
						scriptedBodyState = MarioBodyState.WALKRUN;
						break;
					case STAND:
					default:
						scriptedBodyState = MarioBodyState.STAND;
						break;
				}
				marioSprite.update(delta, sss.position, MarioAgentState.PLAY, scriptedBodyState, curPowerState,
						sss.facingRight, false, isBigBody(), sss.moveDir);

				// TODO only run this next line when script ends,
				//   to prevent mario getting pipe warp contact where there is none
				pipeToEnter = null;

				return;
			}
		}

		// reset consecutive bounce points
		if(mBody.isOnGround())
			consecBouncePoints = PointAmount.ZERO;

		bodyState = MarioBodyState.STAND;
		nextAgentState = processMarioAgentState(delta, moveAdvice);
		// mario special states (e.g. end level script) override regular body states (e.g. mario use powerup) 
		if(nextAgentState == MarioAgentState.PLAY || nextAgentState == MarioAgentState.FIREBALL) {
			processDamage(delta);
			processPowerups();
			bodyState = mBodyUpdate(delta, moveAdvice, curPowerState);
		}
		else if(curAgentState == MarioAgentState.DEAD && stateTimer > MARIO_DEAD_TIME) {
			supervisor.setGameOver();
		}

		stateTimer = nextAgentState == curAgentState ? stateTimer+delta : 0f;
		curAgentState = nextAgentState;

		isStarPowered = false;
		if(powerStarTimer > 0f) {
			isStarPowered = true;
			powerStarTimer -= delta;
		}

		marioSprite.update(delta, mBody.getPosition(), curAgentState, bodyState, curPowerState,
				isFacingRight, isStarPowered, isBigBody(), null);

		prevFrameAdvisedShoot = moveAdvice.action0;
	}

	private MarioBodyState mBodyUpdate(float delta, MoveAdvice advice, MarioPowerState curPowerState) {
		MarioBodyState nextState;
		boolean isVelocityLeft;
		boolean isVelocityRight;
		boolean doDuckSlideMove;
		boolean doWalkRunMove;
		boolean doDecelMove;
		boolean doBrakeMove;

		pipeToEnter = mBody.getEnterPipeWarp(advice.getMoveDir4());
		processOtherContacts();

		nextState = MarioBodyState.STAND;
		isVelocityRight = mBody.getVelocity().x > MarioBody.MARIO_MIN_WALKSPEED;
		isVelocityLeft = mBody.getVelocity().x < -MarioBody.MARIO_MIN_WALKSPEED;

		// If mario's velocity is below min walking speed while on ground and he is not duck sliding then
		// zero his velocity
		if(mBody.isOnGround() && !isDuckSliding && !isVelocityRight && !isVelocityLeft &&
				!advice.moveRight && !advice.moveLeft)
			mBody.setVelocity(0f, mBody.getVelocity().y);

		// multiple concurrent body impulses may be necessary
		doDuckSlideMove = false;
		doWalkRunMove = false;
		doDecelMove = false;
		doBrakeMove = false;

		// make a note of the last direction in which mario was moving, for duck sliding
		if(isVelocityRight)
			isLastVelocityRight = true;
		else if(isVelocityLeft)
			isLastVelocityRight = false;

		// eligible for duck/unduck?
		if(curPowerState != MarioPowerState.SMALL && mBody.isOnGround()) {
			Vector2 bodyTilePos = UInfo.getM2PTileForPos(mBody.getPosition());

			// first time duck check
			if(advice.moveDown && !isDucking) {
				// quack
				isDucking = true;
				if(isDuckSliding)
					isDuckSliding = false;
				else {
					// mario's body's height is reduced when ducking, so recreate the body in a slightly lower pos
					mBody.defineBody(mBody.getPosition().cpy().sub(0f, UInfo.P2M(8f)), mBody.getVelocity(),
							isBig, isDucking);
				}
			}
			// first time unduck check
			else if(!advice.moveDown && isDucking) {
				isDucking = false;

				// Check the space above and around mario to test if mario can unduck normally, or if he is in a
				// tight spot

				// if the tile above ducking mario is solid ...
				if(isMapTileSolid(bodyTilePos.cpy().add(0, 1))) {
					Vector2 subTilePos = UInfo.getSubTileCoordsForMPos(mBody.getPosition());
					// If the player's last velocity direction was rightward, and their position is in the left half
					// of the tile, and the tile above and to the left of them is solid, then the player should
					// duckslide right.
					if((isLastVelocityRight && subTilePos.x <= 0.5f && isMapTileSolid(bodyTilePos.cpy().add(-1, 1))) ||
							(subTilePos.x > 0.5f && !isMapTileSolid(bodyTilePos.cpy().add(1, 1))) ||
							(isLastVelocityRight && subTilePos.x > 0.5f && isMapTileSolid(bodyTilePos.cpy().add(1, 1)))) {
						isDuckSlideRight = true;
					}
					// the only other option is to duckslide left
					else
						isDuckSlideRight = false;

					// tile above is solid so must be ducksliding
					isDuckSliding = true;
				}
				else {
					mBody.defineBody(mBody.getPosition().cpy().add(0f, UInfo.P2M(8f)), mBody.getVelocity(),
							isBig, isDucking);
				}
			}

			if(isDuckSliding) {
				// if the player was duck sliding but the space above them is now nonsolid then end duckslide
				if(!isMapTileSolid(bodyTilePos.cpy().add(0, 1))) {
					isDuckSliding = false;
					mBody.defineBody(mBody.getPosition().cpy().add(0f, UInfo.P2M(8f)), mBody.getVelocity(),
							isBig, isDucking);
				}
				else
					doDuckSlideMove = true;
			}
		}

		// want to move left or right? (but not both! because they would cancel each other)
		if((advice.moveRight && !advice.moveLeft) || (!advice.moveRight && advice.moveLeft)) {
			doWalkRunMove = true;

			// mario can change facing direction, but not while airborne
			if(mBody.isOnGround()) {
				// brake becomes available again when facing direction changes
				if(isFacingRight != advice.moveRight) {
					isBrakeAvailable = true;
					brakeTimer = 0f;
				}

				// can't run/walk on ground while ducking, only slide
				if(isDucking) {
					doWalkRunMove = false;
					doDecelMove = true;
				}
				else	// update facing direction
					isFacingRight = advice.moveRight;
			}
		}
		// decelerate if on ground and not wanting to move left or right
		else if(mBody.isOnGround() && (isVelocityRight || isVelocityLeft))
			doDecelMove = true;

		// check for brake application
		if(!isDucking && mBody.isOnGround() && isBrakeAvailable &&
				((isFacingRight && isVelocityLeft) || (!isFacingRight && isVelocityRight))) {
			isBrakeAvailable = false;
			brakeTimer = MarioBody.MARIO_BRAKE_TIME;
		}
		// this catches brake applications from this update() call and previous update() calls
		if(brakeTimer > 0f) {
			doBrakeMove = true;
			brakeTimer -= delta;
		}

		// apply impulses if necessary
		if(doDuckSlideMove)
			mBody.duckSlideLeftRight(isDuckSlideRight);
		else if(doBrakeMove) {
			mBody.brakeLeftRight(isFacingRight);
			nextState = MarioBodyState.BRAKE;
		}
		else if(doWalkRunMove) {
			mBody.moveBodyLeftRight(advice.moveRight, advice.action0, isDucking);
			nextState = MarioBodyState.WALKRUN;
		}
		else if(doDecelMove) {
			mBody.decelLeftRight();
			nextState = MarioBodyState.WALKRUN;
		}

		// Do not check mario's "on ground" state for a short time after mario jumps, because his foot sensor
		// might still be contacting the ground even after his body enters the air.
		if(jumpGroundCheckTimer > delta)
			jumpGroundCheckTimer -= delta;
		else {
			jumpGroundCheckTimer = 0f;
			// The player can jump once per press of the jump key, so let them jump again when they release the
			// button but, they need to be on the ground with the button released.
			if(mBody.isOnGround()) {
				isJumping = false;
				if(!advice.action1)
					isNewJumpAllowed = true;
			}
		}

		// jump?
		if(advice.action1 && isNewJumpAllowed) {	// do jump
			isNewJumpAllowed = false;
			isJumping = true;
			// start a timer to delay checking for onGround state
			jumpGroundCheckTimer = MARIO_JUMP_GROUNDCHECK_DELAY;
			nextState = MarioBodyState.JUMP;

			// the faster mario is moving, the higher he jumps, up to a max
			float mult = Math.abs(mBody.getVelocity().x) / MARIO_MAX_RUNJUMPVEL;
			// cap the multiplier
			if(mult > 1f)
				mult = 1f;

			mult *= MARIO_RUNJUMP_MULT;
			mult += 1f;

			// apply initial (and only) jump impulse
			mBody.applyBodyImpulse(new Vector2(0, MARIO_JUMP_IMPULSE * mult));
			// the remainder of the jump up velocity is achieved through mid-air up-force
			jumpForceTimer = MARIO_JUMPFORCE_TIME;
			if(curPowerState != MarioPowerState.SMALL)
				agency.playSound(AudioInfo.Sound.SMB.MARIO_BIGJUMP);
			else
				agency.playSound(AudioInfo.Sound.SMB.MARIO_SMLJUMP);
		}
		else if(isJumping) {	// jumped and is mid-air
			nextState = MarioBodyState.JUMP;
			// jump force stops, and cannot be restarted, if the player releases the jump key
			if(!advice.action1)
				jumpForceTimer = 0f;
			// The longer the player holds the jump key, the higher they go,
			// if mario is moving up (no jump force allowed while mario is moving down)
			// TODO: what if mario is initally moving down because he jumped from an elevator?
			else if(mBody.getVelocity().y > 0f && jumpForceTimer > 0f) {
				jumpForceTimer -= delta;
				// the force was strong to begin and tapered off over time - some said it became irrelevant
				mBody.applyForce(new Vector2(0, MARIO_JUMP_FORCE * jumpForceTimer / MARIO_JUMPFORCE_TIME));
			}
		}
		// finally, if mario is not on the ground (for reals) then he is falling since he is not jumping
		else if(!mBody.isOnGround() && jumpGroundCheckTimer <= 0f) {
			// cannot jump while falling
			isNewJumpAllowed = false;
			nextState = MarioBodyState.FALL;
		}

		if(isDucking)
			nextState = MarioBodyState.DUCK;

		stateTimer = nextState == curBodyState ? stateTimer+delta : 0f;
		curBodyState = nextState;
		mBody.updatePrevs();

		return nextState;
	}

	private void processOtherContacts() {
		// despawn contact?
		if(mBody.getFirstContactByClass(DespawnBox.class) != null) {
			die();
			return;
		}

		processHeadContacts();	// hitting bricks with head

		// item contact?
//		PowerupGiveAgent item = (PowerupGiveAgent) mBody.getFirstContactByClass(PowerupGiveAgent.class);
//		if(item != null)
//			item.use(this);

		// if power star is in use...
		if(powerStarTimer > 0f) {
			// apply powerstar damage
			List<ContactDmgTakeAgent> list = mBody.getContactsByClass(ContactDmgTakeAgent.class);
			for(ContactDmgTakeAgent agent : list) {
				// playSound should go in the processBody method, but... this is so much easier!
				agency.playSound(AudioInfo.Sound.SMB.KICK);
				agent.onTakeDamage(this, GameTeam.PLAYER, 1f, mBody.getPosition());
			}

			// Remove any agents that accumulate in the begin queue, to prevent begin contacts during
			// power star time being ignored - which would cause mario to take damage when power star time ends. 
			mBody.getAndResetBeginContacts();
		}
		else {
			// check for headbounces
			List<Agent> list = mBody.getAndResetBeginContacts();
			LinkedList<Agent> bouncedAgents = new LinkedList<Agent>();
			for(Agent agent : list) {
				// skip the agent if not bouncy :)
				if(!(agent instanceof HeadBounceTakeAgent) || !((HeadBounceTakeAgent) agent).isBouncy())
					continue;
				// If the bottom of mario's bounds box is at least as high as the middle of the agent then bounce.
				// (i.e. if mario's foot is at least as high as midway up the other agent...)
				// Note: check this frame postiion and previous frame postiion in case mario is travelling quickly...
				if(mBody.getPosition().y - mBody.getBounds().height/2f >= agent.getPosition().y ||
						mBody.getPrevPosition().y - mBody.getBounds().height/2f >= agent.getPosition().y) {
					bouncedAgents.add(agent);
					((HeadBounceTakeAgent) agent).onHeadBounce(this);
				}
			}
			if(!bouncedAgents.isEmpty()) {
				mBody.setVelocity(mBody.getVelocity().x, 0f);
				mBody.applyBodyImpulse(new Vector2(0f, MARIO_HEADBOUNCE_VEL));
			}

/*			// if not invincible then check for incoming damage
			if(dmgInvincibleTime <= 0f) {
				// check for contact damage
				for(Agent a : list) {
					if(!(a instanceof ContactDmgGiveAgent))
						continue;
					// if the agent does contact damage and they were not head bounced
					if(((ContactDmgGiveAgent) a).isContactDamage() && !bouncedAgents.contains(a))
						isTakeDamage = true;
				}
			}*/
		}
	}

	/*
	 * Process the head contact add and remove queues, then check the list of current contacts for a head bang.
	 *
	 * NOTE: After banging his head while moving up, mario cannot bang his head again until he has moved down a
	 * sufficient amount. Also, mario can only break one block per head bang - but if his head contacts multiple
	 * blocks when he hits, then choose the block closest to mario on the x axis.
	 */
	private void processHeadContacts() {
		// if can head bang and is moving upwards fast enough then ...
		if(canHeadBang && (mBody.getVelocity().y > MIN_HEADBANG_VEL || mBody.getPrevVelocity().y > MIN_HEADBANG_VEL)) {
			// check the list of tiles for the closest to mario
			float closest = 0;
			TileBumpTakeAgent closestTile = null;
			for(TileBumpTakeAgent thingHit : mBody.getBumptileContacts()) {
				float dist = Math.abs(((Agent) thingHit).getPosition().x - mBody.getPosition().x);
				if(closestTile == null || dist < closest) {
					closest = dist;
					closestTile = thingHit;
				}
			}

			// we have a weiner!
			if(closestTile != null) {
				canHeadBang = false;
				TileBumpStrength tbs = TileBumpStrength.SOFT;
				if(curPowerState != MarioPowerState.SMALL)
					tbs = TileBumpStrength.HARD;
				((TileBumpTakeAgent) closestTile).onTakeTileBump(this, tbs);
			}
		}
		// mario can headbang once per up/down cycle of movement, so re-enable head bang when mario moves down
		else if(mBody.getVelocity().y < 0f)
			canHeadBang = true;
	}

	private void processSprite(float delta) {
	}

	// Process the body and return a character state based on the findings.
	private MarioAgentState processMarioAgentState(float delta, MoveAdvice advice) {
		if(marioIsDead) {
			if(curAgentState != MarioAgentState.DEAD) {
				observer.stopAllMusic();
				agency.playSound(AudioInfo.Sound.SMB.MARIO_DIE);

				mBody.setContactEnabled(false);
				mBody.setVelocity(0f, 0f);
				mBody.applyBodyImpulse(new Vector2(0, 4f));
			}
			// make sure mario doesn't move left or right while dead
			mBody.zeroVelocity(true, false);
			return MarioAgentState.DEAD;
		}
		else if(isUseLevelEndTrigger())
			return curAgentState;
		// flagpole contact and use?
		else if(isUseFlagpole())
			return curAgentState;
		// scripted pipe entrance
		else if(pipeToEnter != null) {
			pipeToEnter.use(this);
			return curAgentState;
		}
		// otherwise the player has control, because no script is runnning
		else {
			if(processFireball(delta, advice.action0))
				return MarioAgentState.FIREBALL;
			else
				return MarioAgentState.PLAY;
		}
	}

	private boolean isUseLevelEndTrigger() {
		// check for end level trigger contact, and use it if found
		LevelEndTrigger leTrigger = mBody.getFirstContactByClass(LevelEndTrigger.class);
		if(leTrigger != null) {
			leTrigger.use(this);
			return true;
		}
		return false;
	}

	private boolean isUseFlagpole() {
		// check for end level flagpole contact, and use it if found
		Flagpole flagpole = mBody.getFirstContactByClass(Flagpole.class);
		if(flagpole != null) {
			flagpole.use(this);
			return true;
		}
		return false;
	}

	// mario can shoot fireballs two at a time, but must wait if his "fireball timer" runs low
	private boolean processFireball(float delta, boolean shoot) {
		if(curAgentState != MarioAgentState.PLAY)
			return false;

		fireballTimer += delta;
		if(fireballTimer > TIME_PER_FIREBALL)
			fireballTimer = TIME_PER_FIREBALL;

		// fire a ball?
		if(curPowerState == MarioPowerState.FIRE && shoot && !prevFrameAdvisedShoot && fireballTimer > 0f) {
			fireballTimer -= TIME_PER_FIREBALL;
			throwFireball();
			return true;
		}

		return false;
	}

	private void throwFireball() {
		Vector2 offset;
		if(isFacingRight)
			offset = mBody.getPosition().cpy().add(FIREBALL_OFFSET, 0f);
		else
			offset = mBody.getPosition().cpy().add(-FIREBALL_OFFSET, 0f);

		agency.createAgent(MarioFireball.makeAP(offset, isFacingRight, this));
		agency.playSound(AudioInfo.Sound.SMB.FIREBALL);
	}

	private void processPowerups() {
		// apply powerup if received
		switch(powerupRec) {
			case MUSH1UP:
				agency.createAgent(FloatingPoints.makeAP(PointAmount.P1UP, false,
						mBody.getPosition(), UInfo.P2M(16), this));
				break;
			case MUSHROOM:
				if(curPowerState == MarioPowerState.SMALL) {
					curPowerState = MarioPowerState.BIG;

					isBig = true;
					mBody.defineBody(mBody.getPosition().add(0f, UInfo.P2M(8f)), mBody.getVelocity(),
							isBig, isDucking);

					agency.playSound(AudioInfo.Sound.SMB.POWERUP_USE);
				}
				agency.createAgent(FloatingPoints.makeAP(PointAmount.P1000, false,
						mBody.getPosition(), UInfo.P2M(16), this));
				break;
			case FIREFLOWER:
				if(curPowerState == MarioPowerState.SMALL) {
					curPowerState = MarioPowerState.BIG;

					isBig = true;
					mBody.defineBody(mBody.getPosition().add(0f, UInfo.P2M(8f)), mBody.getVelocity(),
							isBig, isDucking);

					agency.playSound(AudioInfo.Sound.SMB.POWERUP_USE);
				}
				else if(curPowerState == MarioPowerState.BIG) {
					curPowerState = MarioPowerState.FIRE;
					agency.playSound(AudioInfo.Sound.SMB.POWERUP_USE);
				}
				agency.createAgent(FloatingPoints.makeAP(PointAmount.P1000, false,
						mBody.getPosition(), UInfo.P2M(16), this));
				break;
			case POWERSTAR:
				powerStarTimer = POWERSTAR_TIME;
				agency.playSound(AudioInfo.Sound.SMB.POWERUP_USE);
				observer.startSinglePlayMusic(AudioInfo.Music.SMB.STARPOWER);
				agency.createAgent(FloatingPoints.makeAP(PointAmount.P1000, false,
						mBody.getPosition(), UInfo.P2M(16), this));
				break;
			case NONE:
				break;
			// did mario receive a powerup that's not for his character?
			default:
				// tell supervisor!
				supervisor.applyNonMarioPowerup(powerupRec);
				break;
		}

		powerupRec = PowType.NONE;
	}

	private void processDamage(float delta) {
		// if invincible then remove incoming damage from the damage queue
		if(dmgInvincibleTime > 0f)
			dmgInvincibleTime -= delta;
		else if(isDmgInvincible)
			endDmgInvincibility();

		// check for damage, and reset the flag
		boolean dmg = getAndResetTakeDamage();
		// exit because no damage during power star time
		if(powerStarTimer > 0f)
			return;

		// exit if no damage received
		if(!dmg)
			return;

		// Apply damage if received:
		//   Fire mario becomes small mario,
		//   Big mario becomes small mario,
		//   Small mario becomes dead mario.
		if(curPowerState == MarioPowerState.FIRE || curPowerState == MarioPowerState.BIG) {
			curPowerState = MarioPowerState.SMALL;
			if(isDucking) {
				isBig = false;
				mBody.defineBody(mBody.getPosition(), mBody.getVelocity(), false, isDucking);
			}
			else {
				isBig = false;
				mBody.defineBody(mBody.getPosition().sub(0f, UInfo.P2M(8f)), mBody.getVelocity(), false, isDucking);
			}
			startDmgInvincibility();
			agency.playSound(AudioInfo.Sound.SMB.POWERDOWN);
		}
		else
			marioIsDead = true;
	}

	private void startDmgInvincibility() {
		isDmgInvincible = true;
		dmgInvincibleTime = DMG_INVINCIBLE_TIME;
	}

	private void endDmgInvincibility() {
		isDmgInvincible = false;
		dmgInvincibleTime = 0f;
		getAndResetTakeDamage();
	}

	private void doDraw(AgencyDrawBatch batch) {
		if(supervisor.isRunningScript() && !supervisor.isRunningScriptMoveAdvice() &&
				!supervisor.getScriptAgentState().scriptedSpriteState.visible)
			return;

		// if mario has damage invincible time then the sprite will blink visible/invisible
		if(!(isDmgInvincible && Math.floorMod((int) (this.dmgInvincibleTime / BLINK_DURATION), 2) == 0))
			batch.draw(marioSprite);
	}

	/*
	 * Check for contact with tiled collision map. If contact then get solid state of tile given by tileCoords.
	 */
	private boolean isMapTileSolid(Vector2 tileCoords) {
		CollisionTiledMapAgent octMap = mBody.getFirstContactByClass(CollisionTiledMapAgent.class);
		if(octMap == null)
			return false;
		return octMap.isMapTileSolid(tileCoords);
	}

	private boolean isBigBody() {
		if(!isBig || isDucking || isDuckSliding)
			return false;
		else
			return true;
	}

	private boolean getAndResetTakeDamage() {
		boolean t = isTakeDamage;
		isTakeDamage = false;
		return t;
	}

	private void die() {
		marioIsDead = true;
	}

	public void giveCoin() {
		agency.playSound(AudioInfo.Sound.SMB.COIN);
		givePoints(PointAmount.P200, false);
		coinTotal++;
	}

	public PointAmount givePoints(PointAmount amt, boolean relative) {
		PointAmount actualAmt = amt;
		// relative points do not stack when mario is onground
		if(relative && !mBody.isOnGround()) {
			if(consecBouncePoints.increment().getIntAmt() >= amt.getIntAmt()) {
				consecBouncePoints = consecBouncePoints.increment();
				actualAmt = consecBouncePoints;
			}
			else
				consecBouncePoints = amt;
		}

		if(actualAmt == PointAmount.P1UP)
			give1UP();
		else
			pointTotal += actualAmt.getIntAmt();

		return actualAmt;
	}

	private void give1UP() {
		extraLives++;
	}

	@Override
	public RoomBox getCurrentRoom() {
		return mBody.getCurrentRoom();
	}

	public float getLevelTimeRemaining() {
		return levelTimeRemaining;
	}

	public int getCoinTotal() {
		return coinTotal;
	}

	public int getPointTotal() {
		return pointTotal;
	}

	public int getExtraLives() {
		return extraLives;
	}

	public boolean isBig() {
		return (curPowerState != MarioPowerState.SMALL);
	}

	@Override
	public Vector2 getPosition() {
		return mBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return mBody.getBounds();
	}

	@Override
	public AgentSupervisor getSupervisor() {
		return supervisor;
	}

	@Override
	public GameAgentObserver getObserver() {
		return observer;
	}

//	@Override
//	public void applyPowerup(PowType pt) {
//		powerupRec = pt;
//	}

	// unchecked cast to T warnings ignored because T is checked with class.equals(cls) 
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(String key, Object defaultValue, Class<T> cls) {
		if(key.equals(CommonKV.Script.KEY_FACINGRIGHT) && Boolean.class.equals(cls)) {
			Boolean he = isFacingRight;
			return (T) he;
		}
		else if(key.equals(CommonKV.Script.KEY_SPRITESTATE) && SpriteState.class.equals(cls)) {
			SpriteState he = SpriteState.STAND;
			return (T) he;
		}
		else if(key.equals(CommonKV.Script.KEY_SPRITESIZE) && Vector2.class.equals(cls)) {
			Vector2 he = new Vector2(marioSprite.getWidth(), marioSprite.getHeight());
			return (T) he;
		}
		return properties.get(key, defaultValue, cls);
	}

	@Override
	public void disposeAgent() {
		mBody.dispose();
	}
}
