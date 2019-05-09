package kidridicarus.game.Metroid.agent.player.samus;

import java.util.LinkedList;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agent;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.agent.playeragent.PlayerAgentSupervisor;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.powerup.PowChar;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.Direction8;
import kidridicarus.common.tool.MoveAdvice4x2;
import kidridicarus.game.Metroid.agent.player.samuschunk.SamusChunk;
import kidridicarus.game.Metroid.agent.player.samusshot.SamusShot;
import kidridicarus.game.SMB1.agent.TileBumpTakeAgent.TileBumpStrength;
import kidridicarus.game.SMB1.agent.other.pipewarp.PipeWarp;
import kidridicarus.game.info.MetroidAudio;
import kidridicarus.game.info.MetroidPow;
import kidridicarus.game.info.SMB1_Pow;

public class SamusBrain {
	enum MoveState { STAND, BALL_GRND, RUN, RUNSHOOT, PRE_JUMPSHOOT, JUMPSHOOT, JUMPSPINSHOOT,
		PRE_JUMPSPIN, JUMPSPIN, PRE_JUMP, JUMP, BALL_AIR, CLIMB, DEAD;
		public boolean equalsAny(MoveState ...otherStates) {
			for(MoveState state : otherStates) { if(this.equals(state)) return true; } return false;
		}
		public boolean isGround() { return this.equalsAny(STAND, BALL_GRND, RUN, RUNSHOOT); }
		public boolean isRun() { return this.equalsAny(RUN, RUNSHOOT); }
		public boolean isJumpSpin() { return this.equalsAny(PRE_JUMPSPIN, JUMPSPIN, JUMPSPINSHOOT); }
		public boolean isBall() { return this.equalsAny(BALL_GRND, BALL_AIR); }
	}

	private static final float STEP_SOUND_TIME = 0.167f;
	private static final float POSTPONE_RUN_DELAY = 0.15f;
	private static final float PRE_JUMP_TIME = 0.1f;
	private static final float JUMPUP_VEL_TIME = 0.05f;
	private static final float JUMPUP_FORCE_TIME = 0.75f;
	private static final float JUMPSHOOT_RESPIN_DELAY = 0.05f;
	private static final float SHOOT_COOLDOWN = 0.15f;
	private static final float SHOT_VEL = 2f;
	private static final Vector2 SHOT_OFFSET_RIGHT = UInfo.VectorP2M(11, 7);
	private static final Vector2 SHOT_OFFSET_UP = UInfo.VectorP2M(1, 20);
	private static final float NO_DAMAGE_TIME = 0.8f;
	private static final float DEAD_DELAY_TIME = 3f;
	private static final int START_ENERGY_SUPPLY = 30;
	private static final int MAX_ENERGY_SUPPLY = 99;
	private static final int ENERGY_POW_AMOUNT = 5;

	private Samus parent;
	private SamusBody body;
	private PlayerAgentSupervisor supervisor;
	private MoveState moveState;
	private float moveStateTimer;
	private boolean isFacingRight;
	private boolean isFacingUp;
	private float noDamageCooldown;
	private int energySupply;
	private float runStateTimer;
	private float lastStepSoundTime;
	private boolean isNextJumpAllowed;
	private float jumpForceTimer;
	private float shootCooldownTimer;
	private int takeDamageAmount;
	private Vector2 takeDmgOrigin;
	private boolean gaveHeadBounce;
	private boolean isHeadBumped;
	// list of powerups received during contact update
	private LinkedList<Powerup> powerupsReceived;
	private RoomBox lastKnownRoom;

