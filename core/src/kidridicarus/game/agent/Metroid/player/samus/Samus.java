package kidridicarus.game.agent.Metroid.player.samus;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.AgentSupervisor;
import kidridicarus.common.agent.GameAgentObserver;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.MoveAdvice;
import kidridicarus.game.agent.Metroid.player.samusshot.SamusShot;
import kidridicarus.game.info.AudioInfo;

public class Samus extends Agent implements PlayerAgent, ContactDmgTakeAgent, DisposableAgent {
	public enum MoveState { STAND, BALL_GRND, RUN, RUNSHOOT, PRE_JUMPSHOOT, JUMPSHOOT, JUMPSPINSHOOT,
		PRE_JUMPSPIN, JUMPSPIN, PRE_JUMP, JUMP, BALL_AIR, CLIMB;
		public boolean equalsAny(MoveState ...otherStates) {
			for(MoveState state : otherStates) { if(this.equals(state)) return true; } return false;
		}
		public boolean isGroundMove() { return this.equalsAny(STAND, BALL_GRND, RUN, RUNSHOOT); }
		public boolean isRun() { return this.equalsAny(RUN, RUNSHOOT); }
		public boolean isJumpSpin() { return this.equalsAny(PRE_JUMPSPIN, JUMPSPIN, JUMPSPINSHOOT); }
		public boolean isBall() { return this.equalsAny(BALL_GRND, BALL_AIR); }
	}

	private static final float STEP_SOUND_TIME = 0.167f;
	private static final float POSTPONE_RUN_DELAY = 0.15f;
	private static final float JUMPUP_CONSTVEL_TIME = 0.2f;
	private static final float JUMPUP_FORCE_TIME = 0.75f;
	private static final float JUMPSHOOT_RESPIN_DELAY = 0.05f;
	private static final Vector2 SHOT_OFFSET_RIGHT = UInfo.P2MVector(11, 7);
	private static final Vector2 SHOT_OFFSET_UP = UInfo.P2MVector(1, 20);
	private static final float SHOT_VEL = 2f;
	private static final float SHOOT_COOLDOWN = 0.15f;
	private static final float NO_DAMAGE_TIME = 0.8f;

	private SamusSupervisor supervisor;
	private SamusObserver observer;
	private SamusBody body;
	private SamusSprite sprite;
	private MoveState moveState;
	private float moveStateTimer;

	private boolean isFacingRight;
	private boolean isFacingUp;
	private float runStateTimer;
	private float lastStepSoundTime;
	private float jumpForceTimer;
	private boolean isNextJumpAllowed;
	private float shootCooldownTimer;
	private boolean didTakeDamage;
	private Vector2 takeDmgOrigin;
	private float noDamageCooldown;

