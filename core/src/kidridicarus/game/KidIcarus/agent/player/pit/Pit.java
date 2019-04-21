package kidridicarus.game.KidIcarus.agent.player.pit;

import java.util.LinkedList;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.playeragent.PlayerAgentSupervisor;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentproperties.GetPropertyListenerDirection4;
import kidridicarus.common.agentproperties.GetPropertyListenerInteger;
import kidridicarus.common.agentproperties.GetPropertyListenerVector2;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.powerup.PowChar;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.MoveAdvice4x4;
import kidridicarus.game.KidIcarus.agent.player.pit.HUD.PitHUD;
import kidridicarus.game.KidIcarus.agent.player.pitarrow.PitArrow;
import kidridicarus.game.SMB1.agent.HeadBounceGiveAgent;
import kidridicarus.game.SMB1.agent.other.bumptile.BumpTile.TileBumpStrength;
import kidridicarus.game.SMB1.agent.other.pipewarp.PipeWarp;
import kidridicarus.game.info.KidIcarusAudio;
import kidridicarus.game.info.KidIcarusKV;
import kidridicarus.game.info.KidIcarusPow;
import kidridicarus.game.info.SMB1_Pow;

/*
 * Notes:
 * Upon receiving damage contact, pit immediately moves 4 pixels to the left, with no change in velocity.
 *   (double check 4 pixels)
 * 
 * Pit faces these directions under these conditions:
 *   -if not aiming up
 *     -faces right when moving right (or if stopped and advised move right)
 *     -faces left when moving left (or if stopped and advised move left)
 *   -otherwise, aiming up:
 *     -if move right/left advice is given then use move advice to determine facing direction
 *     -otherwise retain previous facing direction
 * Glitches implemented:
 *   -duck, unduck re-shoot - if pit shoots, then quickly ducks and unducks, he can shoot more often than normal
 */
public class Pit extends PlayerAgent implements PowerupTakeAgent, ContactDmgTakeAgent, HeadBounceGiveAgent {
	private static final int MAX_HEARTS_COLLECTED = 999;
	private static final int MAX_HEALTH = 7;
	private static final int START_HEALTH = 7;
	private static final float NO_DAMAGE_TIME = 2f;
	private static final float PRE_JUMP_TIME = 0.1f;
	private static final float JUMPUP_FORCE_TIME = 0.5f;
	private static final float SHOOT_COOLDOWN = 0.3f;
	private static final float SHOT_VEL = 2f;
	private static final Vector2 SHOT_OFFSET_RIGHT = UInfo.VectorP2M(4, 1);
	private static final Vector2 SHOT_OFFSET_UP = UInfo.VectorP2M(1, 5);
	private static final float DEAD_DELAY_TIME = 3f;
	private static final Vector2 SHOT_OFFSET_HEAD_IN_TILE = UInfo.VectorP2M(0, 4);
	private static final Vector2 DUCK_POS_OFFSET = UInfo.VectorP2M(0, 4);

	enum MoveState {
		STAND, AIMUP, WALK, DUCK, PRE_JUMP, PRE_JUMP_DUCK, PRE_JUMP_AIMUP, JUMP, JUMP_DUCK, JUMP_AIMUP, CLIMB, DEAD;

		public boolean equalsAny(MoveState ...otherStates) {
			for(MoveState state : otherStates) { if(this.equals(state)) return true; } return false;
		}
		public boolean isGround() { return this.equalsAny(STAND, WALK, DUCK, AIMUP); }
		public boolean isDuck() { return this.equalsAny(DUCK, PRE_JUMP_DUCK, JUMP_DUCK); }
		public boolean isPreJump() { return this.equalsAny(PRE_JUMP, PRE_JUMP_DUCK, PRE_JUMP_AIMUP); }
		public boolean isJump() { return this.equalsAny(PRE_JUMP, PRE_JUMP_DUCK, PRE_JUMP_AIMUP, JUMP,
				JUMP_AIMUP, JUMP_DUCK); }
		public boolean isAimUp() { return this.equalsAny(AIMUP, PRE_JUMP_AIMUP, JUMP_AIMUP); }
	}