	public SamusBrain(Samus parent, SamusBody body, boolean isFacingRight, Integer energySupply) {
		this.parent = parent;
		this.body = body;
		supervisor = new PlayerAgentSupervisor(parent);
		moveState = MoveState.STAND;
		moveStateTimer = 0f;
		this.isFacingRight = isFacingRight;
		isFacingUp = false;
		noDamageCooldown = 0f;
		this.energySupply = energySupply != null ? energySupply : START_ENERGY_SUPPLY;
		runStateTimer = 0f;
		lastStepSoundTime = 0f;
		// player must land on ground before first jump is allowed
		isNextJumpAllowed = false;
		jumpForceTimer = 0f;
		shootCooldownTimer = 0f;
		takeDamageAmount = 0;
		takeDmgOrigin = new Vector2();
		gaveHeadBounce = false;
		isHeadBumped = false;
		powerupsReceived = new LinkedList<Powerup>();
		lastKnownRoom = null;
	}

	/*
	 * Check for and do head bumps during contact update, so bump tiles can show results of bump immediately
	 * by way of regular update.
	 */
	public void processContactFrame(BrainContactFrameInput cFrameInput) {
		// update last known room if not dead, so dead player moving through other RoomBoxes won't cause problems
		if(moveState != MoveState.DEAD) {
			if(cFrameInput.room != null)
				lastKnownRoom = cFrameInput.room;
		}
		if(supervisor.isRunningScriptNoMoveAdvice())
			return;
		if(!isHeadBumped) {
			// if ducking then hit soft
			if(moveState.isJumpSpin())
				isHeadBumped = body.getSpine().checkDoHeadBump(TileBumpStrength.HARD);
			// otherwise hit hard
			else
				isHeadBumped = body.getSpine().checkDoHeadBump(TileBumpStrength.SOFT);
		}
	}

	public SpriteFrameInput processFrame(FrameTime frameTime) {
		// if a script is running with no move advice then apply scripted body state and exit
		if(supervisor.isRunningScriptNoMoveAdvice()) {
			ScriptedAgentState scriptedState = supervisor.getScriptAgentState();
			body.useScriptedBodyState(scriptedState.scriptedBodyState);
			isFacingRight = scriptedState.scriptedSpriteState.isFacingRight;
			// return null if scripted sprite is not visible
			if(!supervisor.getScriptAgentState().scriptedSpriteState.visible)
				return null;
			return getScriptedSpriteFrameInput(supervisor.getScriptAgentState().scriptedSpriteState, frameTime);
		}

		// since body has zero bounciness, a manual check is needed while in ball form 
		if(moveState.isBall())
			body.getSpine().doBounceCheck();

		MoveAdvice4x2 moveAdvice = supervisor.pollMoveAdvice();
		processPowerupsReceived();
		processDamageTaken(frameTime);
		processHeadBouncesGiven();
		processPipeWarps(moveAdvice);

		MoveState nextMoveState = getNextMoveState(moveAdvice);
		boolean isMoveStateChange = nextMoveState != moveState;
		if(nextMoveState == MoveState.DEAD)
			processDeadMove(isMoveStateChange);
		else {
			if(nextMoveState.isGround())
				processGroundMove(frameTime, moveAdvice, nextMoveState);
			else
				processAirMove(frameTime, moveAdvice, nextMoveState);
			processShoot(frameTime, moveAdvice);
			// do space wrap last so that contacts are maintained
			body.getSpine().checkDoSpaceWrap(lastKnownRoom);
		}
		moveStateTimer = isMoveStateChange ? 0f : moveStateTimer+frameTime.timeDelta;
		moveState = nextMoveState;
		return new SamusSpriteFrameInput(frameTime, body.getPosition(), moveState, isFacingRight, isFacingUp,
				(noDamageCooldown > 0f), Direction4.NONE); 
	}

	private void processPowerupsReceived() {
		for(Powerup pu : powerupsReceived) {
			if(pu instanceof MetroidPow.EnergyPow) {
				energySupply += ENERGY_POW_AMOUNT;  
				if(energySupply > MAX_ENERGY_SUPPLY)
					energySupply = MAX_ENERGY_SUPPLY;
			}
			// TODO: implement ignore points pow for samus somewhere better
			else if(pu.getPowerupCharacter() != PowChar.SAMUS && !(pu instanceof SMB1_Pow.PointsPow))
				supervisor.receiveNonCharPowerup(pu);
		}
		powerupsReceived.clear();
	}

