package kidridicarus.game.SMB1.agent.player.mario;

import java.util.LinkedList;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.Agent;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgentSupervisor;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.powerup.PowChar;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.MoveAdvice4x2;
import kidridicarus.game.SMB1.SMB1_Audio;
import kidridicarus.game.SMB1.SMB1_Pow;
import kidridicarus.game.SMB1.agent.TileBumpTakeAgent.TileBumpStrength;
import kidridicarus.game.SMB1.agent.other.pipewarp.PipeWarp;
import kidridicarus.game.SMB1.agent.player.mariofireball.MarioFireball;

class MarioBrain {
	private static final Vector2 DUCK_OFFSET = new Vector2(0f, UInfo.P2M(7f));
	private static final Vector2 GROW_OFFSET = DUCK_OFFSET;
	private static final float DEAD_DELAY_TIME = 3f;
	private static final float FIREBALL_OFFSET = UInfo.P2M(8f);
	private static final float SHOOT_COOLDOWN = 1/20f;
	private static final float NO_DAMAGE_TIME = 3f;
	private static final Vector2 DEAD_BOUNCE_IMP = new Vector2(0, 6f);
	private static final float POWERSTAR_MAXTIME = 15f;
	private static final float STARPOWER_DAMAGE = 1f;

	enum PowerState {
		SMALL, BIG, FIRE;
		boolean isBigBody() { return !this.equals(SMALL); }
	}
	enum MoveState {
		STAND, RUN, BRAKE, FALL, DUCK, DUCKSLIDE, DUCKFALL, DUCKJUMP, JUMP, DEAD, DEAD_BOUNCE, CLIMB;
		boolean equalsAny(MoveState ...otherStates) {
			for(MoveState state : otherStates) { if(this.equals(state)) return true; } return false;
		}
		boolean isDuck() { return this.equalsAny(DUCK, DUCKFALL, DUCKJUMP, DUCKSLIDE); }
		boolean isDuckNoSlide() { return this.equalsAny(DUCK, DUCKFALL, DUCKJUMP); }
		boolean isJump() { return this.equalsAny(JUMP, DUCKJUMP); }
		boolean isOnGround() { return this.equalsAny(STAND, RUN, BRAKE, DUCK, DUCKSLIDE); }
		boolean isDead() { return this.equalsAny(DEAD, DEAD_BOUNCE); }
	}

	private Mario parent;
	private AgentHooks parentHooks;
	private MarioBody body;
	private PlayerAgentSupervisor supervisor;
	private int coinTotal;
	private int pointTotal;
	private MoveState moveState;
	private float moveStateTimer;
	private PowerState powerState;
	private boolean isFacingRight;
	private float noDamageCooldown;
	private float starPowerCooldown;
	private boolean isNextJumpAllowed;
	private boolean isNextJumpDelayed;
	private boolean isJumpForceContinue;
	// next head bump is denied immediately following headbump and lasts until Agent moves downward
	private boolean isHeadBumped;
	private int activeFireballCount;
	private float shootCooldown;
	private boolean shootAdviceReset;
	private boolean didShootFireballThisFrame;
	private boolean gaveHeadBounce;
	// list of powerups received during contact update
	private LinkedList<Powerup> powerupsReceived;
	private boolean didTakeDamage;
	private boolean isDeadBounce;
	private Direction4 lastHorizontalMoveDir;
	private boolean isDuckSlideRight;
	private RoomBox lastKnownRoom;

	MarioBrain(Mario parent, AgentHooks parentHooks, MarioBody body, boolean isFacingRight,
			PowerState powerState, int coinTotal, int pointsTotal) {
		this.parent = parent;
		this.parentHooks = parentHooks;
		this.body = body;
		this.coinTotal = coinTotal;
		this.pointTotal = pointsTotal;
		supervisor = new PlayerAgentSupervisor(parent, parentHooks);
		moveState = MoveState.STAND;
		moveStateTimer = 0f;
		this.isFacingRight = isFacingRight;
		this.powerState = powerState;
		noDamageCooldown = 0f;
		starPowerCooldown = 0f;
		isDeadBounce = false;
		isNextJumpAllowed = false;
		isNextJumpDelayed = false;
		isJumpForceContinue = false;
		isHeadBumped = false;
		activeFireballCount = 0;
		shootCooldown = 0f;
		shootAdviceReset = true;
		didShootFireballThisFrame = false;
		gaveHeadBounce = false;
		powerupsReceived = new LinkedList<Powerup>();
		didTakeDamage = false;
		lastHorizontalMoveDir = Direction4.NONE;
		isDuckSlideRight = false;
		lastKnownRoom = null;
	}