	private PlayerAgentSupervisor supervisor;
	private PitBody body;
	private PitSprite sprite;
	private PitHUD playerHUD;
	private MoveState moveState;
	private float moveStateTimer;
	private boolean isFacingRight;
	private boolean isNextJumpAllowed;
	private float jumpForceTimer;
	private boolean isNextShotAllowed;
	private float shootCooldownTimer;
	private boolean takeDamageThisFrame;
	private float noDamageCooldown;
	private boolean isOnGroundHeadInTile;
	private boolean gaveHeadBounce;
	private boolean isHeadBumped;
	// list of powerups received during contact update
	private LinkedList<Powerup> powerupsReceived;
	private int heartsCollected;
	private int health;
	private RoomBox lastKnownRoom;

	public Pit(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		setStateFromProperties(properties);
		createGetPropertyListeners();
		body = new PitBody(this, agency.getWorld(), AP_Tool.getCenter(properties),
				properties.get(CommonKV.KEY_VELOCITY, new Vector2(0f, 0f), Vector2.class), false);
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doContactUpdate(); }
			});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.POST_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doPostUpdate(); }
			});
		sprite = new PitSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_TOP, new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { doDraw(adBatch); }
			});
		playerHUD = new PitHUD(this, agency.getAtlas());
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.PLAYER_HUD, new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { playerHUD.draw(adBatch); }
			});

		supervisor = new PlayerAgentSupervisor(this);
	}

	private void setStateFromProperties(ObjectProperties properties) {
		moveState = MoveState.STAND;
		moveStateTimer = 0f;
		isFacingRight = false;
		if(properties.get(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class) == Direction4.RIGHT)
			isFacingRight = true;
		isNextJumpAllowed = false;	// false until land on solid ground
		jumpForceTimer = 0f;
		isNextShotAllowed = true;
		shootCooldownTimer = 0f;
		isOnGroundHeadInTile = false;
		powerupsReceived = new LinkedList<Powerup>();
		health = properties.get(KidIcarusKV.KEY_HEALTH, START_HEALTH, Integer.class);
		heartsCollected = properties.get(KidIcarusKV.KEY_HEART_COUNT, 0, Integer.class);
		noDamageCooldown = 0f;
		takeDamageThisFrame = false;
		gaveHeadBounce = false;
		isHeadBumped = false;
		lastKnownRoom = null;
	}

	private void createGetPropertyListeners() {
		addGetPropertyListener(CommonKV.Script.KEY_SPRITE_SIZE, new GetPropertyListenerVector2() {
				@Override
				public Vector2 getVector2() { return new Vector2(sprite.getWidth(), sprite.getHeight()); }
			});
		addGetPropertyListener(CommonKV.KEY_DIRECTION, new GetPropertyListenerDirection4() {
				@Override
				public Direction4 getDirection4() { return isFacingRight ? Direction4.RIGHT : Direction4.LEFT; }
			});
		addGetPropertyListener(KidIcarusKV.KEY_HEALTH, new GetPropertyListenerInteger() {
				@Override
				public Integer getInteger() { return health; }
			});
		addGetPropertyListener(KidIcarusKV.KEY_HEART_COUNT, new GetPropertyListenerInteger() {
				@Override
				public Integer getInteger() { return heartsCollected; }
			});
	}

	/*
	 * Check for and do head bumps during contact update, so bump tiles can show results of bump immediately
	 * by way of regular update. Also apply star power damage if needed.
	 */
	private void doContactUpdate() {
		if(supervisor.isRunningScriptNoMoveAdvice())
			return;
		// exit if head bump flag hasn't reset
		if(isHeadBumped)
			return;
		// if ducking then hit soft
		if(moveState.isDuck())
			isHeadBumped = body.getSpine().checkDoHeadBump(TileBumpStrength.SOFT);
		// otherwise hit hard
		else
			isHeadBumped = body.getSpine().checkDoHeadBump(TileBumpStrength.HARD);
	}

	private void doUpdate(float delta) {
		MoveAdvice4x4 moveAdvice = supervisor.pollMoveAdvice();
		processContacts(delta, moveAdvice);
		processMove(delta, moveAdvice);
		processSprite(delta);
	}

	private void processContacts(float delta, MoveAdvice4x4 moveAdvice) {
		if(supervisor.isRunningScriptNoMoveAdvice())
			return;

		processPowerupsReceived();
		processHeadBouncesGiven();
		processDamageTaken(delta);
		processPipeWarps(moveAdvice);
	}

	private void processMove(float delta, MoveAdvice4x4 moveAdvice) {
		// if a script is running with no move advice then switch to scripted body state and exit
		if(supervisor.isRunningScriptNoMoveAdvice()) {
			ScriptedAgentState scriptedState = supervisor.getScriptAgentState();
			body.useScriptedBodyState(scriptedState.scriptedBodyState);
			isFacingRight = scriptedState.scriptedSpriteState.isFacingRight;
			return;
		}

		MoveState nextMoveState = getNextMoveState(moveAdvice);
		boolean moveStateChanged = nextMoveState != moveState;
		if(nextMoveState == MoveState.DEAD)
			processDeadMove(moveStateChanged);
		else {
			body.getSpine().checkDoBodySizeChange(nextMoveState.isDuck());

			if(nextMoveState.isGround())
				processGroundMove(moveAdvice, nextMoveState);
			else
				processAirMove(delta, moveAdvice, nextMoveState);

			// check/do facing direction change
			if(body.getSpine().isWalkingRight())
				isFacingRight = true;
			else if(body.getSpine().isWalkingLeft())
				isFacingRight = false;

			processShoot(delta, moveAdvice);

			// do space wrap last so that contacts are maintained
			body.getSpine().checkDoSpaceWrap(lastKnownRoom);
		}

		if((moveState.isPreJump() && nextMoveState.isPreJump()) ||
				(moveState.isJump() && nextMoveState.isJump())) {
			moveStateTimer += delta;
		}
		else
			moveStateTimer = moveState == nextMoveState ? moveStateTimer+delta : 0f;
		moveState = nextMoveState;
	}

	private void processPowerupsReceived() {
		for(Powerup pu : powerupsReceived) {
			if(pu instanceof KidIcarusPow.AngelHeartPow) {
				heartsCollected += ((KidIcarusPow.AngelHeartPow) pu).getNumHearts();  
				if(heartsCollected > MAX_HEARTS_COLLECTED)
					heartsCollected = MAX_HEARTS_COLLECTED;
			}
			else if(pu instanceof KidIcarusPow.ChaliceHealthPow)
				addHealth(((KidIcarusPow.ChaliceHealthPow) pu).getHealAmount());
			// TODO: implement ignore points pow for pit somewhere better
			else if(pu.getPowerupCharacter() != PowChar.PIT && !(pu instanceof SMB1_Pow.PointsPow))
				supervisor.receiveNonCharPowerup(pu);
		}
		powerupsReceived.clear();
	}

	private void addHealth(int healAmt) {
		// add health and cap it to MAX_HEALTH
		health = health+healAmt > MAX_HEALTH ? MAX_HEALTH : health+healAmt;
	}

	private void processDamageTaken(float delta) {
		// check for contact with scroll kill box (insta-kill)
		if(body.getSpine().isContactScrollKillBox()) {
			health = 0;
			return;
		}
		// if invulnerable to damage then exit
		else if(noDamageCooldown > 0f) {
			noDamageCooldown -= delta;
			takeDamageThisFrame = false;
			return;
		}
		// if no damage taken this frame then exit
		else if(!takeDamageThisFrame)
			return;

		// apply damage against health, health cannot be less than zero
		health = health-1 > 0 ? health-1 : 0;
		// reset frame take damage amount
		takeDamageThisFrame = false;
		// start no damage period
		noDamageCooldown = NO_DAMAGE_TIME;
		// ouchie sound
		agency.getEar().playSound(KidIcarusAudio.Sound.Pit.HURT);
	}

	private void processHeadBouncesGiven() {
		// if a head bounce was given in the update frame then reset the flag and do bounce move
		if(gaveHeadBounce) {
			gaveHeadBounce = false;
			body.getSpine().applyHeadBounce();
		}
	}

	private void processPipeWarps(MoveAdvice4x4 moveAdvice) {
		PipeWarp pw = body.getSpine().getEnterPipeWarp(moveAdvice.getMoveDir4());
		if(pw != null)
			pw.use(this);
	}

	private MoveState getNextMoveState(MoveAdvice4x4 moveAdvice) {
		if(moveState == MoveState.DEAD || body.getSpine().isContactDespawn() || health <= 0)
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

	private MoveState getNextMoveStateGround(MoveAdvice4x4 moveAdvice) {
		// if advised jump, and allowed to jump, and not aiming up, and not currently ducking...
		if(moveAdvice.action1 && isNextJumpAllowed && moveState != MoveState.AIMUP && moveState != MoveState.DUCK) {
			// Pit can start to jump and do one other thing:
			//     1) Duck, or
			//     2) Aim Up
			// Duck takes priority over Aim Up.
			if(moveAdvice.moveDown)
				return MoveState.PRE_JUMP_DUCK;
			else if(moveAdvice.moveUp)
				return MoveState.PRE_JUMP_AIMUP;
			else
				return MoveState.PRE_JUMP;
		}
		else if(moveAdvice.moveDown)
			return MoveState.DUCK;
		else if(moveAdvice.moveUp)
			return MoveState.AIMUP;
		// if advised move horizontally, or if already moving horizontally, then return RUN
		else if(moveAdvice.moveRight || moveAdvice.moveLeft || !body.getSpine().isStandingStill())
			return MoveState.WALK;
		else
			return MoveState.STAND;
	}

	private MoveState getNextMoveStateAir(MoveAdvice4x4 moveAdvice) {
		if(moveState.isPreJump() && moveStateTimer <= PRE_JUMP_TIME) {
			if(moveAdvice.moveDown)
				return MoveState.PRE_JUMP_DUCK;
			else if(moveAdvice.moveUp)
				return MoveState.PRE_JUMP_AIMUP;
			else
				return MoveState.PRE_JUMP;
		}
		else {
			if(moveAdvice.moveDown)
				return MoveState.JUMP_DUCK;
			else if(moveAdvice.moveUp)
				return MoveState.JUMP_AIMUP;
			else
				return MoveState.JUMP;
		}
	}

	private void processDeadMove(boolean moveStateChanged) {
		// if newly dead then disable contacts and start dead sound
		if(moveStateChanged) {
			body.getSpine().applyDead();
			agency.getEar().stopAllMusic();
			agency.getEar().startSinglePlayMusic(KidIcarusAudio.Music.PIT_DIE);
		}
		// ... and if died a long time ago then do game over
		else if(moveStateTimer > DEAD_DELAY_TIME)
				supervisor.setGameOver();
		// ... else do nothing.
	}

	private void processGroundMove(MoveAdvice4x4 moveAdvice, MoveState nextMoveState) {
		// next jump changes to allowed when on ground and not advising jump move 
		if(!moveAdvice.action1)
			isNextJumpAllowed = true;

		// update the "head-in-tile" flag, will be used by sprite for correct sprite offset
		isOnGroundHeadInTile = body.getSpine().isHeadInTile();

		Direction4 moveDir = Direction4.NONE;
		switch(nextMoveState) {
			case STAND:
			case WALK:
			case DUCK:
				// check for move application
				if(moveAdvice.moveRight)
					moveDir = Direction4.RIGHT;
				else if(moveAdvice.moveLeft)
					moveDir = Direction4.LEFT;

				// if head is in tile and body is not moving, then facing direction can change
				if(isOnGroundHeadInTile && body.getSpine().isStandingStill()) {
					if(moveAdvice.moveRight)
						isFacingRight = true;
					else if(moveAdvice.moveLeft)
						isFacingRight = false;
				}

				break;
			case AIMUP:
				// Aimup freezes x velocity while on ground - by not zeroing Y velocity, this code should
				// avoid problems with vertical moving platforms - still problems with horizontally moving
				// platforms though - unless zeroVelocity checks for relative velocity and uses relative zero?
				// TODO !
				body.zeroVelocity(true, false);
				if(moveAdvice.moveRight)
					isFacingRight = true;
				else if(moveAdvice.moveLeft)
					isFacingRight = false;
				break;
			default:
				throw new IllegalStateException("Wrong ground nextMoveState = " + nextMoveState);
		}

		// if not ducking and head is stuck in tile then disable horizontal move
		boolean disableHMove = !nextMoveState.isDuck() && isOnGroundHeadInTile;
		// move right advice takes priority over move left advice, but disable move is highest priority
		if(!disableHMove && moveDir == Direction4.RIGHT)
			body.getSpine().applyWalkMove(true);
		else if(!disableHMove && moveDir == Direction4.LEFT)
			body.getSpine().applyWalkMove(false);
		else
			body.getSpine().applyStopMove();

		// reset head bump flag while on ground and not moving up
		if(body.getVelocity().y < UInfo.VEL_EPSILON)
			isHeadBumped = false;
	}

	private void processAirMove(float delta, MoveAdvice4x4 moveAdvice, MoveState nextMoveState) {
		isOnGroundHeadInTile = false;
		switch(nextMoveState) {
			case PRE_JUMP:
			case PRE_JUMP_DUCK:
			case PRE_JUMP_AIMUP:
				// if previously on ground and advised jump then jump 
				if(moveState.isGround() && moveAdvice.action1) {
					isNextJumpAllowed = false;
					jumpForceTimer = PRE_JUMP_TIME+JUMPUP_FORCE_TIME;
					body.getSpine().applyJumpVelocity();
					agency.getEar().playSound(KidIcarusAudio.Sound.Pit.JUMP);
				}
				else if(moveStateTimer <= PRE_JUMP_TIME)
					body.getSpine().applyJumpVelocity();
				break;
			case JUMP:
			case JUMP_DUCK:
			case JUMP_AIMUP:
				// This takes care of player falling off ledge - player cannot jump again until at least
				// one frame has elapsed where player was on ground and not advised to  jump (to prevent
				// re-jump bouncing).
				isNextJumpAllowed = false;

				// if jump force continues and jump is advised then do jump force
				if(jumpForceTimer > 0f && moveAdvice.action1)
					body.getSpine().applyJumpForce(jumpForceTimer-PRE_JUMP_TIME, JUMPUP_FORCE_TIME);
				break;
			default:
				throw new IllegalStateException("Wrong air nextMoveState = " + nextMoveState);
		}

		// disallow jump force until next jump if [jump advice stops] or [body stops moving up]
		if(!moveAdvice.action1 || !body.getSpine().isMovingInDir(Direction4.UP))
			jumpForceTimer = 0f;

		// move right advice takes priority over move left advice
		if(moveAdvice.moveRight)
			body.getSpine().applyAirMove(true);
		else if(moveAdvice.moveLeft)
			body.getSpine().applyAirMove(false);

		if(isHeadBumped) {
			body.getSpine().applyHeadBumpMove();
			isHeadBumped = false;
		}

		// decrement jump force timer
		jumpForceTimer = jumpForceTimer > delta ? jumpForceTimer-delta : 0f;
	}

	private void processShoot(float delta, MoveAdvice4x4 moveAdvice) {
		if(moveAdvice.action0 && isNextShotAllowed && shootCooldownTimer <= 0f && !moveState.isDuck())
			doShoot();
		else if(!moveAdvice.action0)
			isNextShotAllowed = true;

		// GLITCH when Pit ducks, the shoot cooldown timer resets and Pit can shoot again immediately
		if(moveState.isDuck())
			shootCooldownTimer = 0f;
		else
			shootCooldownTimer = shootCooldownTimer > delta ? shootCooldownTimer-delta : 0f;
	}

	private void doShoot() {
		isNextShotAllowed = false;
		shootCooldownTimer = SHOOT_COOLDOWN;

		// calculate position and velocity of shot based on samus' orientation
		Direction4 arrowDir;
		Vector2 velocity = new Vector2();
		Vector2 position = new Vector2();
		if(moveState.isAimUp()) {
			arrowDir = Direction4.UP;
			velocity.set(0f, SHOT_VEL);
			if(isFacingRight)
				position.set(SHOT_OFFSET_UP).add(body.getPosition());
			else
				position.set(SHOT_OFFSET_UP).scl(-1, 1).add(body.getPosition());
		}
		else {
			if(isFacingRight) {
				arrowDir = Direction4.RIGHT;
				velocity.set(SHOT_VEL, 0f);
				position.set(SHOT_OFFSET_RIGHT).add(body.getPosition());
			}
			else {
				arrowDir = Direction4.LEFT;
				velocity.set(-SHOT_VEL, 0f);
				position.set(SHOT_OFFSET_RIGHT).scl(-1, 1).add(body.getPosition());
			}

			if(isOnGroundHeadInTile)
				position.add(SHOT_OFFSET_HEAD_IN_TILE);
		}

		// create shot; if the spawn point of shot is in a solid tile then the shot must show for a short time
		agency.createAgent(PitArrow.makeAP(
				this, position, velocity, arrowDir, body.getSpine().isMapPointSolid(position)));
		agency.getEar().playSound(KidIcarusAudio.Sound.Pit.SHOOT);
	}

	private void doPostUpdate() {
		// let body update previous position/velocity
		body.postUpdate();
		// update last known room if not dead, so dead player moving through other RoomBoxes won't cause problems
		if(moveState != MoveState.DEAD) {
			RoomBox nextRoom = body.getSpine().getCurrentRoom();
			if(nextRoom != null)
				lastKnownRoom = nextRoom;
		}
	}

	private void processSprite(float delta) {
		if(supervisor.isRunningScriptNoMoveAdvice()) {
			MoveState scriptedMoveState;
			ScriptedSpriteState sss = supervisor.getScriptAgentState().scriptedSpriteState;
			switch(sss.spriteState) {
				case MOVE:
					scriptedMoveState = MoveState.WALK;
					break;
				case CLIMB:
					scriptedMoveState = MoveState.CLIMB;
					break;
				case STAND:
				default:
					scriptedMoveState = MoveState.STAND;
					break;
			}

			sprite.update(delta, sss.position, scriptedMoveState, sss.isFacingRight, false, false, false, false,
					sss.moveDir);
		}
		else {
			sprite.update(delta, body.getPosition(), moveState, isFacingRight, (noDamageCooldown > 0f),
					(shootCooldownTimer > 0f), isOnGroundHeadInTile, body.getSpine().isMovingInDir(Direction4.UP),
					Direction4.NONE);
		}
	}

	private void doDraw(Eye adBatch) {
		// exit if using scripted sprite state and script says don't draw
		if(supervisor.isRunningScriptNoMoveAdvice() &&
				!supervisor.getScriptAgentState().scriptedSpriteState.visible)
			return;
		adBatch.draw(sprite);
	}

	@Override
	public boolean onTakePowerup(Powerup pu) {
		if(moveState == MoveState.DEAD)
			return false;
		powerupsReceived.add(pu);
		return true;
	}

	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		// don't take more damage if damage taken already this frame, or if invulnerable
		if(moveState == MoveState.DEAD || takeDamageThisFrame || noDamageCooldown > 0f)
			return false;
		takeDamageThisFrame = true;
		return true;
	}

	@Override
	public boolean onGiveHeadBounce(Agent agent) {
		// if other agent has bounds and head bounce is allowed then give head bounce to agent
		Rectangle otherBounds = AP_Tool.getBounds(agent);
		if(otherBounds != null && body.getSpine().isGiveHeadBounceAllowed(otherBounds)) {
			gaveHeadBounce = true;
			return true;
		}
		return false;
	}

	@Override
	public PlayerAgentSupervisor getSupervisor() {
		return supervisor;
	}

	@Override
	public RoomBox getCurrentRoom() {
		return lastKnownRoom;
	}

	@Override
	protected Vector2 getPosition() {
		// use the "real" position of Pit, which is independent of body size, to improve smooth screen scroll
		Vector2 offset = new Vector2(0f, 0f);
		if(isOnGroundHeadInTile || moveState.isDuck())
			offset.set(DUCK_POS_OFFSET);
		return body.getPosition().cpy().add(offset);
	}

	@Override
	protected Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	protected Vector2 getVelocity() {
		return body.getVelocity();
	}

	@Override
	public void dispose() {
		body.dispose();
	}
}