	private void processDamageTaken(FrameTime frameTime) {
		// check for contact with scroll kill box (insta-kill)
		if(body.getSpine().isContactScrollKillBox()) {
			energySupply = 0;
			return;
		}
		// if invulnerable to damage then exit
		else if(noDamageCooldown > 0f) {
			noDamageCooldown -= frameTime.timeDelta;
			takeDamageAmount = 0;
			takeDmgOrigin.set(0f, 0f);
			return;
		}
		// if no damage taken this frame then exit
		else if(takeDamageAmount == 0f)
			return;

		// apply damage against health, health cannot be less than zero
		energySupply = energySupply-takeDamageAmount > 0 ? energySupply-takeDamageAmount : 0;

		// reset frame take damage amount
		takeDamageAmount = 0;
		// start no damage period
		noDamageCooldown = NO_DAMAGE_TIME;
		// push player away from damage origin
		body.getSpine().applyDamageKick(takeDmgOrigin);
		// reset take damage origin
		takeDmgOrigin.set(0f, 0f);
		// ouchie sound
		parent.getAgency().getEar().playSound(MetroidAudio.Sound.HURT);
	}

	private void processHeadBouncesGiven() {
		// if a head bounce was given in the update frame then reset the flag and do bounce move
		if(gaveHeadBounce) {
			gaveHeadBounce = false;
			body.getSpine().applyHeadBounce();
		}
	}

	private void processPipeWarps(MoveAdvice4x2 moveAdvice) {
		PipeWarp pw = body.getSpine().getEnterPipeWarp(moveAdvice.getMoveDir4());
		if(pw != null)
			pw.use(parent);
	}

	private MoveState getNextMoveState(MoveAdvice4x2 moveAdvice) {
		if(moveState == MoveState.DEAD || body.getSpine().isContactDespawn() || energySupply <= 0)
			return MoveState.DEAD;
		// if [on ground flag is true] and agent isn't [moving upward while in air move state], then do ground move
		else if(body.getSpine().isOnGround() && !(body.getSpine().isMovingInDir(Direction4.UP) &&
				!moveState.isGround())) {
			return getNextMoveStateGround(moveAdvice);
		}
		// do air move
		else
			return getNextMoveStateAir(moveAdvice);
	}

	private MoveState getNextMoveStateGround(MoveAdvice4x2 moveAdvice) {
		if(moveState.isBall()) {
			// if advised move up and not move down, and map tile above player is not solid, then change to stand
			if(moveAdvice.moveUp && !moveAdvice.moveDown &&
					!body.getSpine().isMapTileSolid(UInfo.VectorM2T(body.getPosition()).add(0, 1)))
				return MoveState.STAND;
			// continue current ball state
			else
				return moveState;
		}
		// if already standing and advised to move down but not advised to move horizontally then start ball state
		else if(moveState == MoveState.STAND && moveAdvice.moveDown &&
				!moveAdvice.moveRight && !moveAdvice.moveLeft)
			return MoveState.BALL_GRND;
		// if advised to jump and allowed to jump then...
		else if(moveAdvice.action1 && isNextJumpAllowed) {
			// if advised move right or left but not both at same time then do jump spin
			if(moveAdvice.moveRight^moveAdvice.moveLeft && !moveAdvice.action0)
				return MoveState.PRE_JUMPSPIN;
			// if advised shoot then do jumpshoot
			else if(moveAdvice.action0)
				return MoveState.PRE_JUMPSHOOT;
			else
				return MoveState.PRE_JUMP;
		}
		else if(body.getSpine().isNoHorizontalVelocity()) {
			// If advised move right and contacting right wall, or
			// if advised move left and contacting left wall, or
			// if no left/right move advice,
			// then stand.
			if((moveAdvice.moveRight && body.getSpine().isSideMoveBlocked(true)) ||
					(moveAdvice.moveLeft && body.getSpine().isSideMoveBlocked(false)) ||
					!(moveAdvice.moveRight^moveAdvice.moveLeft))
				return MoveState.STAND;
			else {
				if(moveAdvice.action0)
					return MoveState.RUNSHOOT;
				else
					return MoveState.RUN;
			}
		}
		else if(moveAdvice.action0 || (moveState == MoveState.RUNSHOOT && moveStateTimer < POSTPONE_RUN_DELAY))
			return MoveState.RUNSHOOT;
		else
			return MoveState.RUN;
	}