	/*
	 * Check for and do head bumps during contact update, so bump tiles can show results of bump immediately
	 * by way of regular update. Also apply star power damage if needed.
	 */
	void processContactFrame(BrainContactFrameInput cFrameInput) {
		// update last known room if not dead, so dead player moving through other RoomBoxes won't cause problems
		if(moveState != MoveState.DEAD && cFrameInput.room != null)
			lastKnownRoom = cFrameInput.room;
		if(supervisor.isRunningScriptNoMoveAdvice())
			return;
		if(!isHeadBumped) {
			// if mario is big then hit hard
			if(powerState.isBigBody())
				isHeadBumped = body.getSpine().checkDoHeadBump(TileBumpStrength.HARD);
			else
				isHeadBumped = body.getSpine().checkDoHeadBump(TileBumpStrength.SOFT);
		}
		// if star powered then apply star power damage
		if(starPowerCooldown > 0f) {
			for(Agent agent : body.getSpine().getPushDamageContacts()) {
				// if they take contact damage then push it
				if(agent instanceof ContactDmgTakeAgent)
					((ContactDmgTakeAgent) agent).onTakeDamage(parent, STARPOWER_DAMAGE, body.getPosition());
			}
		}
	}

	SpriteFrameInput processFrame(FrameTime frameTime) {
		MoveAdvice4x2 moveAdvice = supervisor.pollMoveAdvice();

		// if a script is running with no move advice then apply scripted body state and exit
		if(supervisor.isRunningScriptNoMoveAdvice()) {
			ScriptedAgentState scriptedState = supervisor.getScriptAgentState();
			body.useScriptedBodyState(scriptedState.scriptedBodyState, powerState.isBigBody());
			isFacingRight = scriptedState.scriptedSpriteState.isFacingRight;
			// return null if scripted sprite is not visible
			if(!supervisor.getScriptAgentState().scriptedSpriteState.visible)
				return null;
			return getScriptedSpriteFrameInput(frameTime);
		}

		if(!moveState.isDead()) {
			processPowerupsReceived();
			processFireball(moveAdvice);
			processDamageTaken(frameTime);
			processHeadBouncesGiven();
			processPipeWarps(moveAdvice);

			// make a note of the last direction in which mario was moving, for duck sliding
			if(body.getSpine().isMovingInDir(Direction4.RIGHT))
				lastHorizontalMoveDir = Direction4.RIGHT;
			else if(body.getSpine().isMovingInDir(Direction4.LEFT))
				lastHorizontalMoveDir = Direction4.LEFT;
		}

		MoveState nextMoveState = getNextMoveState(moveAdvice);
		boolean moveStateChanged = nextMoveState != moveState;
		// if mario is dead...
		if(nextMoveState.isDead())
			processDeadMove(moveStateChanged, nextMoveState);
		else {
			// if on ground...
			if(nextMoveState.isOnGround())
				processGroundMove(moveAdvice, nextMoveState);
			// else do air move
			else
				processAirMove(moveAdvice, nextMoveState);

			// do space wrap last so that contacts are maintained
			body.getSpine().checkDoSpaceWrap(lastKnownRoom);
		}

		// decrement fireball shoot cooldown timer
		shootCooldown = shootCooldown < frameTime.timeDelta ? 0f : shootCooldown-frameTime.timeDelta;
		// decrement starpower cooldown timer
		starPowerCooldown = starPowerCooldown < frameTime.timeDelta ? 0f : starPowerCooldown-frameTime.timeDelta;

		moveStateTimer = moveState == nextMoveState ? moveStateTimer+frameTime.timeDelta : 0f;
		moveState = nextMoveState;

		return new MarioSpriteFrameInput(frameTime, body.getPosition(), moveState, powerState, isFacingRight,
				(noDamageCooldown > 0f), (starPowerCooldown > 0f), didShootFireballThisFrame, Direction4.NONE);
	}