	public Samus(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		moveState = MoveState.STAND;
		moveStateTimer = 0f;
		isFacingRight = false;
		if(properties.get(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class) == Direction4.RIGHT)
			isFacingRight = true;
		isFacingUp = false;
		runStateTimer = 0f;
		lastStepSoundTime = 0f;
		jumpForceTimer = 0f;
		// player must land on ground before first jump is allowed
		isNextJumpAllowed = false;
		shootCooldownTimer = 0f;
		didTakeDamage = false;
		takeDmgOrigin = new Vector2();
		noDamageCooldown = 0f;

		body = new SamusBody(this, agency.getWorld(), Agent.getStartPoint(properties));
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doUpdate(delta); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.POST_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doPostUpdate(); }
		});
		sprite = new SamusSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_TOP, new AgentDrawListener() {
			@Override
			public void draw(AgencyDrawBatch batch) { doDraw(batch); }
		});

		supervisor = new SamusSupervisor(this);
		observer = new SamusObserver(this, agency.getAtlas());
	}

	private void doPostUpdate() {
		// let body update previous position/velocity
		body.postUpdate();
	}

	private void doUpdate(float delta) {
		processMove(delta, supervisor.pollMoveAdvice());
		processSprite(delta);
	}

	private void processMove(float delta, MoveAdvice moveAdvice) {
		processDamageTaken(delta);

		MoveState nextMoveState = getNextMoveState(moveAdvice);
		if(nextMoveState.isGroundMove())
			processGroundMove(delta, moveAdvice, nextMoveState);
		else
			processAirMove(delta, moveAdvice, nextMoveState);

		moveStateTimer = moveState == nextMoveState ? moveStateTimer+delta : 0f;
		moveState = nextMoveState;

		processShoot(delta, moveAdvice);
	}

	private void processDamageTaken(float delta) {
		// if invulnerable to damage then exit
		if(noDamageCooldown > 0f) {
			noDamageCooldown -= delta;
			didTakeDamage = false;
			takeDmgOrigin.set(0f, 0f);
			return;
		}
		// if no damage taken this frame then exit
		else if(!didTakeDamage)
			return;
		// take damage
		didTakeDamage = false;
		noDamageCooldown = NO_DAMAGE_TIME;
		body.getSpine().applyDamageKick(takeDmgOrigin);
		takeDmgOrigin.set(0f, 0f);
		agency.playSound(AudioInfo.Sound.Metroid.HURT);
	}

	private MoveState getNextMoveState(MoveAdvice moveAdvice) {
		// if [on ground flag is true] and agent isn't [moving upward while in air move state], then do ground move
		if(body.getSpine().isOnGround() && !(body.getSpine().isMovingUp() && !moveState.isGroundMove()))
			return getNextMoveStateGround(moveAdvice);
		// do air move
		else
			return getNextMoveStateAir(moveAdvice);
	}

	private MoveState getNextMoveStateGround(MoveAdvice moveAdvice) {
		if(moveState.isBall()) {
			// if advised move up and not move down, and map tile above player is not solid, then change to stand
			if(moveAdvice.moveUp && !moveAdvice.moveDown &&
					!body.getSpine().isMapTileSolid(UInfo.getM2PTileForPos(body.getPosition()).add(0, 1)))
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
			if((moveAdvice.moveRight && body.getSpine().isSolidOnThisSide(true)) ||
					(moveAdvice.moveLeft && body.getSpine().isSolidOnThisSide(false)) ||
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

	private MoveState getNextMoveStateAir(MoveAdvice moveAdvice) {
		if(moveState.isBall()) {
			// If advised to move up and not move down and tile above samus head is not solid then change to
			// standing type state.
			if(moveAdvice.moveUp && !moveAdvice.moveDown &&
					!body.getSpine().isMapTileSolid(UInfo.getM2PTileForPos(body.getPosition()).add(0, 1)))
				return MoveState.JUMP;
			// continue air ball state if already in air ball state, or change from ground to air ball state
			else
				return MoveState.BALL_AIR;
		}
		else if(moveState == MoveState.JUMPSHOOT)
			return MoveState.JUMPSHOOT;
		else if(moveState == MoveState.JUMPSPINSHOOT) {
			// if not moving up then lose spin and do jumpshoot
			if(!body.getSpine().isMovingUp())
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
			if(body.getSpine().isJumpSpinAllowed()) {
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
		else
			return MoveState.JUMP;
	}

	private void processGroundMove(float delta, MoveAdvice moveAdvice, MoveState nextMoveState) {
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
		if(!moveState.isGroundMove())
			agency.playSound(AudioInfo.Sound.Metroid.STEP);
		// if last move and this move are run moves then check/do step sound
		else if(moveState.isRun() && nextMoveState.isRun()) {
			if(runStateTimer - lastStepSoundTime >= STEP_SOUND_TIME) {
				lastStepSoundTime = runStateTimer;
				agency.playSound(AudioInfo.Sound.Metroid.STEP);
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

		// since body has zero bounciness, a manual check is needed while in ball form 
		if(nextMoveState.isBall())
			body.getSpine().doBounceCheck();

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

		runStateTimer = nextMoveState.isRun() ? runStateTimer+delta : 0f;
	}

	private void processAirMove(float delta, MoveAdvice moveAdvice, MoveState nextMoveState) {
		// check for change of facing direction
		if(moveAdvice.moveRight^moveAdvice.moveLeft) {
			if(isFacingRight && moveAdvice.moveLeft)
				isFacingRight = false;
			else if(!isFacingRight && moveAdvice.moveRight)
				isFacingRight = true;
		}

		// facing up can change during jumpspin while player moves upward
		if(nextMoveState.isJumpSpin() && body.getSpine().isMovingUp()) {
			if(moveAdvice.moveUp && !moveAdvice.moveDown)
				isFacingUp = true;
			else
				isFacingUp = false;
		}

		// if changed to non-ball state from ball state then increase body size 
		if(!nextMoveState.isBall() && moveState.isBall())
			body.setBallForm(false);
		// note: cannot change from non-ball state to ball state while mid-air

		// since body has zero bounciness, a manual check is needed while in ball form 
		if(nextMoveState.isBall())
			body.getSpine().doBounceCheck();

		switch(nextMoveState) {
			case PRE_JUMPSPIN:
			case PRE_JUMP:
			case PRE_JUMPSHOOT:
				// if previously on ground then...
				if(moveState.isGroundMove()) {
					// if advised jump and allowed to jump then do it 
					if(moveAdvice.action1 && isNextJumpAllowed) {
						isNextJumpAllowed = false;
						jumpForceTimer = JUMPUP_CONSTVEL_TIME+JUMPUP_FORCE_TIME;
						body.getSpine().applyJumpVelocity();
						agency.playSound(AudioInfo.Sound.Metroid.JUMP);
					}
				}
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
					body.getSpine().applyJumpForce(jumpForceTimer-JUMPUP_CONSTVEL_TIME, JUMPUP_FORCE_TIME);

				break;
			case BALL_AIR:
				break;
			default:
				throw new IllegalStateException("Wrong air nextMoveState = " + nextMoveState);
		}

		// disallow jump force until next jump when [jump advice stops] or [body stops moving up]
		if(!moveAdvice.action1 || !body.getSpine().isMovingUp())
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

		// decrement jump force timer
		jumpForceTimer = jumpForceTimer > delta ? jumpForceTimer-delta : 0f;
	}

	private void processShoot(float delta, MoveAdvice moveAdvice) {
		if(moveAdvice.action0 && isShootAllowed() && !moveState.isBall())
			doShoot();
		// decrememnt shoot cooldown timer
		shootCooldownTimer = shootCooldownTimer > delta ? shootCooldownTimer-delta : 0f;
	}

	private boolean isShootAllowed() {
		return shootCooldownTimer <= 0f;
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

		// create shot
		ObjectProperties shotProps = SamusShot.makeAP(this, position, velocity);
		// if the spawn point of shot is in a solid tile then the shot must immediately explode
		if(body.getSpine().isMapPointSolid(position))
			shotProps.put(CommonKV.Spawn.KEY_EXPIRE, true);
		agency.createAgent(shotProps);
		agency.playSound(AudioInfo.Sound.Metroid.SHOOT);
	}

	private void processSprite(float delta) {
		sprite.update(delta, body.getPosition(), moveState, isFacingRight, isFacingUp, (noDamageCooldown > 0f),
				Direction4.NONE);
	}

	private void doDraw(AgencyDrawBatch batch) {
		batch.draw(sprite);
	}

	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		// no damage taken if already took damage this frame, or if invulnerable, or if star powered, or if dead
		if(didTakeDamage || noDamageCooldown > 0f)
			return false;
		didTakeDamage = true;
		takeDmgOrigin.set(dmgOrigin);
		return true;
	}

	@Override
	public AgentSupervisor getSupervisor() {
		return supervisor;
	}

	@Override
	public GameAgentObserver getObserver() {
		return observer;
	}

	@Override
	public RoomBox getCurrentRoom() {
		return body.getSpine().getCurrentRoom();
	}

	@Override
	public Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}

/*
 * TODO:
 * -samus loses JUMPSPIN when her y position goes below her jump start position
 */
/*
public class Samus extends Agent implements PlayerAgent, ContactDmgTakeAgent, DisposableAgent {
	private static final float DAMAGE_INV_TIME = 0.8f;
	private static final Vector2 SHOT_OFFSET_RIGHT = UInfo.P2MVector(11, 7);
	private static final Vector2 SHOT_OFFSET_UP = UInfo.P2MVector(1, 20);
	private static final float SHOT_VEL = 2f;
	private static final float SHOOT_COOLDOWN = 0.15f;
	private static final float JUMPSHOOT_RESPIN_DELAY = 0.05f;
	private static final float STEP_SOUND_TIME = 0.167f;
	private static final float POSTPONE_RUN_DELAY = 0.15f;

	private enum ContactState { REGULAR, DAMAGE }
	public enum MoveState { STAND, RUN, JUMP, JUMPSPIN, JUMPSHOOT, SHOOT, BALL, CLIMB;
			public boolean equalsAny(MoveState ...otherStates) {
				for(MoveState state : otherStates) { if(this.equals(state)) return true; } return false;
			}
			public boolean isJump() { return this.equalsAny(JUMP, JUMPSPIN, JUMPSHOOT); }
		}

	private SamusSupervisor supervisor;
	private SamusObserver observer;
	private SamusBody samusBody;
	private SamusSprite samusSprite;

	private ContactState curContactState;
	private float contactStateTimer;
	private MoveState curMoveState;
	private float moveStateTimer;

	private boolean isFacingRight;
	private boolean isFacingUp;
	private boolean isJumpForceEnabled;
	// the last jump must land before the next jump can start
	private boolean isLastJumpLandable;
	private boolean isNextJumpEnabled;
	private boolean isDrawThisFrame;
	private float shootCooldownTime;
	private float startJumpY;
	private boolean isJumpSpinAvailable;
	private float lastStepSoundTime = 0f;
	private boolean isAutoContinueRightAirMove;
	private boolean isAutoContinueLeftAirMove;

	public Samus(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		curContactState = ContactState.REGULAR;
		contactStateTimer = 0f;
		curMoveState = MoveState.STAND;
		moveStateTimer = 0f;
		isFacingRight = true;
		isFacingUp = false;
		isJumpForceEnabled = true;
		isLastJumpLandable = true;
		isNextJumpEnabled = true;
		isDrawThisFrame = false;
		startJumpY = 0;
		isJumpSpinAvailable = false;
		isAutoContinueRightAirMove = false;
		isAutoContinueLeftAirMove = false;
		shootCooldownTime = agency.getGlobalTimer();

		samusBody = new SamusBody(this, agency.getWorld(), Agent.getStartPoint(properties));
		samusSprite = new SamusSprite(agency.getAtlas(), samusBody.getPosition());
		observer = new SamusObserver(this, agency.getAtlas());
		supervisor = new SamusSupervisor(this);
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
		processContacts(delta);
		processMove(delta, supervisor.pollMoveAdvice());
		processSprite(delta);
	}

	private void processContacts(float delta) {
		// skip contacts update when script is running without move advice provided
		if(supervisor.isRunningScript() && !supervisor.isRunningScriptMoveAdvice())
			return;

		ContactState nextContactState = ContactState.REGULAR;
		switch(curContactState) {
			case REGULAR:
				break;
			case DAMAGE:
				// check for return to regular contact state
				if(contactStateTimer > DAMAGE_INV_TIME)
					nextContactState = ContactState.REGULAR;
				else
					nextContactState = ContactState.DAMAGE;
				break;
		}

		contactStateTimer = nextContactState == curContactState ? contactStateTimer+delta : 0f;
		curContactState = nextContactState;
	}

	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		samusBody.getSpine().applyDamageKick(dmgOrigin);
		if(curMoveState != MoveState.JUMPSPIN)
			isJumpForceEnabled = false;
		agency.playSound(AudioInfo.Sound.Metroid.HURT);
		return true;
	}

	private void processMove(float delta, MoveAdvice advice) {
		// if a script is running with move advice, then switch advice to the scripted move advice
		if(supervisor.isRunningScriptMoveAdvice())
			advice = supervisor.getScriptAgentState().scriptedMoveAdvice;
		// if no script is running then do the normal move stuff
		if(!supervisor.isRunningScript() || supervisor.isRunningScriptMoveAdvice()) {
			// if no pipe move started then do the regular move
			if(!processPipeMove(advice))
				processRegularMove(delta, advice);
		}
		// The previous code might have started a script, or a script may have already been running.
		// If a script is now running, that doesn't use scripted move advice, then process the scripted state.
		if(supervisor.isRunningScript() && !supervisor.isRunningScriptMoveAdvice()) {
			samusBody.useScriptedBodyState(supervisor.getScriptAgentState().scriptedBodyState);
			isFacingRight = supervisor.getScriptAgentState().scriptedSpriteState.facingRight;
		}
	}
*/
	/*
	 * Should Samus enter a warp pipe? Do it if needed.
	 */
/*	private boolean processPipeMove(MoveAdvice advice) {
		Direction4 adviceDir = advice.getMoveDir4();
		// if no move advice direction then no pipe move so exit; also, exit if move state is similar to jump
		if(adviceDir == Direction4.NONE || curMoveState.isJump())
			return false;
		// if no pipe to enter then exit (exit the method, not exit the pipe)
		PipeWarp wp = samusBody.getSpine().getPipeWarpForAdvice(adviceDir);
		if(wp == null)
			return false;

		// use the pipe warp, returning the result
		return wp.use(this);
	}
*/
	/*
	 * Run, jump, shoot, etc.
	 */
/*	private void processRegularMove(float delta, MoveAdvice advice) {
		MoveState nextMoveState = null;
		boolean jumpStart = false;
		// XOR the moveleft and moveright, becuase can't move left and right at same time
		boolean isMoveHorizontal = advice.moveRight^advice.moveLeft;

		switch(curMoveState) {
			case BALL:
				// if advised move up then try to stand
				if(advice.moveUp) {
					// if the tile directly above samus is solid then disallow change to stand
					Vector2 bodyTilePos = UInfo.getM2PTileForPos(samusBody.getPosition());
					if(isMapTileSolid(bodyTilePos.cpy().add(0, 1))) {
						nextMoveState = MoveState.BALL;
						samusBody.doBounceCheck();
					}
					else {
						isFacingUp = true;
						nextMoveState = MoveState.STAND;
						samusBody.switchToStandForm();
					}
				}
				else {
					nextMoveState = MoveState.BALL;
					samusBody.doBounceCheck();
				}
				break;
			case STAND:
			case RUN:
			case SHOOT:
			default:
				if(!isNextJumpEnabled && !advice.action1)
					isNextJumpEnabled = true;

				// if body is falling, or is not rising, then enable the next jump
				if(samusBody.getVelocity().y <= UInfo.VEL_EPSILON)
					isLastJumpLandable = true;

				isFacingUp = advice.moveUp;

				// switch to ball form?
				if(advice.moveDown && samusBody.getSpine().isOnGround()) {
					samusBody.switchToBallForm();
					nextMoveState = MoveState.BALL;
				}
				// jump?
				else if(advice.action1 && samusBody.getSpine().isOnGround() &&
						isLastJumpLandable && isNextJumpEnabled) {
					isLastJumpLandable = false;
					isJumpForceEnabled = true;
					jumpStart = true;
					if(isMoveHorizontal)
						isJumpSpinAvailable = true;
					startJumpY = samusBody.getPosition().y;
					agency.playSound(AudioInfo.Sound.Metroid.JUMP);
					nextMoveState = MoveState.JUMP;
				}
				else {
					boolean didShoot = checkAndDoShoot(advice.action0);

					// stand/run on ground?
					if(samusBody.getSpine().isOnGround()) {
						// moving sideways?
						if(isMoveHorizontal) {
							if((advice.moveRight && samusBody.getSpine().isContactingWall(true)) ||
									(advice.moveLeft && samusBody.getSpine().isContactingWall(false)))
								nextMoveState = MoveState.STAND;
							// move is not blocked
							else {
								checkDoStepSound();
								// Start the shoot state if did shoot, or postpone the run state and continue the
								// shoot state if still in the postpone period 
								if(didShoot || (curMoveState == MoveState.SHOOT &&
										moveStateTimer < POSTPONE_RUN_DELAY)) {
									nextMoveState = MoveState.SHOOT;
								}
								else
									nextMoveState = MoveState.RUN;
							}
						}
						// on ground and not moving, therefore stand state
						else
							nextMoveState = MoveState.STAND;
					}
					// fall
					else {
						// jump is disable when fall starts
						isLastJumpLandable = false;
						isJumpForceEnabled = false;

						if(didShoot)
							nextMoveState = MoveState.JUMPSHOOT;
						else
							nextMoveState = MoveState.JUMP;
					}
				}
				break;
			case JUMP:
			case JUMPSPIN:
			case JUMPSHOOT:
				// if body is falling then enable the next jump and disable jump up force
				if(samusBody.getVelocity().y <= UInfo.VEL_EPSILON) {
					isLastJumpLandable = true;
					isJumpForceEnabled = false;
				}
				// else if body is not being advised to jump then disable jump up force
				else if(!advice.action1)
					isJumpForceEnabled = false;

				// If the body is now onground but jump is still advised then disallow jumping until the
				// jump advice stops (to prevent bunny hopping)
				if(samusBody.getSpine().isOnGround() && advice.action1)
					isNextJumpEnabled = false;

				// disallow change to facing up if player is falling and they started jump with spin allowed
				if(!isFacingUp && advice.moveUp && !(isJumpSpinAvailable && samusBody.getVelocity().y <= 0f))
					isFacingUp = true;

				// Change back to facing sideways if already facing up, and not advised to moveUp,
				// and jump spin is available, and body is moving upward
				if(isFacingUp && !advice.moveUp && isJumpSpinAvailable && samusBody.getVelocity().y > 0f)
					isFacingUp = false;

				// check for landing on ground
				if(samusBody.getSpine().isOnGround() && !isJumpForceEnabled) {
					isJumpSpinAvailable = false;
					isAutoContinueLeftAirMove = false;
					isAutoContinueRightAirMove = false;
					// play landing sound
					agency.playSound(AudioInfo.Sound.Metroid.STEP);
					// if on ground and moving then run
					if(isMoveHorizontal)
						nextMoveState = MoveState.RUN;
					else
						nextMoveState = MoveState.STAND;
				}
				// still mid-air
				else {
					// do shoot?
					if(checkAndDoShoot(advice.action0))
						nextMoveState = MoveState.JUMPSHOOT;
					// already shooting?
					else if(curMoveState == MoveState.JUMPSHOOT) {
						// If delay is finished, and not advised to shoot, and moving horizontally, and spin
						// is available, and body is moving upward, and samus is at least 2 tiles higher than
						// jump start position, then switch back to jumpspin.
						if(moveStateTimer > JUMPSHOOT_RESPIN_DELAY && !advice.action0 && isMoveHorizontal &&
								isJumpSpinAvailable && samusBody.getVelocity().y > 0f &&
								samusBody.getPosition().y > startJumpY + 2f*UInfo.P2M(UInfo.TILEPIX_Y)) {
							nextMoveState = MoveState.JUMPSPIN;
						}
						// otherwise continue jump shoot
						else
							nextMoveState = MoveState.JUMPSHOOT;
					}
					// not shooting, just jumping?
					else if(curMoveState == MoveState.JUMP) {
						// switch to jumpspin when samus moves at least 2 tiles higher than jump start position 
						if(samusBody.getVelocity().y > 0f && isJumpSpinAvailable && samusBody.getPosition().y >
								startJumpY + 2f*UInfo.P2M(UInfo.TILEPIX_Y)) {
							// If move left/right advice is given during change from jump to jumpspin then
							// auto continue air move in advised direction until user presses other direction
							if(advice.moveRight)
								isAutoContinueRightAirMove = true;
							else if(advice.moveLeft)
								isAutoContinueLeftAirMove = true;

							nextMoveState = MoveState.JUMPSPIN;
						}
						else
							nextMoveState = MoveState.JUMP;
					}
					// not shooting, currently jump spinning
					else {
						// switch to regular JUMP state if moving down and below 1 tile height above jump start position
						if(samusBody.getVelocity().y <= 0f &&
								samusBody.getPosition().y <= startJumpY + UInfo.P2M(UInfo.TILEPIX_Y))
							nextMoveState = MoveState.JUMP;
						else
							nextMoveState = MoveState.JUMPSPIN;
					}
				}
				break;
		}

		// check for and apply vertical (jump) impulses, forces, etc.
		if(nextMoveState.isJump()) {
			if(jumpStart)
				samusBody.doJumpStart();
			if(isJumpForceEnabled)
				samusBody.doJumpContinue(delta);
		}

		// check for and apply horizontal movement impulses, forces, etc.
		if(isMoveHorizontal) {
			// cancel auto air move right?
			if(isAutoContinueRightAirMove && advice.moveLeft)
				isAutoContinueRightAirMove = false;
			// cancel auto air move left?
			else if(isAutoContinueLeftAirMove && advice.moveRight)
				isAutoContinueLeftAirMove = false;

			switch(nextMoveState) {
				case BALL:
				case STAND:
				case RUN:
				case SHOOT:
				default:
					samusBody.doGroundMove(advice.moveRight);
					break;
				case JUMP:
				case JUMPSHOOT:
					// samus can airmove if jumping and not in damage state
					if(curContactState != ContactState.DAMAGE)
						samusBody.doAirMove(advice.moveRight);
					break;
				case JUMPSPIN:
					// samus can airmove while jump spinning even when in damage state
					samusBody.doAirMove(advice.moveRight);
					break;
			}
		}
		// not moving left/right - may be on-ground or mid-air
		else {
			// The spin jump may cause air move to continue even though move left/right advice is not given,
			// check left...
			if(isAutoContinueRightAirMove)
				samusBody.doAirMove(true);
			// ... and right
			// Note: Auto left/right air move is cancelled when on-ground.
			else if(isAutoContinueLeftAirMove)
				samusBody.doAirMove(false);
			// if auto move is not continuing, then do stop move
			else {
				if(curContactState == ContactState.DAMAGE && contactStateTimer < 0.2f)
					samusBody.doStopMove(true);
				else
					samusBody.doStopMove(false);
			}
		}

		// check against maximum velocities, etc.
		samusBody.clampMove();

		// update prev velocity
		samusBody.postUpdate();

		// if advised move left or right (but not both at same time!) then set facing direction accordingly
		if(advice.moveRight^advice.moveLeft)
			isFacingRight = advice.moveRight;

		// update move state timer and current move state
		moveStateTimer = nextMoveState == curMoveState ? moveStateTimer + delta : 0f;
		curMoveState = nextMoveState;
	}

	private void checkDoStepSound() {
		if(curMoveState != MoveState.RUN)
			lastStepSoundTime = 0;
		else if(moveStateTimer - lastStepSoundTime >= STEP_SOUND_TIME) {
			lastStepSoundTime = moveStateTimer;
			agency.playSound(AudioInfo.Sound.Metroid.STEP);
		}
	}

	private boolean checkAndDoShoot(boolean shoot) {
		// can't shoot if not advised to shoot or cooldown has not finished
		if(!shoot || shootCooldownTime > agency.getGlobalTimer())
			return false;

		shootCooldownTime = agency.getGlobalTimer() + SHOOT_COOLDOWN;

		// calculate position and velocity of shot based on samus' orientation
		Vector2 velocity = new Vector2();
		Vector2 position = new Vector2();
		if(isFacingUp) {
			velocity.set(0f, SHOT_VEL);
			if(isFacingRight)
				position.set(SHOT_OFFSET_UP).add(samusBody.getPosition());
			else
				position.set(SHOT_OFFSET_UP).scl(-1, 1).add(samusBody.getPosition());
		}
		else if(isFacingRight) {
			velocity.set(SHOT_VEL, 0f);
			position.set(SHOT_OFFSET_RIGHT).add(samusBody.getPosition());
		}
		else {
			velocity.set(-SHOT_VEL, 0f);
			position.set(SHOT_OFFSET_RIGHT).scl(-1, 1).add(samusBody.getPosition());
		}

		// create shot
		ObjectProperties shotProps = SamusShot.makeAP(this, position, velocity);
		// check spawn point of shot, if it is in a solid tile then the shot immediately explodes
		if(isMapPointSolid(position)) {
			// add the immediate explode property to the properties list
			shotProps.put(CommonKV.Spawn.KEY_EXPIRE, true);
		}
		agency.createAgent(shotProps);
		agency.playSound(AudioInfo.Sound.Metroid.SHOOT);

		// shot fired, so return true
		return true;
	}

	private void processSprite(float delta) {
		if(supervisor.isRunningScript() && !supervisor.isRunningScriptMoveAdvice()) {
			ScriptedSpriteState sss = supervisor.getScriptAgentState().scriptedSpriteState;
			MoveState ms;
			switch(sss.spriteState) {
				case MOVE:
					ms = MoveState.RUN;
					break;
				case CLIMB:
					ms = MoveState.CLIMB;
					break;
				default:
					ms = MoveState.STAND;
					break;
			}
			samusSprite.update(delta, sss.position, ms, sss.facingRight, isFacingUp, sss.moveDir);
		}
		else {
			samusSprite.update(delta, samusBody.getPosition(), curMoveState, isFacingRight, isFacingUp, null);
			// toggle sprite on/off each frame while in contact damage state
			if(curContactState == ContactState.DAMAGE && isDrawThisFrame)
				isDrawThisFrame = false;
			else
				isDrawThisFrame = true;
		}
	}

	private void doDraw(AgencyDrawBatch batch) {
		// if a script is running and the sprite is visible then draw it
		if(supervisor.isRunningScript() && !supervisor.isRunningScriptMoveAdvice()) {
			if(supervisor.getScriptAgentState().scriptedSpriteState.visible)
				batch.draw(samusSprite);
		}
		// no script running, do the normal draw stuff
		else {
			if(isDrawThisFrame)
				batch.draw(samusSprite);
		}
	}
*/
	/*
	 * Check for contact with tiled collision map. If contact then get solid state of tile given by tileCoords.
	 */
/*	private boolean isMapTileSolid(Vector2 tileCoords) {
		CollisionTiledMapAgent octMap = samusBody.getSpine().getFirstContactByClass(CollisionTiledMapAgent.class);
		if(octMap == null)
			return false;
		return octMap.isMapTileSolid(tileCoords);
	}

	private boolean isMapPointSolid(Vector2 position) {
		CollisionTiledMapAgent octMap = samusBody.getSpine().getFirstContactByClass(CollisionTiledMapAgent.class);
		if(octMap == null)
			return false;
		return octMap.isMapPointSolid(position);
	}

	@Override
	public RoomBox getCurrentRoom() {
		return samusBody.getSpine().getCurrentRoom();
	}

	@Override
	public Vector2 getPosition() {
		return samusBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return samusBody.getBounds();
	}

	@Override
	public GameAgentObserver getObserver() {
		return observer;
	}

	@Override
	public AgentSupervisor getSupervisor() {
		return supervisor;
	}

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
			Vector2 he = new Vector2(samusSprite.getWidth(), samusSprite.getHeight());
			return (T) he;
		}
		return super.getProperty(key, defaultValue, cls);
	}

	@Override
	public void disposeAgent() {
		samusBody.dispose();
	}
}*/