	private MoveState getNextMoveStateAir(MoveAdvice4x2 moveAdvice) {
		if(moveState.isBall()) {
			// If advised to move up and not move down and tile above samus head is not solid then change to
			// standing type state.
			if(moveAdvice.moveUp && !moveAdvice.moveDown &&
					!body.getSpine().isMapTileSolid(UInfo.VectorM2T(body.getPosition()).add(0, 1)))
				return MoveState.JUMP;
			// continue air ball state if already in air ball state, or change from ground to air ball state
			else
				return MoveState.BALL_AIR;
		}
		else if(moveState == MoveState.JUMPSPINSHOOT) {
			// if not moving up then lose spin and do jumpshoot
			if(!body.getSpine().isMovingInDir(Direction4.UP))
				return MoveState.JUMPSHOOT;
			// If not advised to shoot, and advised move left or right but not both, and respin is allowed,
			// then do jumpspin.
			else if(!moveAdvice.action0 && moveAdvice.moveRight^moveAdvice.moveLeft &&
					moveStateTimer > JUMPSHOOT_RESPIN_DELAY)
				return MoveState.JUMPSPIN;
			// continue jumpshoot
			else
				return MoveState.JUMPSPINSHOOT;
		}
		else if(moveState == MoveState.JUMPSPIN) {
			// if advised to shoot then switch to jumpspin with shoot
			if(moveAdvice.action0)
				return MoveState.JUMPSPINSHOOT;
			// continue jumpspin
			else
				return MoveState.JUMPSPIN;
		}
		else if(moveState == MoveState.PRE_JUMPSPIN) {
			// is change to jumpspin allowed?
			if(moveStateTimer > PRE_JUMP_TIME) {
				// if shooting then enter jumpspin with shoot
				if(moveAdvice.action0)
					return MoveState.JUMPSPINSHOOT;
				// start jumpspin
				else
					return MoveState.JUMPSPIN;
			}
			// continue pre-jumpspin
			else
				return MoveState.PRE_JUMPSPIN;
		}
		else if(moveState == MoveState.PRE_JUMP) {
			if(moveStateTimer > PRE_JUMP_TIME)
				return MoveState.JUMP;
			else
				return MoveState.PRE_JUMP;
		}
		else if(moveAdvice.action0 || moveState == MoveState.JUMPSHOOT)
			return MoveState.JUMPSHOOT;
		else
			return MoveState.JUMP;
	}

	private void processDeadMove(boolean moveStateChanged) {
		// if newly dead then disable contacts and start dead sound
		if(moveStateChanged) {
			body.applyDead();
			doDeathBurst();
			parent.getAgency().getEar().stopAllMusic();
			parent.getAgency().getEar().startSinglePlayMusic(MetroidAudio.Music.SAMUS_DIE);
		}
		// ... and if died a long time ago then do game over
		else if(moveStateTimer > DEAD_DELAY_TIME)
				supervisor.setGameOver();
		// ... else do nothing.
	}