	private SpriteFrameInput getScriptedSpriteFrameInput(FrameTime frameTime) {
		MoveState scriptedMoveState;
		ScriptedSpriteState sss = supervisor.getScriptAgentState().scriptedSpriteState;
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
		return new MarioSpriteFrameInput(frameTime, sss.position, scriptedMoveState, powerState, sss.isFacingRight,
				false, (starPowerCooldown > 0f), false, sss.moveDir);
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

	private void processDeadMove(boolean moveStateChanged, MoveState nextMoveState) {
		// if newly dead then disable contacts and start dead sound
		if(moveStateChanged) {
			body.allowOnlyDeadContacts();
			body.zeroVelocity(true, true);
			parentHooks.getEar().stopAllMusic();
			parentHooks.getEar().playSound(SMB1_Audio.Sound.MARIO_DIE);

			// do bounce up if needed
			if(nextMoveState == MoveState.DEAD_BOUNCE)
				body.applyImpulse(DEAD_BOUNCE_IMP);
		}
		// ... and if died a long time ago then do game over
		else if(moveStateTimer > DEAD_DELAY_TIME)
				supervisor.setGameOver();
		// ... else do nothing.
	}

	private void processPowerupsReceived() {
		for(Powerup pu : powerupsReceived) {
			if(pu.getPowerupCharacter() != PowChar.MARIO)
				supervisor.receiveNonCharPowerup(pu);

			applyPowerup(pu);
		}
		powerupsReceived.clear();
	}

	private void applyPowerup(Powerup pu) {
		PowerState newPowerState = powerState;
		if(pu instanceof SMB1_Pow.MushroomPow) {
			// if small then power up to big
			if(powerState == PowerState.SMALL)
				newPowerState = PowerState.BIG;
			parentHooks.getEar().playSound(SMB1_Audio.Sound.POWERUP_USE);
		}
		else if(pu instanceof SMB1_Pow.FireFlowerPow) {
			// if small then power up to big
			if(powerState == PowerState.SMALL)
				newPowerState = PowerState.BIG;
			// if big then power up to fire
			else if(powerState == PowerState.BIG)
				newPowerState = PowerState.FIRE;
			parentHooks.getEar().playSound(SMB1_Audio.Sound.POWERUP_USE);
		}
		else if(pu instanceof SMB1_Pow.Mush1UpPow) {
			// TODO apply 1-UP mushroom
		}
		else if(pu instanceof SMB1_Pow.PowerStarPow) {
			// Reset the begin contacts list, so that any turtles that were head bounced don't instantly die
			// when mario gets a powerstar.
			body.getSpine().getPushDamageContacts();
			starPowerCooldown = POWERSTAR_MAXTIME;
			parentHooks.getEar().startSinglePlayMusic(SMB1_Audio.Music.STARPOWER);
		}
		else if(pu instanceof SMB1_Pow.CoinPow) {
			coinTotal++;
			if(coinTotal > 99)
				coinTotal = 0;
			// TODO award extra life for 100 coins collected
		}
		else if(pu instanceof SMB1_Pow.PointsPow)
			pointTotal += 100;

		// if growing then increase body size
		if(newPowerState.isBigBody() && !powerState.isBigBody())
			body.setMarioBodyStuff(body.getPosition().cpy().add(GROW_OFFSET), body.getVelocity(), true, false);
		powerState = newPowerState;
	}

	private void processFireball(MoveAdvice4x2 moveAdvice) {
		// check do shoot fireball
		didShootFireballThisFrame = false;
		if(!moveAdvice.action0) {
			shootAdviceReset = true;
			return;
		}
		if(!isFireBallAllowed())
			return;

		activeFireballCount++;
		shootCooldown += SHOOT_COOLDOWN;
		didShootFireballThisFrame = true;
		// do not shoot again until shoot advice has been deactivated for at least 1 frame
		shootAdviceReset = false;

		Vector2 offset;
		if(isFacingRight)
			offset = body.getPosition().cpy().add(FIREBALL_OFFSET, 0f);
		else
			offset = body.getPosition().cpy().add(-FIREBALL_OFFSET, 0f);

		// create fireball with remove listener attached, so new fireballs can be created when old ones expire
		MarioFireball fireball = (MarioFireball) parentHooks.createAgent(
				MarioFireball.makeAP(offset, isFacingRight, parent));
		parentHooks.createAgentRemoveListener(fireball, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { activeFireballCount--; }
			});
		// boom goes the dynamite
		parentHooks.getEar().playSound(SMB1_Audio.Sound.FIREBALL);
	}