	private static final float CHUNK_OFFSET_X = UInfo.P2M(4f); 
	private static final float CHUNK_OFFSET_Y = UInfo.P2M(8f); 
	private static final float BOT_VEL_X = 0.6f; 
	private static final float BOT_VEL_Y = 1f; 
	private static final float MID_VEL_X = 0.6f; 
	private static final float MID_VEL_Y = 1f; 
	private static final float TOP_VEL_X = 0.6f; 
	private static final float TOP_VEL_Y = 0.9f; 
	// 6 pieces burst in 6 different directions
	// 2 columns and 3 rows of 8x8 sprite animation
	private void doDeathBurst() {
		parent.getAgency().createAgent(SamusChunk.makeAP(body.getPosition().cpy().add(CHUNK_OFFSET_X, -CHUNK_OFFSET_Y),
				new Vector2(BOT_VEL_X, BOT_VEL_Y), Direction8.DOWN_RIGHT));
		parent.getAgency().createAgent(SamusChunk.makeAP(body.getPosition().cpy().add(-CHUNK_OFFSET_X, -CHUNK_OFFSET_Y),
				new Vector2(-BOT_VEL_X, BOT_VEL_Y), Direction8.DOWN_LEFT));
		parent.getAgency().createAgent(SamusChunk.makeAP(body.getPosition().cpy().add(CHUNK_OFFSET_X, 0f),
				new Vector2(MID_VEL_X, MID_VEL_Y), Direction8.RIGHT));
		parent.getAgency().createAgent(SamusChunk.makeAP(body.getPosition().cpy().add(-CHUNK_OFFSET_X, 0f),
				new Vector2(-MID_VEL_X, MID_VEL_Y), Direction8.LEFT));
		parent.getAgency().createAgent(SamusChunk.makeAP(body.getPosition().cpy().add(CHUNK_OFFSET_X, CHUNK_OFFSET_Y),
				new Vector2(TOP_VEL_X, TOP_VEL_Y), Direction8.UP_RIGHT));
		parent.getAgency().createAgent(SamusChunk.makeAP(body.getPosition().cpy().add(-CHUNK_OFFSET_X, CHUNK_OFFSET_Y),
				new Vector2(-TOP_VEL_X, TOP_VEL_Y), Direction8.UP_LEFT));
	}

	private void processGroundMove(FrameTime frameTime, MoveAdvice4x2 moveAdvice, MoveState nextMoveState) {
		// next jump changes to allowed when on ground and not advising jump move 
		if(!moveAdvice.action1)
			isNextJumpAllowed = true;

		boolean applyWalkMove = false;
		boolean applyStopMove = false;
		// if advised to move right or move left, but not both at same time, then...
		if(moveAdvice.moveRight^moveAdvice.moveLeft) {
			if(isFacingRight && moveAdvice.moveLeft)
				isFacingRight = false;
			else if(!isFacingRight && moveAdvice.moveRight)
				isFacingRight = true;

			applyWalkMove = true;
		}
		else
			applyStopMove = true;

		// if advised to move up or move down, but not both at same time, then...
		if(moveAdvice.moveUp^moveAdvice.moveDown)
			isFacingUp = moveAdvice.moveUp;
		else
			isFacingUp = false;

		// if previous move air move then samus just landed, so play landing sound
		if(!moveState.isGround())
			parent.getAgency().getEar().playSound(MetroidAudio.Sound.STEP);
		// if last move and this move are run moves then check/do step sound
		else if(moveState.isRun() && nextMoveState.isRun()) {
			if(runStateTimer - lastStepSoundTime >= STEP_SOUND_TIME) {
				lastStepSoundTime = runStateTimer;
				parent.getAgency().getEar().playSound(MetroidAudio.Sound.STEP);
			}
		}
		else
			lastStepSoundTime = 0f;

		// if changed to ball state from non-ball state then decrease body size
		if(nextMoveState.isBall() && !moveState.isBall())
			body.setBallForm(true);
		// if changed to non-ball state from ball state then increase body size 
		if(!nextMoveState.isBall() && moveState.isBall())
			body.setBallForm(false);

		switch(nextMoveState) {
			case STAND:
			case RUN:
			case RUNSHOOT:
			case BALL_GRND:
				break;
			default:
				throw new IllegalStateException("Wrong ground nextMoveState = " + nextMoveState);
		}

		if(applyWalkMove)
			body.getSpine().applyWalkMove(isFacingRight);
		if(applyStopMove)
			body.getSpine().applyStopMove();

		// reset head bump flag while on ground and not moving up
		if(body.getVelocity().y < UInfo.VEL_EPSILON)
			isHeadBumped = false;

		runStateTimer = nextMoveState.isRun() ? runStateTimer+frameTime.timeDelta : 0f;
	}