	private void processDamageTaken(FrameTime frameTime) {
		// exit if invulnerable to next damage
		if(noDamageCooldown > 0f) {
			noDamageCooldown -= frameTime.timeDelta;
			didTakeDamage = false;
			return;
		}
		// do not take damage during star power
		else if(starPowerCooldown > 0f)
			return;
		// exit if no damage taken this frame
		else if(!didTakeDamage)
			return;

		// apply damage and modify body if needed
		didTakeDamage = false;
		switch(powerState) {
			case SMALL:
				isDeadBounce = true;
				break;
			case BIG:
			case FIRE:
				powerState = PowerState.SMALL;
				noDamageCooldown = NO_DAMAGE_TIME;
				body.setMarioBodyStuff(body.getPosition().cpy().sub(GROW_OFFSET), body.getVelocity(), false, false);
				parentHooks.getEar().playSound(SMB1_Audio.Sound.POWERDOWN);
				break;
		}
	}

	private void processGroundMove(MoveAdvice4x2 moveAdvice, MoveState nextMoveState) {
		// if was not ducking and is now ducking then define ducking body
		if(!moveState.isDuck() && nextMoveState.isDuck()) {
			body.setMarioBodyStuff(body.getPosition().cpy().sub(DUCK_OFFSET), body.getVelocity(),
					powerState.isBigBody(), true);
		}
		// if was ducking and is now now ducking then define regular body
		else if(moveState.isDuck() && !nextMoveState.isDuck()) {
			body.setMarioBodyStuff(body.getPosition().cpy().add(DUCK_OFFSET), body.getVelocity(),
					powerState.isBigBody(), false);
		}

		// If current move type is air and next move type is ground and jump is advised then delay jump until
		// jump advice is released.
		if(!moveState.isOnGround() && nextMoveState.isOnGround() && moveAdvice.action1)
			isNextJumpDelayed = true;

		// if not moving up (i.e. resting on ground) then allow next jump...
		if(!body.getSpine().isMovingInDir(Direction4.UP)) {
			isNextJumpAllowed = true;
			// ... and if jump advice is released then turn off jump delay
			if(!moveAdvice.action1)
				isNextJumpDelayed = false;
		}

		// do other ground move changes
		Direction4 moveDir = getMarioMoveDir(moveAdvice);
		boolean doHorizontalImpulse = false;
		boolean doDecelImpulse = false;
		boolean doDuckSlideImpulse = false;
		switch(nextMoveState) {
			case STAND:
				if(moveDir.isHorizontal())
					doHorizontalImpulse = true;
				else
					doDecelImpulse = true;
				break;
			case RUN:
				if(moveDir.isHorizontal())
					doHorizontalImpulse = true;
				else
					doDecelImpulse = true;
				break;
			case BRAKE:
				doDecelImpulse = true;
				break;
			case DUCK:
				doDecelImpulse = true;
				break;
			case DUCKSLIDE:
				doDuckSlideImpulse = true;
				break;
			default:
				throw new IllegalStateException("Only ground move states allowed, wrong nextMoveState="+nextMoveState);
		}

		// if not ducking then apply move advice to facing direction
		if(moveDir.isHorizontal() && !moveState.isDuckNoSlide()) {
			if(moveDir == Direction4.RIGHT)
				isFacingRight = true;
			else if(moveDir == Direction4.LEFT)
				isFacingRight = false;
		}

		if(doHorizontalImpulse)
			body.getSpine().applyWalkMove(isFacingRight, moveAdvice.action0);
		if(doDecelImpulse)
			body.getSpine().applyDecelMove(isFacingRight, nextMoveState.isDuck());
		if(doDuckSlideImpulse)
			body.getSpine().applyDuckSlideMove(isDuckSlideRight);

		// reset head bump flag while on ground and not moving up
		if(body.getVelocity().y < UInfo.VEL_EPSILON)
			isHeadBumped = false;
	}