	private void processAirMove(FrameTime frameTime, MoveAdvice4x2 moveAdvice, MoveState nextMoveState) {
		// check for change of facing direction
		if(moveAdvice.moveRight^moveAdvice.moveLeft) {
			if(isFacingRight && moveAdvice.moveLeft)
				isFacingRight = false;
			else if(!isFacingRight && moveAdvice.moveRight)
				isFacingRight = true;
		}

		// facing up can change during jumpspin while player moves upward
		if(nextMoveState.isJumpSpin() && body.getSpine().isMovingInDir(Direction4.UP))
			isFacingUp = moveAdvice.moveUp;
		// facing up can change to true during jump, but cannot change back to false
		else if(moveAdvice.moveUp)
			isFacingUp = true;

		// if changed to non-ball state from ball state then increase body size 
		if(!nextMoveState.isBall() && moveState.isBall())
			body.setBallForm(false);
		// note: cannot change from non-ball state to ball state while mid-air

		switch(nextMoveState) {
			case PRE_JUMPSPIN:
			case PRE_JUMP:
			case PRE_JUMPSHOOT:
				// if previously on ground and advised jump and allowed to jump then jump 
				if(moveState.isGround() && moveAdvice.action1) {
					isNextJumpAllowed = false;
					jumpForceTimer = PRE_JUMP_TIME+JUMPUP_FORCE_TIME;
					body.getSpine().applyJumpVelocity();
					parent.getAgency().getEar().playSound(MetroidAudio.Sound.JUMP);
				}
				else if(moveStateTimer <= JUMPUP_VEL_TIME)
					body.getSpine().applyJumpVelocity();
				else if(jumpForceTimer > 0f && moveAdvice.action1)
					body.getSpine().applyJumpForce(jumpForceTimer-PRE_JUMP_TIME, JUMPUP_FORCE_TIME);
				break;
			case JUMP:
			case JUMPSPIN:
			case JUMPSHOOT:
			case JUMPSPINSHOOT:
				// This takes care of player falling off ledge - player cannot jump again until at least
				// one frame has elapsed where player was on ground and not advised to  jump (to prevent
				// re-jump bouncing).
				isNextJumpAllowed = false;

				// if jump force continues and jump is advised then do jump force
				if(jumpForceTimer > 0f && moveAdvice.action1)
					body.getSpine().applyJumpForce(jumpForceTimer-PRE_JUMP_TIME, JUMPUP_FORCE_TIME);

				break;
			case BALL_AIR:
				break;
			default:
				throw new IllegalStateException("Wrong air nextMoveState = " + nextMoveState);
		}

		// disallow jump force until next jump if [jump advice stops] or [body stops moving up]
		if(!moveAdvice.action1 || !body.getSpine().isMovingInDir(Direction4.UP))
			jumpForceTimer = 0f;

		// if advised move right or move left but not both at same time then...
		if(moveAdvice.moveRight^moveAdvice.moveLeft) {
			// if advised move right and facing left then change to facing right
			if(moveAdvice.moveRight && !isFacingRight)
				isFacingRight = true;
			// if advised move left and facing right then change to facing left
			else if(moveAdvice.moveLeft && isFacingRight)
				isFacingRight = false;

			body.getSpine().applyAirMove(isFacingRight);
		}
		// if in jumpspin then maintain velocity, otherwise apply stop move 
		else if(moveState != MoveState.JUMPSPIN)
			body.getSpine().applyStopMove();

		if(isHeadBumped) {
			body.getSpine().applyHeadBumpMove();
			isHeadBumped = false;
		}

		// decrement jump force timer
		jumpForceTimer = jumpForceTimer > frameTime.timeDelta ? jumpForceTimer-frameTime.timeDelta : 0f;
	}

	private void processShoot(FrameTime frameTime, MoveAdvice4x2 moveAdvice) {
		if(moveAdvice.action0 && shootCooldownTimer <= 0f && !moveState.isBall())
			doShoot();
		// decrememnt shoot cooldown timer
		shootCooldownTimer = shootCooldownTimer > frameTime.timeDelta ? shootCooldownTimer-frameTime.timeDelta : 0f;
	}

	private void doShoot() {
		shootCooldownTimer = SHOOT_COOLDOWN;

		// calculate position and velocity of shot based on samus' orientation
		Vector2 velocity = new Vector2();
		Vector2 position = new Vector2();
		if(isFacingUp) {
			velocity.set(0f, SHOT_VEL);
			if(isFacingRight)
				position.set(SHOT_OFFSET_UP).add(body.getPosition());
			else
				position.set(SHOT_OFFSET_UP).scl(-1, 1).add(body.getPosition());
		}
		else if(isFacingRight) {
			velocity.set(SHOT_VEL, 0f);
			position.set(SHOT_OFFSET_RIGHT).add(body.getPosition());
		}
		else {
			velocity.set(-SHOT_VEL, 0f);
			position.set(SHOT_OFFSET_RIGHT).scl(-1, 1).add(body.getPosition());
		}

		// create shot; if the spawn point of shot is in a solid tile then the shot must immediately explode
		parent.getAgency().createAgent(SamusShot.makeAP(parent, position, velocity, body.getSpine().isMapPointSolid(position)));
		parent.getAgency().getEar().playSound(MetroidAudio.Sound.SHOOT);
	}

	private SpriteFrameInput getScriptedSpriteFrameInput(ScriptedSpriteState sss, FrameTime frameTime) {
		MoveState scriptedMoveState;
		switch(sss.spriteState) {
			case MOVE:
				scriptedMoveState = MoveState.RUN;
				break;
			case CLIMB:
				scriptedMoveState = MoveState.CLIMB;
				break;
			case STAND:
			default:
				scriptedMoveState = MoveState.STAND;
				break;
		}
		return new SamusSpriteFrameInput(
				frameTime, sss.position, scriptedMoveState, sss.isFacingRight, false, false,  sss.moveDir);
	}

	public boolean isFacingRight() {
		return isFacingRight;
	}

	public Integer getEnergySupply() {
		return energySupply;
	}

	public boolean onTakePowerup(Powerup pu) {
		if(moveState == MoveState.DEAD)
			return false;
		powerupsReceived.add(pu);
		return true;
	}

	public boolean onTakeDamage(float amount, Vector2 dmgOrigin) {
		// don't take more damage if dead, or if damage already taken in this frame, or if invulnerable
		if(moveState == MoveState.DEAD || takeDamageAmount > 0f || noDamageCooldown > 0f)
			return false;
		// keep damage info
		takeDamageAmount = (int) amount;
		takeDmgOrigin.set(dmgOrigin);
		return true;
	}

	public boolean onGiveHeadBounce(Agent agent) {
		// if other agent has bounds and head bounce is allowed then give head bounce to agent
		Rectangle otherBounds = AP_Tool.getBounds(agent);
		if(otherBounds != null && body.getSpine().isGiveHeadBounceAllowed(otherBounds)) {
			gaveHeadBounce = true;
			return true;
		}
		return false;
	}

	public PlayerAgentSupervisor getSupervisor() {
		return supervisor;
	}

	public RoomBox getCurrentRoom() {
		return lastKnownRoom;
	}
}