	private void processAirMove(MoveAdvice4x2 moveAdvice, MoveState nextMoveState) {
		if(!body.getSpine().isMovingInDir(Direction4.UP))
			isJumpForceContinue = false;

		boolean isMoveStateChange = (moveState != nextMoveState);
		switch(nextMoveState) {
			case JUMP:
			case DUCKJUMP:
				if(isMoveStateChange) {
					isNextJumpAllowed = false;
					isJumpForceContinue = true;
					body.getSpine().applyJumpImpulse();

					if(powerState.isBigBody())
						parentHooks.getEar().playSound(SMB1_Audio.Sound.MARIO_BIGJUMP);
					else
						parentHooks.getEar().playSound(SMB1_Audio.Sound.MARIO_SMLJUMP);
				}
				else {
					if(!moveAdvice.action1)
						isJumpForceContinue = false;
				}
				break;
			case FALL:
			case DUCKFALL:
				break;
			default:
				throw new IllegalStateException("Only air move states allowed, wrong nextMoveState="+nextMoveState);
		}

		// mario might be ducking and moving right/left
		if(moveAdvice.moveRight && !moveAdvice.moveLeft)
			body.getSpine().applyAirMove(true);
		else if(moveAdvice.moveLeft && !moveAdvice.moveRight)
			body.getSpine().applyAirMove(false);

		// if jump force must continue then apply it, unless head bumped something
		if(isHeadBumped) {
			body.getSpine().applyHeadBumpMove();
			isHeadBumped = false;
		}
		else if(isJumpForceContinue) {
			if(isMoveStateChange)
				body.getSpine().applyJumpForce(0f);
			else
				body.getSpine().applyJumpForce(moveStateTimer);
		}

		body.getSpine().capFallVelocity();
	}

	private MoveState getNextMoveState(MoveAdvice4x2 moveAdvice) {
		if(moveState.isDead())
			return moveState;
		else if(body.getSpine().isContactDespawn() || body.getSpine().isContactScrollKillBox())
			return MoveState.DEAD;
		else if(isDeadBounce)
			return MoveState.DEAD_BOUNCE;
		// if [on ground flag is true] and agent isn't [moving upward while in air move state], then do ground move
		else if(body.getSpine().isOnGround() && !(body.getSpine().isMovingInDir(Direction4.UP) &&
				!moveState.isOnGround())) {
			return getNextMoveStateGround(moveAdvice);
		}
		// do air move
		else
			return getNextMoveStateAir();
	}

	private boolean isFireBallAllowed() {
		return activeFireballCount < 2 && powerState == PowerState.FIRE && shootCooldown <= 0f &&
				!moveState.isDuckNoSlide() && shootAdviceReset;
	}

	private MoveState getNextMoveStateGround(MoveAdvice4x2 moveAdvice) {
		Direction4 moveDir = getMarioMoveDir(moveAdvice);

		// if advised to jump and jumping is okay...
		if(moveAdvice.action1 && isNextJumpAllowed && !isNextJumpDelayed) {
			// if ducking already then duck jump
			if(moveState.isDuck())
				return MoveState.DUCKJUMP;
			// otherwise regular jump
			else
				return MoveState.JUMP;
		}
		else if(moveState.isDuck()) {
			// if not advising move down then check for unduck - if can't unduck then duckslide
			if(moveDir == Direction4.DOWN)
				return MoveState.DUCK;
			if(!moveState.isOnGround())
				return MoveState.STAND;
			Vector2 bodyTilePos = UInfo.VectorM2T(body.getPosition());

			// Check the space above and around mario to test if mario can unduck normally, or if he is in a
			// tight spot:

			// if the tile above ducking mario is not solid then allow stand
			if(!body.getSpine().isMapTileSolid(bodyTilePos.cpy().add(0, 1)))
				return MoveState.STAND;

			Vector2 subTilePos = UInfo.VectorM2SubT(body.getPosition());
			// If the player's last velocity direction was rightward, and their position is in the left half
			// of the tile, and the tile above and to the left of them is solid, then the player should
			// duckslide right.
			if((lastHorizontalMoveDir == Direction4.RIGHT &&
					subTilePos.x <= 0.5f && body.getSpine().isMapTileSolid(bodyTilePos.cpy().add(-1, 1))) ||
					(subTilePos.x > 0.5f && !body.getSpine().isMapTileSolid(bodyTilePos.cpy().add(1, 1))) ||
					(lastHorizontalMoveDir == Direction4.RIGHT &&
					subTilePos.x > 0.5f && body.getSpine().isMapTileSolid(bodyTilePos.cpy().add(1, 1)))) {
				isDuckSlideRight = true;
			}
			// the only other option is to duckslide left
			else
				isDuckSlideRight = false;

			return MoveState.DUCKSLIDE;
		}
		// if big body mario and move down is advised then duck
		else if(powerState.isBigBody() && moveDir == Direction4.DOWN)
			return MoveState.DUCK;
		// moving too slowly?
		else if(body.getSpine().isStandingStill())
			return MoveState.STAND;
		// moving in wrong direction?
		else if(body.getSpine().isBraking(isFacingRight))
			return MoveState.BRAKE;
		else
			return MoveState.RUN;
	}

	private MoveState getNextMoveStateAir() {
		// if in jump state then continue jump state
		if(moveState.isJump())
			return moveState;
		// not in jump state
		else {
			// if is ducking then do duck fall 
			if(moveState.isDuck())
				return MoveState.DUCKFALL;
			// not ducking so do regular fall
			else
				return MoveState.FALL;
		}
	}

	private Direction4 getMarioMoveDir(MoveAdvice4x2 moveAdvice) {
		// if no left/right move then return unmodified direction from move advice
		if(moveAdvice.moveLeft^moveAdvice.moveRight == false) {
			// down takes priority over up advice
			if(moveAdvice.moveDown)
				return Direction4.DOWN;
			else if(moveAdvice.moveUp)
				return Direction4.UP;
			else
				return Direction4.NONE;
		}

		// if advising move down while advising left/right then return no direction
		if(moveAdvice.moveDown)
			return Direction4.NONE;
		// ignore move up advice and return horizontal move direction
		else {
			if(moveAdvice.moveRight)
				return Direction4.RIGHT;
			else
				return Direction4.LEFT;
		}
	}

	RoomBox getCurrentRoom() {
		return lastKnownRoom;
	}

	PlayerAgentSupervisor getSupervisor() {
		return supervisor;
	}

	boolean onTakePowerup(Powerup pu) {
		if(moveState == MoveState.DEAD)
			return false;
		powerupsReceived.add(pu);
		return true;
	}

	boolean onTakeDamage() {
		// no damage taken if already took damage this frame, or if invulnerable, or if star powered, or if dead
		if(didTakeDamage || noDamageCooldown > 0f || starPowerCooldown > 0f || moveState == MoveState.DEAD)
			return false;
		didTakeDamage = true;
		return true;
	}

	boolean onGiveHeadBounce(Agent agent) {
		// no head bouncing while star powered or dead
		if(starPowerCooldown > 0f || moveState == MoveState.DEAD)
			return false;
		// if other agent has bounds and head bounce is allowed then give head bounce to agent
		Rectangle otherBounds = AP_Tool.getBounds(agent);
		if(otherBounds != null && body.getSpine().isGiveHeadBounceAllowed(otherBounds)) {
			gaveHeadBounce = true;
			return true;
		}
		return false;
	}

	PowerState getPowerState() {
		return powerState;
	}

	boolean isFacingRight() {
		return isFacingRight;
	}

	int getCoinTotal() {
		return coinTotal;
	}

	int getPointTotal() {
		return pointTotal;
	}
}
